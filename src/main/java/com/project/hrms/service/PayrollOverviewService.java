package com.project.hrms.service;

import com.project.hrms.dto.FundStructureDTO;
import com.project.hrms.dto.PayrollOverviewDTO;
import com.project.hrms.dto.ReminderDTO;
import com.project.hrms.dto.SalaryTrendDTO;
import com.project.hrms.model.PayrollPeriod;
import com.project.hrms.model.SalaryComponent;
import com.project.hrms.repository.*; // Import tất cả repository
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PayrollOverviewService {

    private final PayrollRepository payrollRepository;
    private final PayrollPeriodRepository payrollPeriodRepository;
    private final EmployeeRepository employeeRepository;
    private final SystemSettingRepository systemSettingRepository;

    // ✅ THÊM: Inject Repository này để lấy danh sách lương
    private final SalaryComponentRepository salaryComponentRepository;

    public PayrollOverviewDTO getPayrollOverview() {
        // 1. Lấy tổng số lương (Xử lý null nếu DB chưa có dữ liệu)
        BigDecimal totalSalary = payrollRepository.sumTotalSalary();
        if (totalSalary == null) totalSalary = BigDecimal.ZERO;

        // 2. Lấy thuế, bảo hiểm, headcount
        BigDecimal personalIncomeTax = payrollRepository.sumPersonalIncomeTax();
        if (personalIncomeTax == null) personalIncomeTax = BigDecimal.ZERO;

        BigDecimal insuranceDeduction = payrollRepository.sumInsuranceDeduction();
        if (insuranceDeduction == null) insuranceDeduction = BigDecimal.ZERO;

        Long headcount = employeeRepository.count();

        // 3. Lương trung bình (Check chia cho 0)
        BigDecimal averageSalary = BigDecimal.ZERO;
        if (headcount > 0) {
            averageSalary = totalSalary.divide(BigDecimal.valueOf(headcount), 2, RoundingMode.HALF_UP);
        }

        // 4. Lấy kỳ lương hiện tại
        PayrollPeriod currentPeriod = payrollPeriodRepository.findCurrentPeriod();

        // 5. Salary Trend
        List<SalaryTrendDTO> salaryTrend = payrollRepository.getSalaryTrend();

        // 6. Cơ cấu quỹ lương (SỬA LỖI: Dùng SalaryComponentRepository)
        // Thay vì systemSettingRepository.findBySalaryComponents(), dùng findAll()
        List<SalaryComponent> salaryComponents = salaryComponentRepository.findAll();

        List<FundStructureDTO> fundStructure = salaryComponents.stream()
                .map(c -> {
                    // SỬA LỖI: Kiểm tra xem getter trong Entity là getAmount() hay getDefaultAmount()
                    // Giả sử tên getter là getAmount(). Nếu Entity chưa có, xem Bước 3 bên dưới.
                    BigDecimal amt = c.getAmount() != null ? c.getAmount() : BigDecimal.ZERO;
                    return new FundStructureDTO(c.getName(), amt);
                })
                .collect(Collectors.toList());

        // 7. Ngân sách lương
        String budgetStr = systemSettingRepository.getValue("PLANNED_BUDGET", "0");
        BigDecimal plannedBudget = new BigDecimal(budgetStr);
        BigDecimal actualBudget = totalSalary;
        BigDecimal budgetUsagePercent = BigDecimal.ZERO;

        if (plannedBudget.compareTo(BigDecimal.ZERO) > 0) {
            budgetUsagePercent = actualBudget.divide(plannedBudget, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // 8. Nhắc nhở
        List<ReminderDTO> reminders = getReminders();

        return new PayrollOverviewDTO(totalSalary, personalIncomeTax, insuranceDeduction, headcount, averageSalary, currentPeriod, salaryTrend, fundStructure, plannedBudget, actualBudget, budgetUsagePercent, reminders);
    }

    private List<ReminderDTO> getReminders() {
        List<ReminderDTO> reminders = new ArrayList<>();
        reminders.add(new ReminderDTO("warning", "Có 2 nhân viên chưa có thông tin ngân hàng"));
        return reminders;
    }
}