package com.project.hrms.service;

import com.project.hrms.model.*;
import com.project.hrms.model.enums.AttendanceStatus;
import com.project.hrms.model.enums.DayType;
import com.project.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonthlyTimesheetService {

    private final WorkdayRepository workdayRepo;
    private final MonthlyTimesheetRepository monthlyRepo;
    private final EmployeeRepository employeeRepo;

    // --- HÀM 1: CHẠY CHO TOÀN BỘ CÔNG TY (Controller sẽ gọi hàm này) ---
    @Transactional
    public void generateMonthlyTimesheetForAll(int month, int year) {
        List<Employee> employees = employeeRepo.findAll();
        for (Employee emp : employees) {
            calculateEmployeeMonthlyTimesheet(emp, month, year);
        }
    }

    // --- HÀM 2: TÍNH TOÁN CHI TIẾT CHO 1 NHÂN VIÊN ---
    @Transactional
    public void calculateEmployeeMonthlyTimesheet(Employee employee, int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 1. Lấy dữ liệu Workday
        List<Workday> workdays = workdayRepo.findByEmployee_EmployeeIdAndDateBetween(
                employee.getEmployeeId(), startDate, endDate
        );

        // 2. Khởi tạo biến cộng dồn
        double actualWorkDays = 0.0;
        double paidLeaveDays = 0.0;
        double unpaidLeaveDays = 0.0;
        double holidayLeaveDays = 0.0;

        double otWeekdayHours = 0.0;
        double otWeekendHours = 0.0;
        double otHolidayHours = 0.0;

        int totalLateMinutes = 0;
        int lateCount = 0;

        // 3. Tính công chuẩn (Trừ Chủ Nhật)
        double standardDays = 0.0;
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate d = LocalDate.of(year, month, day);
            if (d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                standardDays += 1.0;
            }
        }

        // 4. Vòng lặp quét từng ngày (Aggregation)
        for (Workday wd : workdays) {
            AttendanceStatus status = wd.getAttendanceStatus();
            DayType dayType = wd.getWorkType(); // NORMAL, WEEKEND

            // --- A. TÍNH CÔNG ĐI LÀM ---
            if (status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE) {
                if (wd.getHoursWorked() != null && wd.getHoursWorked() > 0) {
                    actualWorkDays += 1.0;
                }
            }

            // --- B. TÍNH CÔNG NGHỈ ---
            if (status == AttendanceStatus.LEAVE_PAID) {
                paidLeaveDays += 1.0;
            } else if (status == AttendanceStatus.LEAVE_UNPAID) {
                unpaidLeaveDays += 1.0;
            }
            // else if (status == AttendanceStatus.HOLIDAY) { holidayLeaveDays += 1.0; }

            // --- C. TÍNH OT (Phân loại) ---
            if (wd.getHoursOvertime() != null && wd.getHoursOvertime() > 0) {
                double ot = wd.getHoursOvertime();

                // Phân loại OT dựa trên loại ngày làm việc
                if (dayType == DayType.WEEKEND) {
                    otWeekendHours += ot;
                } else if (dayType == DayType.HOLIDAY) { // Nếu bạn có Enum Holiday
                    otHolidayHours += ot;
                } else {
                    otWeekdayHours += ot; // Ngày thường
                }
            }

            // --- D. TÍNH ĐI MUỘN ---
            if (status == AttendanceStatus.LATE) {
                lateCount++;
            }
        }

        // 5. Lưu vào DB
        // [QUAN TRỌNG] Sử dụng hàm Repo vừa thêm để check tồn tại
        List<MonthlyTimesheet> existingList = monthlyRepo.findByEmployee_EmployeeIdAndMonthAndYear(
                employee.getEmployeeId(), month, year
        );

        MonthlyTimesheet timesheet;

        if (existingList.isEmpty()) {
            // Case 1: Chưa có -> Tạo mới
            timesheet = new MonthlyTimesheet();
            timesheet.setEmployee(employee);
            timesheet.setMonth(month);
            timesheet.setYear(year);
            timesheet.setStatus("DRAFT");
        } else {
            // Case 2: Đã có -> Lấy cái đầu tiên để update (Logic List)
            timesheet = existingList.get(0);
        }

        // Set Data
        timesheet.setStandardWorkDays(standardDays);
        timesheet.setActualWorkDays(actualWorkDays);
        timesheet.setPaidLeaveDays(paidLeaveDays);
        timesheet.setUnpaidLeaveDays(unpaidLeaveDays);
        timesheet.setHolidayLeaveDays(holidayLeaveDays);

        timesheet.setOtWeekdayHours(otWeekdayHours);
        timesheet.setOtWeekendHours(otWeekendHours);
        timesheet.setOtHolidayHours(otHolidayHours);

        // Tổng hợp OT
        double totalOt = otWeekdayHours + otWeekendHours + otHolidayHours;
        timesheet.setTotalOtHours(totalOt);

        timesheet.setLateCount(lateCount);
        timesheet.setTotalLateMinutes(totalLateMinutes);

        monthlyRepo.save(timesheet);
    }
}