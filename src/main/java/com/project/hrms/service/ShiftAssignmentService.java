//// src/main/java/com/project/hrms/service/ShiftAssignmentService.java
//package com.project.hrms.service;
//
//import com.project.hrms.dto.BulkAssignDTO;
//import com.project.hrms.dto.ShiftAssignmentDTO;
//import com.project.hrms.dto.ShiftRegisterDTO;
//import com.project.hrms.exception.DataNotFoundException;
//import com.project.hrms.exception.InvalidParamException;
//import com.project.hrms.model.Account;
//import com.project.hrms.model.Employee;
//import com.project.hrms.model.Shift;
//import com.project.hrms.model.ShiftAssignment;
//import com.project.hrms.repository.AccountRepository;
//import com.project.hrms.repository.EmployeeRepository;
//import com.project.hrms.repository.ShiftAssignmentRepository;
//import com.project.hrms.repository.ShiftRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class ShiftAssignmentService {
//
//    private final ShiftAssignmentRepository assignmentRepository;
//    private final EmployeeRepository employeeRepository;
//    private final ShiftRepository shiftRepository;
//    private final AccountRepository accountRepository;
//
//    // Đã bỏ ModelMapper để tránh lỗi Bean
//    // private final ModelMapper modelMapper;
//
//    /**
//     * API chính: Phân ca cho nhân viên
//     * (Tạo mới nếu chưa có, hoặc cập nhật ca ĐẦU TIÊN tìm thấy nếu đã tồn tại)
//     */
//    public ShiftAssignmentDTO assignShift(ShiftAssignmentDTO dto) {
//        // 1. Kiểm tra Employee và Shift
//        Employee employee = employeeRepository.findById(dto.getEmployeeId())
//                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy nhân viên"));
//        Shift shift = shiftRepository.findById(dto.getShiftId())
//                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy ca làm việc"));
//
//        // 2. Lấy danh sách các ca ĐÃ CÓ trong ngày
//        List<ShiftAssignment> existingAssignments = assignmentRepository
//                .findAllByEmployee_EmployeeIdAndAssignmentDate(dto.getEmployeeId(), dto.getAssignmentDate());
//
//        // 3. LOGIC QUAN TRỌNG: Tìm xem ca này (ShiftId này) đã có chưa?
//        // Nếu muốn hỗ trợ 2 ca (Sáng, Chiều), ta phải check trùng ShiftId chứ không chỉ check trùng ngày.
//
//        ShiftAssignment assignment = existingAssignments.stream()
//                .filter(a -> a.getShift().getShiftId().equals(dto.getShiftId()))
//                .findFirst()
//                .orElse(new ShiftAssignment()); // Nếu chưa có ca này -> Tạo mới (ADD)
//
//        // 4. Set thông tin
//        assignment.setEmployee(employee);
//        assignment.setShift(shift);
//        assignment.setAssignmentDate(dto.getAssignmentDate());
//        assignment.setIsApproved(dto.getIsApproved());
//        assignment.setNote(dto.getNote());
//
//        // 5. Validate Trùng giờ (Tùy chọn: Nếu bạn muốn chặn ca 8-12h trùng với ca 9-13h)
//        // checkOverlapping(assignment, existingAssignments);
//
//        // 6. Lưu
//        ShiftAssignment savedAssignment = assignmentRepository.save(assignment);
//
//        return mapToExtendedDTO(savedAssignment);
//    }
//    /**
//     * API Lấy lịch làm việc của 1 nhân viên
//     */
//    public List<ShiftAssignmentDTO> getAssignmentsByEmployee(Long employeeId, LocalDate startDate, LocalDate endDate) {
//        return assignmentRepository.findByEmployee_EmployeeIdAndAssignmentDateBetween(employeeId, startDate, endDate)
//                .stream()
//                .map(this::mapToExtendedDTO) // Dùng Map thủ công
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * API Xóa một phân ca
//     */
//    public void deleteAssignment(Long assignmentId) {
//        if (!assignmentRepository.existsById(assignmentId)) {
//            throw new DataNotFoundException("Không tìm thấy phân ca này");
//        }
//        assignmentRepository.deleteById(assignmentId);
//    }
//
//    @Transactional
//    public void bulkAssignByDepartment(BulkAssignDTO dto) {
//        Shift shift = shiftRepository.findById(dto.getShiftId())
//                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy ca làm việc"));
//
//        List<Employee> employeesInDept = employeeRepository
//                .findByDepartment_DepartmentId(dto.getDepartmentId());
//
//        if (employeesInDept.isEmpty()) {
//            throw new DataNotFoundException("Không có nhân viên nào trong phòng ban này.");
//        }
//
//        for (Employee emp : employeesInDept) {
//            for (LocalDate date = dto.getStartDate(); !date.isAfter(dto.getEndDate()); date = date.plusDays(1)) {
//
//                // Lấy các ca đã có
//                List<ShiftAssignment> existingAssignments = assignmentRepository
//                        .findAllByEmployee_EmployeeIdAndAssignmentDate(emp.getEmployeeId(), date);
//
//                // Kiểm tra xem nhân viên đã có ca này chưa
//                boolean alreadyHasThisShift = existingAssignments.stream()
//                        .anyMatch(a -> a.getShift().getShiftId().equals(shift.getShiftId()));
//
//                // Nếu chưa có thì mới thêm (Tránh duplicate 2 dòng y hệt nhau)
//                if (!alreadyHasThisShift) {
//                    ShiftAssignment assignment = new ShiftAssignment();
//                    assignment.setEmployee(emp);
//                    assignment.setShift(shift);
//                    assignment.setAssignmentDate(date);
//                    assignment.setIsApproved(true);
//                    assignment.setNote(dto.getNote());
//
//                    assignmentRepository.save(assignment);
//                }
//            }
//        }
//    }
//
//    /**
//     * Nhân viên tự đăng ký ca.
//     */
//    @Transactional
//    public ShiftAssignmentDTO employeeRegisterShift(ShiftRegisterDTO dto, String username) {
//
//        // 1. Tìm Employee
//        Account account = accountRepository.findByUsername(username)
//                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy tài khoản"));
//
//        Employee employee = account.getEmployee();
//        if (employee == null) {
//            throw new InvalidParamException("Tài khoản chưa liên kết với hồ sơ nhân viên.");
//        }
//
//        // 2. Kiểm tra Ca
//        Shift shift = shiftRepository.findById(dto.getShiftId())
//                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy ca làm việc này."));
//
//        // 3. SỬA LỖI Ở ĐÂY: Xử lý List
//        List<ShiftAssignment> existingAssignments = assignmentRepository
//                .findAllByEmployee_EmployeeIdAndAssignmentDate(employee.getEmployeeId(), dto.getDate());
//
//        ShiftAssignment assignment;
//        if (existingAssignments.isEmpty()) {
//            assignment = new ShiftAssignment();
//        } else {
//            assignment = existingAssignments.get(0);
//        }
//
//        // Kiểm tra nếu ca đã được *duyệt* bởi admin
//        if (assignment.getShiftAssignmentId() != null && Boolean.TRUE.equals(assignment.getIsApproved())) {
//            throw new InvalidParamException("Bạn không thể tự ý thay đổi ca đã được Quản lý phê duyệt.");
//        }
//
//        // 4. Gán thông tin
//        assignment.setEmployee(employee);
//        assignment.setShift(shift);
//        assignment.setAssignmentDate(dto.getDate());
//        assignment.setIsApproved(false); // Chờ duyệt
//        assignment.setNote(dto.getNote());
//
//        ShiftAssignment savedAssignment = assignmentRepository.save(assignment);
//
//        return mapToExtendedDTO(savedAssignment);
//    }
//
//    // --- CÁC HÀM HỖ TRỢ HIỂN THỊ BẢNG LỊCH (API 2.2) ---
//
//    // Hàm lấy lịch phòng ban
//    public List<ShiftAssignmentDTO> getDepartmentSchedule(Long deptId, LocalDate startDate, LocalDate endDate) {
//        List<ShiftAssignment> entities = assignmentRepository.findByDepartmentAndDateBetween(deptId, startDate, endDate);
//
//        return entities.stream()
//                .map(this::mapToExtendedDTO)
//                .collect(Collectors.toList());
//    }
//
//    // Hàm Map thủ công (Thay thế ModelMapper hoàn toàn)
//    private ShiftAssignmentDTO mapToExtendedDTO(ShiftAssignment entity) {
//        if (entity == null) return null;
//
//        Employee emp = entity.getEmployee();
//        Shift shift = entity.getShift();
//
//        String jobPosName = (emp.getJobPosition() != null) ? emp.getJobPosition().getName() : "";
//        String deptName = (emp.getDepartment() != null) ? emp.getDepartment().getName() : "";
//
//        return ShiftAssignmentDTO.builder()
//                .shiftAssignmentId(entity.getShiftAssignmentId())
//                .assignmentDate(entity.getAssignmentDate())
//                .isApproved(entity.getIsApproved())
//                .note(entity.getNote())
//
//                // Map thông tin Nhân viên mở rộng
//                .employeeId(emp.getEmployeeId())
//                .employeeCode(emp.getEmployeeCode())
//                .employeeName(emp.getFullName())
//                .jobPosition(jobPosName)
//                .departmentName(deptName)
//
//                // Map thông tin Ca
//                .shiftId(shift.getShiftId())
//                .shiftName(shift.getName())
//                .shiftCode(shift.getCode())
//                .startTime(shift.getStartTime())
//                .endTime(shift.getEndTime())
//                .build();
//    }
//}

