package com.project.hrms.service;

import com.project.hrms.dto.TimesheetSummaryDTO;
import com.project.hrms.model.Employee;
import com.project.hrms.model.Workday;
import com.project.hrms.repository.EmployeeRepository;
import com.project.hrms.repository.WorkdayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimesheetService implements ITimesheetService {

    private final WorkdayRepository workdayRepository;
    private final EmployeeRepository employeeRepository;
    private final IDailyTimesheetService dailyTimesheetService;

    @Override
    public List<TimesheetSummaryDTO> getMonthlyTimesheetSummary(int month, int year, Long departmentId) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // Lấy danh sách nhân viên
        List<Employee> employees;
        if (departmentId != null) {
            employees = employeeRepository.findByDepartment_DepartmentId(departmentId);
        } else {
            employees = employeeRepository.findAll();
        }

        List<TimesheetSummaryDTO> summaryList = new ArrayList<>();

        for (Employee emp : employees) {
            // Lấy dữ liệu Workday đã xử lý sẵn
            List<Workday> monthlyWorkdays = workdayRepository.findByEmployee_EmployeeIdAndDateBetween(
                    emp.getEmployeeId(), startDate, endDate);

            // Cộng dồn (Aggregation)
            double actualWorkDays = monthlyWorkdays.stream().mapToDouble(Workday::getStandardWorkDays).sum();
            double paidLeaveDays = monthlyWorkdays.stream().mapToDouble(Workday::getPaidLeaveDays).sum();
            double unpaidLeaveDays = monthlyWorkdays.stream().mapToDouble(Workday::getUnauthorizedLeaveDays).sum();
            double overtimeHours = monthlyWorkdays.stream().mapToDouble(Workday::getOvertimeHours).sum();
            int totalLateMinutes = monthlyWorkdays.stream().mapToInt(Workday::getLateMinutes).sum();

            // Tổng công hưởng lương = Đi làm + Nghỉ phép
            double totalPayable = actualWorkDays + paidLeaveDays;

            // Tính công chuẩn (Đơn giản hóa: lấy tổng ngày trong tháng trừ CN)
            // (Thực tế nên lấy từ cấu hình hệ thống)
            double standardWorkDays = 26.0;

            // Build DTO
            TimesheetSummaryDTO dto = TimesheetSummaryDTO.builder()
                    .employeeId(emp.getEmployeeId())
                    .employeeCode(emp.getEmployeeCode())
                    .fullName(emp.getFullName())
                    .jobPosition(emp.getJobPosition().getName())
                    .standardWorkDays(standardWorkDays)
                    .actualWorkDays(actualWorkDays)
                    .paidLeaveDays(paidLeaveDays)
                    .unpaidLeaveDays(unpaidLeaveDays)
                    .overtimeHours(overtimeHours)
                    .totalLateMinutes(totalLateMinutes)
                    .totalPayableDays(totalPayable)
                    .build();

            summaryList.add(dto);
        }

        return summaryList;
    }

    @Override
    public void runDailyProcessManually(String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        dailyTimesheetService.processAllDailyWorkday(date);
    }
}