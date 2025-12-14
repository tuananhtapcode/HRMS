package com.project.hrms.service;

import com.project.hrms.dto.TimesheetDetailDTO;
import com.project.hrms.dto.TimesheetSummaryDTO;
import com.project.hrms.model.Employee;
import com.project.hrms.model.SystemSetting;
import com.project.hrms.model.Workday;
import com.project.hrms.model.enums.AttendanceStatus;
import com.project.hrms.repository.EmployeeRepository;
import com.project.hrms.repository.SystemSettingRepository;
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
    private final SystemSettingRepository settingRepository;

    @Override
    public List<TimesheetSummaryDTO> getMonthlyTimesheetSummary(int month, int year, Long departmentId) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 1. LẤY CẤU HÌNH TỪ DB
        // Số công chuẩn tháng (VD: 26)
        double standardDaysConfig = getSettingValueAsDouble("STANDARD_WORK_DAYS", 26.0);
        // Số giờ chuẩn 1 ngày (VD: 8.0) <-- QUAN TRỌNG
        double standardHoursConfig = getSettingValueAsDouble("STANDARD_WORK_HOURS", 8.0);

        // 2. Lấy danh sách nhân viên
        List<Employee> employees;
        if (departmentId != null) {
            employees = employeeRepository.findByDepartment_DepartmentId(departmentId);
        } else {
            employees = employeeRepository.findAll();
        }

        List<TimesheetSummaryDTO> summaryList = new ArrayList<>();

        for (Employee emp : employees) {
            // 3. Lấy dữ liệu Workday
            List<Workday> monthlyWorkdays = workdayRepository.findByEmployee_EmployeeIdAndDateBetween(
                    emp.getEmployeeId(), startDate, endDate);

            // 4. TÍNH TOÁN TỔNG HỢP (AGGREGATION)

            // a. Tổng giờ làm việc thực tế
            double totalHoursWorked = monthlyWorkdays.stream()
                    .mapToDouble(w -> w.getHoursWorked() != null ? w.getHoursWorked() : 0.0)
                    .sum();

            // b. Tổng giờ OT
            double overtimeHours = monthlyWorkdays.stream()
                    .mapToDouble(w -> w.getHoursOvertime() != null ? w.getHoursOvertime() : 0.0)
                    .sum();

            // c. Quy đổi ngày nghỉ phép có lương ra GIỜ
            // Logic: Nếu nghỉ phép, coi như được hưởng số giờ còn thiếu của ngày đó
            double totalPaidLeaveHours = monthlyWorkdays.stream()
                    .filter(w -> w.getAttendanceStatus() == AttendanceStatus.LEAVE_PAID)
                    .mapToDouble(w -> {
                        double worked = (w.getHoursWorked() != null) ? w.getHoursWorked() : 0.0;
                        // Ví dụ: Ca 8h, làm 0h -> Hưởng 8h nghỉ. Ca 8h, làm 4h -> Hưởng 4h nghỉ.
                        return Math.max(0, standardHoursConfig - worked);
                    })
                    .sum();

            // d. Đếm số ngày nghỉ không lương (Chỉ để báo cáo)
            long unpaidLeaveCount = monthlyWorkdays.stream()
                    .filter(w -> w.getAttendanceStatus() == AttendanceStatus.LEAVE_UNPAID)
                    .count();

            // e. Số lần đi muộn
            long lateCount = monthlyWorkdays.stream()
                    .filter(w -> w.getAttendanceStatus() == AttendanceStatus.LATE)
                    .count();

            // === TÍNH TOÁN CÔNG ===

            // Công đi làm thực tế = Tổng giờ làm / Giờ chuẩn
            double actualWorkDays = totalHoursWorked / standardHoursConfig;

            // Tổng công hưởng lương = (Tổng giờ làm + Tổng giờ nghỉ có lương) / Giờ chuẩn
            // Logic này chính xác tuyệt đối kể cả trường hợp nghỉ nửa buổi
            double totalPayableDays = (totalHoursWorked + totalPaidLeaveHours) / standardHoursConfig;

            // 5. Build DTO
            TimesheetSummaryDTO dto = TimesheetSummaryDTO.builder()
                    .employeeId(emp.getEmployeeId())
                    .employeeCode(emp.getEmployeeCode())
                    .fullName(emp.getFullName())
                    .jobPosition(emp.getJobPosition() != null ? emp.getJobPosition().getName() : "N/A")

                    .standardWorkDays(standardDaysConfig)
                    .actualWorkDays(actualWorkDays)
                    .paidLeaveDays(totalPaidLeaveHours / standardHoursConfig) // Quy đổi ngược ra công để hiển thị
                    .unpaidLeaveDays((double) unpaidLeaveCount)
                    .overtimeHours(overtimeHours)
                    .totalLateCount((int) lateCount)
                    .totalPayableDays(totalPayableDays) // Đây là con số quan trọng nhất để tính lương
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

    // Hàm phụ trợ lấy setting an toàn
    private double getSettingValueAsDouble(String key, double defaultValue) {
        return settingRepository.findById(key)
                .map(setting -> {
                    try {
                        return Double.parseDouble(setting.getValue());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    @Override
    public List<TimesheetDetailDTO.Response> getMonthlyTimesheetDetails(int month, int year, Long departmentId) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 1. Lấy danh sách nhân viên (theo phòng ban hoặc tất cả)
        List<Employee> employees;
        if (departmentId != null) {
            employees = employeeRepository.findByDepartment_DepartmentId(departmentId);
        } else {
            employees = employeeRepository.findAll();
        }

        List<TimesheetDetailDTO.Response> result = new ArrayList<>();

        for (Employee emp : employees) {
            // 2. Lấy list Workday của nhân viên này trong tháng
            List<Workday> workdays = workdayRepository.findByEmployee_EmployeeIdAndDateBetween(
                    emp.getEmployeeId(), startDate, endDate);

            // 3. Map sang DTO chi tiết từng ngày
            List<TimesheetDetailDTO.DailyItem> dailyItems = workdays.stream()
                    .map(w -> TimesheetDetailDTO.DailyItem.builder()
                            .date(w.getDate())
                            .status(w.getAttendanceStatus()) // Đây là cái bạn cần: PRESENT, LATE...
                            .hoursWorked(w.getHoursWorked())
                            .hoursOvertime(w.getHoursOvertime())
                            .build())
                    .toList();

            // 4. Gom vào DTO của nhân viên
            result.add(TimesheetDetailDTO.Response.builder()
                    .employeeId(emp.getEmployeeId())
                    .employeeCode(emp.getEmployeeCode())
                    .fullName(emp.getFullName())
                    .dailyRecords(dailyItems)
                    .build());
        }

        return result;
    }
    @Override
    public TimesheetDetailDTO.Response getMyMonthlyTimesheet(int month, int year, Long currentEmployeeId) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 1. Lấy thông tin nhân viên (để hiển thị tên, mã...)
        Employee emp = employeeRepository.findById(currentEmployeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // 2. Lấy dữ liệu Workday của CHÍNH NHÂN VIÊN ĐÓ trong tháng
        List<Workday> workdays = workdayRepository.findByEmployee_EmployeeIdAndDateBetween(
                currentEmployeeId, startDate, endDate);

        // 3. Map sang DTO chi tiết (Daily Items)
        List<TimesheetDetailDTO.DailyItem> dailyItems = workdays.stream()
                .map(w -> TimesheetDetailDTO.DailyItem.builder()
                        .date(w.getDate())
                        .status(w.getAttendanceStatus())
                        .hoursWorked(w.getHoursWorked())
                        .hoursOvertime(w.getHoursOvertime())
                        .build())
                .toList();

        // 4. Trả về kết quả
        return TimesheetDetailDTO.Response.builder()
                .employeeId(emp.getEmployeeId())
                .employeeCode(emp.getEmployeeCode())
                .fullName(emp.getFullName())
                .dailyRecords(dailyItems)
                .build();
    }// Status quan trọng: PRESENT, LATE, LEAVE_PAID...
}