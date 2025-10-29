// src/main/java/com/project/hrms/service/ShiftAssignmentService.java
package com.project.hrms.service;

import com.project.hrms.dto.ShiftAssignmentDTO;
import com.project.hrms.model.Employee;
import com.project.hrms.model.Shift;
import com.project.hrms.model.ShiftAssignment;
import com.project.hrms.repository.EmployeeRepository;
import com.project.hrms.repository.ShiftAssignmentRepository;
import com.project.hrms.repository.ShiftRepository;
import com.project.hrms.exception.DataNotFoundException;
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
}