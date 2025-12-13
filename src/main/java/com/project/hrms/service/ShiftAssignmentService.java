// src/main/java/com/project/hrms/service/ShiftAssignmentService.java
package com.project.hrms.service;

import com.project.hrms.dto.BulkAssignDTO;
import com.project.hrms.dto.ShiftAssignmentDTO;
import com.project.hrms.dto.ShiftRegisterDTO;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.Account;
import com.project.hrms.model.Employee;
import com.project.hrms.model.Shift;
import com.project.hrms.model.ShiftAssignment;
import com.project.hrms.repository.AccountRepository;
import com.project.hrms.repository.EmployeeRepository;
import com.project.hrms.repository.ShiftAssignmentRepository;
import com.project.hrms.repository.ShiftRepository;
import com.project.hrms.exception.DataNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftAssignmentService {

    private final ShiftAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;

    /**
     * API chính: Phân ca cho nhân viên
     * (Tạo mới nếu chưa có, hoặc cập nhật ca nếu đã tồn tại)
     */
    public ShiftAssignmentDTO assignShift(ShiftAssignmentDTO dto) {
        // 1. Kiểm tra Employee và Shift có tồn tại không
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy nhân viên"));
        Shift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy ca làm việc"));

        // 2. Kiểm tra xem đã có phân ca cho ngày này chưa
        ShiftAssignment assignment = assignmentRepository
                .findByEmployee_EmployeeIdAndAssignmentDate(dto.getEmployeeId(), dto.getAssignmentDate())
                .orElse(new ShiftAssignment()); // Nếu chưa có, tạo mới

        // 3. Map dữ liệu (Cập nhật hoặc Gán mới)
        assignment.setEmployee(employee);
        assignment.setShift(shift);
        assignment.setAssignmentDate(dto.getAssignmentDate());
        assignment.setIsApproved(dto.getIsApproved());
        assignment.setNote(dto.getNote());

        // 4. Lưu (UPSERT)
        ShiftAssignment savedAssignment = assignmentRepository.save(assignment);
        return modelMapper.map(savedAssignment, ShiftAssignmentDTO.class);
    }

    /**
     * API Lấy lịch làm việc của 1 nhân viên (ví dụ: trong 1 tháng)
     */
    public List<ShiftAssignmentDTO> getAssignmentsByEmployee(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return assignmentRepository.findByEmployee_EmployeeIdAndAssignmentDateBetween(employeeId, startDate, endDate)
                .stream()
                .map(asg -> modelMapper.map(asg, ShiftAssignmentDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * API Xóa một phân ca
     */
    public void deleteAssignment(Long assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            throw new DataNotFoundException("Không tìm thấy phân ca này");
        }
        assignmentRepository.deleteById(assignmentId);
    }
    @Transactional // Rất quan trọng: đảm bảo tất cả thành công hoặc thất bại
    public void bulkAssignByDepartment(BulkAssignDTO dto) {

        // 1. Kiểm tra Shift có tồn tại không
        Shift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy ca làm việc"));

        // 2. Lấy danh sách TẤT CẢ nhân viên trong phòng ban đó
        List<Employee> employeesInDept = employeeRepository
                .findByDepartment_DepartmentId(dto.getDepartmentId());

        if (employeesInDept.isEmpty()) {
            throw new DataNotFoundException("Không có nhân viên nào trong phòng ban này.");
        }

        // 3. Lặp qua từng nhân viên VÀ từng ngày để phân ca
        for (Employee emp : employeesInDept) {
            // Lặp từ startDate đến endDate
            for (LocalDate date = dto.getStartDate(); !date.isAfter(dto.getEndDate()); date = date.plusDays(1)) {

                // Logic UPSERT (giống hàm assignShift)
                ShiftAssignment assignment = assignmentRepository
                        .findByEmployee_EmployeeIdAndAssignmentDate(emp.getEmployeeId(), date)
                        .orElse(new ShiftAssignment()); // Nếu chưa có, tạo mới

                assignment.setEmployee(emp);
                assignment.setShift(shift);
                assignment.setAssignmentDate(date);
                assignment.setIsApproved(true); // Gán hàng loạt thường là duyệt luôn
                assignment.setNote(dto.getNote());

                // Lưu (Đây là lý do cần @Transactional,
                // nếu 1 cái fail, tất cả sẽ rollback)
                assignmentRepository.save(assignment);
            }
        }
    }
    // --- HÀM MỚI CHO NHÂN VIÊN TỰ ĐĂNG KÝ ---

    /**
     * Nhân viên tự đăng ký ca.
     * @param dto Thông tin ca đăng ký
     * @param username Tên đăng nhập (lấy từ token) để tìm Employee
     * @return
     */
    @Transactional
    public ShiftAssignmentDTO employeeRegisterShift(ShiftRegisterDTO dto, String username) {

        // 1. Tìm Employee từ username đang đăng nhập
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy tài khoản"));

        Employee employee = account.getEmployee();
        if (employee == null) {
            throw new InvalidParamException("Tài khoản của bạn chưa được liên kết với hồ sơ nhân viên.");
        }

        // 2. Kiểm tra Ca làm việc
        Shift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy ca làm việc này."));

        // 3. Kiểm tra xem ngày này đã có ca chưa (logic UPSERT)
        // (Do có UNIQUE key, chúng ta có thể dùng lại logic cũ)
        ShiftAssignment assignment = assignmentRepository
                .findByEmployee_EmployeeIdAndAssignmentDate(employee.getEmployeeId(), dto.getDate())
                .orElse(new ShiftAssignment()); // Nếu chưa có, tạo mới

        // Kiểm tra nếu ca đã được *duyệt* bởi admin, không cho nhân viên tự ý sửa
        if (assignment.getShiftAssignmentId() != null && assignment.getIsApproved()) {
            throw new InvalidParamException("Bạn không thể tự ý thay đổi ca đã được Quản lý phê duyệt.");
        }

        // 4. Gán thông tin và ĐẶT TRẠNG THÁI CHỜ DUYỆT
        assignment.setEmployee(employee);
        assignment.setShift(shift);
        assignment.setAssignmentDate(dto.getDate());
        assignment.setIsApproved(false); // QUAN TRỌNG: Chờ quản lý duyệt
        assignment.setNote(dto.getNote());

        ShiftAssignment savedAssignment = assignmentRepository.save(assignment);

        // (Trong thực tế, bạn có thể gửi thông báo cho quản lý ở đây)

        return modelMapper.map(savedAssignment, ShiftAssignmentDTO.class);
    }
}