// src/main/java/com/project/hrms/service/ShiftAssignmentService.java
package com.project.hrms.service;

import com.project.hrms.dto.BulkAssignDTO;
import com.project.hrms.dto.ShiftAssignmentDTO;
import com.project.hrms.dto.ShiftRegisterDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.Account;
import com.project.hrms.model.Employee;
import com.project.hrms.model.Shift;
import com.project.hrms.model.ShiftAssignment;
import com.project.hrms.repository.AccountRepository;
import com.project.hrms.repository.EmployeeRepository;
import com.project.hrms.repository.ShiftAssignmentRepository;
import com.project.hrms.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShiftAssignmentService {

    private final ShiftAssignmentRepository assignmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;
    private final AccountRepository accountRepository;

    // Đã bỏ ModelMapper để tránh lỗi Bean
    // private final ModelMapper modelMapper;

    /**
     * API chính: Phân ca cho nhân viên
     * (Tạo mới nếu chưa có, hoặc cập nhật ca ĐẦU TIÊN tìm thấy nếu đã tồn tại)
     */
    public ShiftAssignmentDTO assignShift(ShiftAssignmentDTO dto) {
        validateAssignmentDate(dto.getAssignmentDate());
        // 1. Kiểm tra Employee và Shift
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy nhân viên"));
        Shift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy ca làm việc"));

        // 2. Lấy danh sách các ca ĐÃ CÓ trong ngày
        List<ShiftAssignment> existingAssignments = assignmentRepository
                .findAllByEmployee_EmployeeIdAndAssignmentDate(dto.getEmployeeId(), dto.getAssignmentDate());

        // 3. LOGIC QUAN TRỌNG: Tìm xem ca này (ShiftId này) đã có chưa?
        // Nếu muốn hỗ trợ 2 ca (Sáng, Chiều), ta phải check trùng ShiftId chứ không chỉ check trùng ngày.

        ShiftAssignment assignment = existingAssignments.stream()
                .filter(a -> a.getShift().getShiftId().equals(dto.getShiftId()))
                .findFirst()
                .orElse(new ShiftAssignment()); // Nếu chưa có ca này -> Tạo mới (ADD)

        // 4. Set thông tin
        assignment.setEmployee(employee);
        assignment.setShift(shift);
        assignment.setAssignmentDate(dto.getAssignmentDate());
        assignment.setIsApproved(dto.getIsApproved());
        assignment.setNote(dto.getNote());

        // 5. Validate Trùng giờ (Tùy chọn: Nếu bạn muốn chặn ca 8-12h trùng với ca 9-13h)
        // checkOverlapping(assignment, existingAssignments);

        // 6. Lưu
        ShiftAssignment savedAssignment = assignmentRepository.save(assignment);

        return mapToExtendedDTO(savedAssignment);
    }
    /**
     * API Lấy lịch làm việc của 1 nhân viên
     */
    public List<ShiftAssignmentDTO> getAssignmentsByEmployee(Long employeeId, LocalDate startDate, LocalDate endDate) {
        return assignmentRepository.findByEmployee_EmployeeIdAndAssignmentDateBetween(employeeId, startDate, endDate)
                .stream()
                .map(this::mapToExtendedDTO) // Dùng Map thủ công
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

    @Transactional
    public void bulkAssignByDepartment(BulkAssignDTO dto) {

        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new InvalidParamException("Khoảng ngày không hợp lệ.");
        }
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new InvalidParamException("Ngày kết thúc phải >= ngày bắt đầu.");
        }

        Shift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy ca làm việc"));

        List<Employee> employeesInDept = employeeRepository
                .findByDepartment_DepartmentId(dto.getDepartmentId());

        if (employeesInDept.isEmpty()) {
            throw new DataNotFoundException("Không có nhân viên nào trong phòng ban này.");
        }

        LocalDate today = LocalDate.now();

        for (Employee emp : employeesInDept) {
            for (LocalDate date = dto.getStartDate(); !date.isAfter(dto.getEndDate()); date = date.plusDays(1)) {

                // Không phân ca vào ngày đã qua
                if (date.isBefore(today)) {
                    continue;
                }

                List<ShiftAssignment> existingAssignments = assignmentRepository
                        .findAllByEmployee_EmployeeIdAndAssignmentDate(emp.getEmployeeId(), date);

                boolean alreadyHasThisShift = existingAssignments.stream()
                        .anyMatch(a -> a.getShift() != null && a.getShift().getShiftId().equals(shift.getShiftId()));

                if (!alreadyHasThisShift) {
                    ShiftAssignment assignment = new ShiftAssignment();
                    assignment.setEmployee(emp);
                    assignment.setShift(shift);
                    assignment.setAssignmentDate(date);
                    assignment.setIsApproved(true);
                    assignment.setNote(dto.getNote());

                    assignmentRepository.save(assignment);
                }
            }
        }
    }


    /**
     * Nhân viên tự đăng ký ca.
     */
    @Transactional
    public ShiftAssignmentDTO employeeRegisterShift(ShiftRegisterDTO dto, String username) {
        validateAssignmentDate(dto.getDate());
        // 1. Tìm Employee
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy tài khoản"));

        Employee employee = account.getEmployee();
        if (employee == null) {
            throw new InvalidParamException("Tài khoản chưa liên kết với hồ sơ nhân viên.");
        }

        // 2. Kiểm tra Ca
        Shift shift = shiftRepository.findById(dto.getShiftId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy ca làm việc này."));

        // 3. SỬA LỖI Ở ĐÂY: Xử lý List
        List<ShiftAssignment> existingAssignments = assignmentRepository
                .findAllByEmployee_EmployeeIdAndAssignmentDate(employee.getEmployeeId(), dto.getDate());

        ShiftAssignment assignment = existingAssignments.stream()
                .filter(a -> a.getShift() != null && a.getShift().getShiftId().equals(dto.getShiftId()))
                .findFirst()
                .orElse(new ShiftAssignment());


        // Kiểm tra nếu ca đã được *duyệt* bởi admin
        if (assignment.getShiftAssignmentId() != null && Boolean.TRUE.equals(assignment.getIsApproved())) {
            throw new InvalidParamException("Bạn không thể tự ý thay đổi ca đã được Quản lý phê duyệt.");
        }

        // 4. Gán thông tin
        assignment.setEmployee(employee);
        assignment.setShift(shift);
        assignment.setAssignmentDate(dto.getDate());
        assignment.setIsApproved(false); // Chờ duyệt
        assignment.setNote(dto.getNote());

        ShiftAssignment savedAssignment = assignmentRepository.save(assignment);

        return mapToExtendedDTO(savedAssignment);
    }

    // --- CÁC HÀM HỖ TRỢ HIỂN THỊ BẢNG LỊCH (API 2.2) ---

    // Hàm lấy lịch phòng ban
    public List<ShiftAssignmentDTO> getDepartmentSchedule(Long deptId, LocalDate startDate, LocalDate endDate) {
        List<ShiftAssignment> entities = assignmentRepository.findByDepartmentAndDateBetween(deptId, startDate, endDate);

        return entities.stream()
                .map(this::mapToExtendedDTO)
                .collect(Collectors.toList());
    }

    // Hàm Map thủ công (Thay thế ModelMapper hoàn toàn)
    private ShiftAssignmentDTO mapToExtendedDTO(ShiftAssignment entity) {
        if (entity == null) return null;

        Employee emp = entity.getEmployee();
        Shift shift = entity.getShift();

        String jobPosName = (emp.getJobPosition() != null) ? emp.getJobPosition().getName() : "";
        String deptName = (emp.getDepartment() != null) ? emp.getDepartment().getName() : "";

        return ShiftAssignmentDTO.builder()
                .shiftAssignmentId(entity.getShiftAssignmentId())
                .assignmentDate(entity.getAssignmentDate())
                .isApproved(entity.getIsApproved())
                .note(entity.getNote())

                // Map thông tin Nhân viên mở rộng
                .employeeId(emp.getEmployeeId())
                .employeeCode(emp.getEmployeeCode())
                .employeeName(emp.getFullName())
                .jobPosition(jobPosName)
                .departmentName(deptName)

                // Map thông tin Ca
                .shiftId(shift.getShiftId())
                .shiftName(shift.getName())
                .shiftCode(shift.getCode())
                .startTime(shift.getStartTime())
                .endTime(shift.getEndTime())
                .build();
    }
    private void validateAssignmentDate(LocalDate date) {
        if (date == null) {
            throw new InvalidParamException("Ngày phân ca không hợp lệ.");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new InvalidParamException("Không thể phân ca vào ngày đã qua.");
        }
    }

    // --- THÊM ĐOẠN NÀY VÀO SERVICE ---

    public List<ShiftAssignmentDTO> getAllAssignments(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new InvalidParamException("Ngày bắt đầu không được sau ngày kết thúc");
        }

        // Gọi Repository tìm theo khoảng ngày
        List<ShiftAssignment> assignments = assignmentRepository.findByAssignmentDateBetween(startDate, endDate);

        // Map sang DTO
        return assignments.stream()
                .map(this::mapToExtendedDTO)
                .collect(Collectors.toList());
    }



}