package com.project.hrms.service;

import com.project.hrms.dto.*;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.*;
import com.project.hrms.model.enums.SalaryComponentType;
import com.project.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final PayrollItemRepository payrollItemRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final SalaryProfileRepository salaryProfileRepository;
    private final SalaryComponentRepository salaryComponentRepository;
    private final MonthlyTimesheetRepository monthlyTimesheetRepository;
    private final SystemSettingRepository systemSettingRepository;

    // ✅ THÊM: Inject EmployeeRepository để dùng cho calculateBatch
    private final EmployeeRepository employeeRepository;

    // =========================
    // 1) TÍNH LƯƠNG 1 NHÂN VIÊN
    // =========================
    @Transactional
    public PayrollResponseDTO calculatePayrollForEmployee(PayrollCalculateRequestDTO dto) {
        // 1. Validate input
        if (dto == null || dto.getEmployeeId() == null || dto.getPayrollPeriodId() == null || dto.getMonth() == null || dto.getYear() == null) {
            throw new InvalidParamException("Thiếu tham số: employeeId, payrollPeriodId, month, year");
        }

        // 2. Load payroll period
        PayrollPeriod period = payrollPeriodRepository.findById(dto.getPayrollPeriodId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy kỳ lương payrollPeriodId=" + dto.getPayrollPeriodId()));

        if (Boolean.TRUE.equals(period.getIsClosed())) {
            throw new InvalidParamException("Kỳ lương đã đóng, không thể tính lại.");
        }

        // 3. Load monthly timesheet
        MonthlyTimesheet ts = monthlyTimesheetRepository.findTopByEmployee_EmployeeIdAndMonthAndYearOrderByIdDesc(
                dto.getEmployeeId(), dto.getMonth(), dto.getYear()
        );
        if (ts == null) {
            throw new DataNotFoundException("Không tìm thấy monthly_timesheet cho employeeId="
                    + dto.getEmployeeId() + ", month=" + dto.getMonth() + ", year=" + dto.getYear());
        }

        // 4. Load salary profiles
        List<SalaryProfile> profiles = salaryProfileRepository.findByEmployee_EmployeeId(dto.getEmployeeId());
        if (profiles == null || profiles.isEmpty()) {
            throw new DataNotFoundException("Nhân viên chưa có SalaryProfile (cấu phần lương).");
        }

        // 5. Load system settings
        Map<String, String> settings = loadSettingMap(Set.of("STANDARD_WORK_DAYS", "STANDARD_WORK_HOURS", "OT_COEFF_WEEKDAY", "OT_COEFF_WEEKEND", "OT_COEFF_HOLIDAY", "LATE_PENALTY_PER_MIN"));
        BigDecimal standardWorkDays = parseBd(settings.get("STANDARD_WORK_DAYS"), bd(ts.getStandardWorkDays(), "26"));
        BigDecimal standardWorkHours = parseBd(settings.get("STANDARD_WORK_HOURS"), new BigDecimal("8"));
        BigDecimal otCoeffWeekday = parseBd(settings.get("OT_COEFF_WEEKDAY"), new BigDecimal("1.5"));
        BigDecimal otCoeffWeekend = parseBd(settings.get("OT_COEFF_WEEKEND"), new BigDecimal("2.0"));
        BigDecimal otCoeffHoliday = parseBd(settings.get("OT_COEFF_HOLIDAY"), new BigDecimal("3.0"));
        BigDecimal latePenaltyPerMin = parseBd(settings.get("LATE_PENALTY_PER_MIN"), BigDecimal.ZERO);

        // 6. Calculate fixed earnings and deductions
        BigDecimal fixedEarnings = profiles.stream()
                .filter(p -> p.getSalaryComponent() != null && p.getSalaryComponent().getType() == SalaryComponentType.earning)
                .map(p -> nvl(p.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal fixedDeductions = profiles.stream()
                .filter(p -> p.getSalaryComponent() != null && p.getSalaryComponent().getType() == SalaryComponentType.deduction)
                .map(p -> nvl(p.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 7. Calculate hour rate
        BigDecimal divisor = standardWorkDays.multiply(standardWorkHours);
        if (divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidParamException("STANDARD_WORK_DAYS hoặc STANDARD_WORK_HOURS không hợp lệ.");
        }
        BigDecimal hourRate = fixedEarnings.divide(divisor, 6, RoundingMode.HALF_UP);

        // 8. Calculate OT pay
        BigDecimal otPay = BigDecimal.ZERO;
        otPay = otPay.add(hourRate.multiply(bd(ts.getOtWeekdayHours(), "0")).multiply(otCoeffWeekday));
        otPay = otPay.add(hourRate.multiply(bd(ts.getOtWeekendHours(), "0")).multiply(otCoeffWeekend));
        otPay = otPay.add(hourRate.multiply(bd(ts.getOtHolidayHours(), "0")).multiply(otCoeffHoliday));

        // 9. Unpaid deduction
        BigDecimal unpaidDays = bd(ts.getUnpaidLeaveDays(), "0");
        BigDecimal unpaidDeduct = BigDecimal.ZERO;
        if (standardWorkDays.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal dayRate = fixedEarnings.divide(standardWorkDays, 6, RoundingMode.HALF_UP);
            unpaidDeduct = dayRate.multiply(unpaidDays);
        }

        // 10. Late penalty
        int lateMinutes = ts.getTotalLateMinutes() == null ? 0 : ts.getTotalLateMinutes();
        BigDecimal latePenalty = (latePenaltyPerMin.compareTo(BigDecimal.ZERO) > 0)
                ? latePenaltyPerMin.multiply(BigDecimal.valueOf(lateMinutes))
                : hourRate.divide(new BigDecimal("60"), 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(lateMinutes));

        // 11. Total salary
        BigDecimal totalSalary = fixedEarnings
                .add(otPay)
                .subtract(fixedDeductions)
                .subtract(unpaidDeduct)
                .subtract(latePenalty)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal personalIncomeTax = totalSalary.multiply(new BigDecimal("0.10"));
        BigDecimal insuranceDeduction = fixedEarnings.multiply(new BigDecimal("0.08"));

        // 12. Save payroll (check for existing payroll)
        Payroll payroll = payrollRepository.findByEmployee_EmployeeIdAndPayrollPeriod_PayrollPeriodId(dto.getEmployeeId(), dto.getPayrollPeriodId()).orElseGet(Payroll::new);
        if (payroll.getPayrollId() == null) {
            // Create new payroll if not exists
            Employee empRef = new Employee();
            empRef.setEmployeeId(dto.getEmployeeId());
            payroll.setEmployee(empRef);
            payroll.setPayrollPeriod(period);
        }

        payroll.setTaxAmount(personalIncomeTax);
        payroll.setInsuranceAmount(insuranceDeduction);
        payroll.setTotalSalary(totalSalary);

        // Rebuild payroll items (delete and reinsert if necessary)
        if (payroll.getPayrollId() != null) {
            payrollItemRepository.deleteByPayroll_PayrollId(payroll.getPayrollId());
        }
        payroll.getPayrollItems().clear();

        // Add fixed items
        for (SalaryProfile sp : profiles) {
            if (sp.getSalaryComponent() == null) continue;
            PayrollItem item = new PayrollItem();
            item.setPayroll(payroll);
            item.setSalaryComponent(sp.getSalaryComponent());
            item.setAmount(nvl(sp.getAmount()).setScale(2, RoundingMode.HALF_UP));
            payroll.getPayrollItems().add(item);
        }

        // Add variable items like OT, unpaid leave deduction, late penalty
        Map<String, SalaryComponent> compMap = loadComponentsByCode(Set.of("OT_PAY", "UNPAID_LEAVE_DEDUCT", "LATE_PENALTY"));
        if(compMap.containsKey("OT_PAY")) payroll.getPayrollItems().add(buildItem(payroll, compMap.get("OT_PAY"), otPay));
        if(compMap.containsKey("UNPAID_LEAVE_DEDUCT")) payroll.getPayrollItems().add(buildItem(payroll, compMap.get("UNPAID_LEAVE_DEDUCT"), unpaidDeduct));
        if(compMap.containsKey("LATE_PENALTY")) payroll.getPayrollItems().add(buildItem(payroll, compMap.get("LATE_PENALTY"), latePenalty));

        // 13. Save payroll
        Payroll saved = payrollRepository.save(payroll);
        return toResponseDTO(saved);
    }


    // =========================
    // 2) TÍNH LƯƠNG BATCH (SỬA LỖI COMPILER)
    // =========================
    @Transactional
    public int calculateBatch(Long periodId, int month, int year) {
        // Cách đơn giản: Lấy tất cả nhân viên và thử tính lương cho từng người
        // (Trong thực tế nên query list nhân viên có Timesheet để tối ưu hơn)
        List<Employee> employees = employeeRepository.findAll();
        int count = 0;

        for (Employee emp : employees) {
            try {
                // Tạo DTO request giả lập
                PayrollCalculateRequestDTO dto = new PayrollCalculateRequestDTO();
                dto.setEmployeeId(emp.getEmployeeId());
                dto.setPayrollPeriodId(periodId);
                dto.setMonth(month);
                dto.setYear(year);

                calculatePayrollForEmployee(dto);
                count++;
            } catch (Exception e) {
                // Bỏ qua nếu nhân viên này không đủ điều kiện (VD: không có timesheet)
                // Hoặc log lỗi: log.warn("Lỗi tính lương NV {}: {}", emp.getEmployeeId(), e.getMessage());
            }
        }
        return count;
    }

    // =========================
    // 3) LẤY DANH SÁCH BẢNG LƯƠNG THEO KỲ
    // =========================
    public Page<PayrollListItemDTO> listPayrollByPeriod(Long periodId, Pageable pageable) {
        return payrollRepository.findPayrollListByPeriod(periodId, pageable);
    }

    // =========================
    // 4) LẤY CHI TIẾT BẢNG LƯƠNG
    // =========================
    public PayrollResponseDTO getPayrollDetail(Long payrollId) {
        Payroll p = payrollRepository.findWithItemsByPayrollId(payrollId)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy bảng lương ID=" + payrollId));
        return toResponseDTO(p);
    }

    // ===== Helper Methods (Giữ nguyên) =====

    private BigDecimal parseBd(String value, BigDecimal defaultValue) {
        if (value == null || value.trim().isEmpty()) return defaultValue;
        try { return new BigDecimal(value); } catch (NumberFormatException e) { return defaultValue; }
    }

    private BigDecimal bd(Object value, String defaultValue) {
        if (value == null) return new BigDecimal(defaultValue);
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Double) return BigDecimal.valueOf((Double) value);
        if (value instanceof Integer) return BigDecimal.valueOf((Integer) value);
        try { return new BigDecimal(value.toString()); } catch (Exception e) { return new BigDecimal(defaultValue); }
    }

    private Map<String, String> loadSettingMap(Set<String> keys) {
        List<SystemSetting> rows = systemSettingRepository.findBySettingKeyIn(keys);
        Map<String, String> map = new HashMap<>();
        for (SystemSetting s : rows) map.put(s.getSettingKey(), s.getValue());
        return map;
    }

    private Map<String, SalaryComponent> loadComponentsByCode(Set<String> codes) {
        return salaryComponentRepository.findAll().stream()
                .filter(sc -> codes.contains(sc.getCode()))
                .collect(Collectors.toMap(SalaryComponent::getCode, x -> x));
    }

    private PayrollItem buildItem(Payroll payroll, SalaryComponent comp, BigDecimal amount) {
        PayrollItem item = new PayrollItem();
        item.setPayroll(payroll);
        item.setSalaryComponent(comp);
        item.setAmount(nvl(amount).setScale(2, RoundingMode.HALF_UP));
        return item;
    }



    private PayrollResponseDTO toResponseDTO(Payroll payroll) {
        PayrollResponseDTO res = new PayrollResponseDTO();
        res.setPayrollId(payroll.getPayrollId());
        res.setEmployeeId(payroll.getEmployee().getEmployeeId());
        res.setPayrollPeriodId(payroll.getPayrollPeriod().getPayrollPeriodId());
        res.setTotalSalary(nvl(payroll.getTotalSalary()));
        res.setItems(payroll.getPayrollItems().stream()
                .map(it -> new PayrollItemResponseDTO(
                        it.getSalaryComponent().getSalaryComponentId(),
                        it.getSalaryComponent().getCode(),
                        it.getSalaryComponent().getName(),
                        it.getSalaryComponent().getType().name(),
                        nvl(it.getAmount())
                ))
                .collect(Collectors.toList()));
        return res;
    }

    private BigDecimal nvl(BigDecimal x) { return x == null ? BigDecimal.ZERO : x; }
}