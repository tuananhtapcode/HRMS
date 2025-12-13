package com.project.hrms.service;

import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.model.*;
import com.project.hrms.model.enums.AttendanceStatus;
import com.project.hrms.model.enums.DayType;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyTimesheetService implements IDailyTimesheetService {

    private final WorkdayRepository workdayRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRecordRepository attendanceRepo;
    private final LeaveRequestRepository leaveRepo;
    private final OvertimeRequestRepository overtimeRepo;
    private final ShiftAssignmentRepository assignmentRepo;

    @Override
    @Transactional
    public void processDailyWorkday(Long employeeId, LocalDate date) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new DataNotFoundException("Employee not found"));

        // 1. Tìm hoặc tạo Workday mới
        Workday workday = workdayRepository.findByEmployeeAndDate(employee, date)
                .orElse(new Workday());
        workday.setEmployee(employee);
        workday.setDate(date);

        // 2. Xác định loại ngày (Cuối tuần / Ngày thường)
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            workday.setWorkType(DayType.WEEKEND);
        } else {
            workday.setWorkType(DayType.NORMAL);
        }

        // 3. Lấy danh sách Ca làm việc (Assignments)
        List<ShiftAssignment> assignments = assignmentRepo.findAllByEmployee_EmployeeIdAndAssignmentDate(employeeId, date);

        // Tính tổng thời gian chuẩn (Expected) của TẤT CẢ các ca trong ngày
        double totalStandardMinutes = 0.0;
        if (!assignments.isEmpty()) {
            // Lấy shift đầu tiên để map vào entity (nếu Workday chỉ lưu 1 shift)
            workday.setShift(assignments.get(0).getShift());

            for (ShiftAssignment sa : assignments) {
                if (sa.getShift().getExpectedWorkMinutes() != null) {
                    totalStandardMinutes += sa.getShift().getExpectedWorkMinutes();
                } else {
                    totalStandardMinutes += 480.0; // Mặc định 8 tiếng nếu null
                }
            }
        } else {
            workday.setShift(null);
            totalStandardMinutes = 480.0; // Không có ca thì chuẩn vẫn là 8h (hoặc 0 tùy policy)
        }

        // 4. [FIX LỖI CHÍNH] Lấy danh sách Record chấm công (List thay vì Optional)
        List<AttendanceRecord> records = attendanceRepo.findByEmployee_EmployeeIdAndAttendanceDate(employeeId, date);

        // Cộng dồn giờ làm thực tế từ các Record (Sáng + Chiều + ...)
        double actualTotalMinutes = 0.0;
        boolean isLateAnyShift = false;
        boolean hasCheckIn = false;

        for (AttendanceRecord record : records) {
            if (record.getTotalWorkMinutes() != null) {
                actualTotalMinutes += record.getTotalWorkMinutes();
            }
            // Logic status tổng hợp: Nếu có bất kỳ ca nào đi muộn -> đánh dấu có vết Late
            if (record.getStatus() == AttendanceStatus.LATE) {
                isLateAnyShift = true;
            }
            if (record.getCheckInTime() != null) {
                hasCheckIn = true;
            }
        }

        // 5. Xác định Status cơ bản dựa trên chấm công
        AttendanceStatus finalStatus = AttendanceStatus.ABSENT;
        if (hasCheckIn) {
            if (isLateAnyShift) {
                finalStatus = AttendanceStatus.LATE;
            } else {
                finalStatus = AttendanceStatus.PRESENT;
            }
        }

        // 6. Xử lý Nghỉ phép (Leave)
        List<LeaveRequest> leaves = leaveRepo.findApprovedLeaveInMonth(employeeId, date, date);
        LeaveRequest approvedLeave = leaves.isEmpty() ? null : leaves.get(0);

        if (approvedLeave != null) {
            workday.setLeaveRequest(approvedLeave);
            String type = approvedLeave.getLeaveType() != null ? approvedLeave.getLeaveType().toLowerCase() : "";

            if (type.contains("không lương") || type.contains("unpaid")) {
                finalStatus = AttendanceStatus.LEAVE_UNPAID;
            } else {
                finalStatus = AttendanceStatus.LEAVE_PAID;
            }
        } else {
            workday.setLeaveRequest(null);
        }

        // 7. Tính toán Giờ công (Regular) và Giờ thừa (Excess)
        // Regular = Max là StandardMinutes. Nếu làm hơn thì là Excess.
        double regularMinutes = Math.min(actualTotalMinutes, totalStandardMinutes);
        workday.setHoursWorked(regularMinutes / 60.0);

        double excessMinutes = Math.max(0, actualTotalMinutes - totalStandardMinutes);

        // 8. Xử lý OT (Overtime)
        // Cộng tổng tất cả các đơn OT trong ngày (phòng trường hợp OT trưa + OT tối)
        List<OvertimeRequest> ots = overtimeRepo.findByEmployee_EmployeeIdAndStatusAndDateBetween(
                employeeId, RequestStatus.APPROVED, date, date);

        double totalApprovedOtHours = 0.0;
        OvertimeRequest primaryOtRequest = null; // Lưu 1 cái đại diện vào DB

        for (OvertimeRequest ot : ots) {
            if (ot.getHours() != null) {
                totalApprovedOtHours += ot.getHours().doubleValue();
            }
            if (primaryOtRequest == null) primaryOtRequest = ot;
        }

        workday.setOvertimeRequest(primaryOtRequest); // Map đại diện 1 đơn

        double totalApprovedOtMinutes = totalApprovedOtHours * 60.0;

        // OT thực tế = Min(Số phút làm dư ra, Số phút đã duyệt)
        // Nếu không làm dư ra (excess = 0) thì dù có đơn duyệt cũng không tính tiền OT
        double finalOtMinutes = Math.min(totalApprovedOtMinutes, excessMinutes);

        workday.setHoursOvertime(finalOtMinutes / 60.0);
        workday.setAttendanceStatus(finalStatus);

        workdayRepository.save(workday);
    }

    @Override
    public void processAllDailyWorkday(LocalDate date) {
        List<Employee> employees = employeeRepository.findAll();
        for (Employee emp : employees) {
            processDailyWorkday(emp.getEmployeeId(), date);
        }
    }
}