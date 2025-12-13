package com.project.hrms.service;

import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.model.*;
import com.project.hrms.model.enums.AttendanceStatus;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 1. Tìm hoặc tạo mới bản ghi Workday
        Workday workday = workdayRepository.findByEmployeeAndDate(employee, date)
                .orElse(new Workday());
        workday.setEmployee(employee);
        workday.setDate(date);

        // 2. Lấy dữ liệu thô
        Optional<ShiftAssignment> assignment = assignmentRepo.findByEmployee_EmployeeIdAndAssignmentDate(employeeId, date);
        Optional<AttendanceRecord> attendance = attendanceRepo.findByEmployee_EmployeeIdAndAttendanceDate(employeeId, date);

        // Tìm đơn nghỉ đã duyệt cho ngày này (Logic tìm ngày nằm trong khoảng start/end)
        List<LeaveRequest> leaves = leaveRepo.findApprovedLeaveInMonth(employeeId, date, date);

        // Tìm đơn OT đã duyệt cho ngày này
        List<OvertimeRequest> ots = overtimeRepo.findByEmployee_EmployeeIdAndStatusAndDateBetween(
                employeeId, RequestStatus.Approved, date, date);

        // 3. Logic Tổng hợp (Core Logic)
        double standardDays = 0.0;
        double paidLeaveDays = 0.0;
        double unauthorizedLeaveDays = 0.0;
        double overtimeHours = 0.0;
        int lateMinutes = 0;
        AttendanceStatus finalStatus = AttendanceStatus.Absent;
        Shift shift = null;

        if (assignment.isPresent()) {
            shift = assignment.get().getShift();
            workday.setShift(shift);

            // Mặc định là vắng nếu có lịch mà chưa có dữ liệu chấm công/nghỉ
            finalStatus = AttendanceStatus.Absent;
            unauthorizedLeaveDays = 1.0;
        }

        // A. Xử lý Chấm công (Attendance)
        if (attendance.isPresent()) {
            AttendanceRecord record = attendance.get();
            lateMinutes = record.getLateMinutes();

            if (record.getStatus() == AttendanceStatus.Present || record.getStatus() == AttendanceStatus.Late) {
                standardDays = 1.0; // Đi làm đủ công
                unauthorizedLeaveDays = 0.0; // Xóa vắng
                finalStatus = record.getStatus(); // Present hoặc Late
            }
        }

        // B. Xử lý Nghỉ phép (Leave) - Ưu tiên đè lên Vắng
        if (!leaves.isEmpty()) {
            // Giả sử có đơn nghỉ là tính nghỉ có phép (bạn có thể check loại nghỉ để tách Paid/Unpaid)
            paidLeaveDays = 1.0;
            unauthorizedLeaveDays = 0.0;
            finalStatus = AttendanceStatus.OnLeave;

            // Nếu vừa đi làm vừa xin nghỉ (nửa buổi), cần logic phức tạp hơn.
            // Ở đây MVP: Nếu có đơn nghỉ -> Ưu tiên tính là status OnLeave, nhưng vẫn giữ công đi làm nếu có.
            if (standardDays > 0) {
                // Case đặc biệt: Đi làm nhưng vẫn có đơn nghỉ (VD nghỉ chiều) -> Cộng dồn
                // (Logic này tùy công ty, ở đây ta giữ nguyên)
            }
        }

        // C. Xử lý OT
        if (!ots.isEmpty()) {
            overtimeHours = ots.stream()
                    .mapToDouble(ot -> ot.getHours().doubleValue())
                    .sum();
        }

        // 4. Lưu kết quả cuối cùng
        workday.setStandardWorkDays(standardDays);
        workday.setPaidLeaveDays(paidLeaveDays);
        workday.setUnauthorizedLeaveDays(unauthorizedLeaveDays);
        workday.setOvertimeHours(overtimeHours);
        workday.setLateMinutes(lateMinutes);
        workday.setFinalStatus(finalStatus);

        workdayRepository.save(workday);
    }

    @Override
    @Transactional
    public void processAllDailyWorkday(LocalDate date) {
        List<Employee> employees = employeeRepository.findAll();
        for (Employee emp : employees) {
            processDailyWorkday(emp.getEmployeeId(), date);
        }
    }
}