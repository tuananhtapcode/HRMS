package com.project.hrms.controller;

import com.project.hrms.dto.MonthlyTimesheetDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.model.Account;
import com.project.hrms.model.Employee;
import com.project.hrms.model.MonthlyTimesheet;
import com.project.hrms.repository.AccountRepository;
import com.project.hrms.repository.MonthlyTimesheetRepository;
import com.project.hrms.response.ApiResponse; // Import chuẩn ApiResponse của bạn
import com.project.hrms.service.MonthlyTimesheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/timesheets")
@RequiredArgsConstructor
public class MonthlyTimesheetController {

    private final MonthlyTimesheetService monthlyService;
    private final MonthlyTimesheetRepository monthlyRepo;
    private final AccountRepository accountRepo;

    // ========================================================================
    // DÀNH CHO ADMIN / HR (QUẢN LÝ)
    // ========================================================================

    // 1. Kích hoạt tính toán công (HR chạy cuối tháng)
    @PostMapping("/calculate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<String>> calculateMonthlyTimesheet(
            @RequestParam int month,
            @RequestParam int year) {

        monthlyService.generateMonthlyTimesheetForAll(month, year);
        return ResponseEntity.ok(ApiResponse.success("Đã tổng hợp công tháng " + month + "/" + year + " cho toàn bộ nhân viên!", null));
    }

    // 2. Xem báo cáo quản trị (Có lọc theo Phòng ban / Tên)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<MonthlyTimesheetDTO>>> getMonthlyReport(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String search
    ) {
        List<MonthlyTimesheet> entities;

        if (departmentId != null) {
            entities = monthlyRepo.findByMonthAndYearAndEmployee_Department_DepartmentId(month, year, departmentId);
        } else if (search != null && !search.isEmpty()) {
            entities = monthlyRepo.findByMonthAndYearAndEmployee_FullNameContainingIgnoreCase(month, year, search);
        } else {
            entities = monthlyRepo.findByMonthAndYear(month, year);
        }

        // Convert sang DTO để response gọn đẹp
        List<MonthlyTimesheetDTO> dtos = entities.stream()
                .map(MonthlyTimesheetDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu bảng công thành công", dtos));
    }

    // ========================================================================
    // DÀNH CHO NHÂN VIÊN (INDIVIDUAL)
    // ========================================================================

    // 3. Nhân viên xem bảng công của chính mình
    @GetMapping("/my-timesheet")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'USER')")
    public ResponseEntity<ApiResponse<MonthlyTimesheetDTO>> getMyTimesheet(
            @RequestParam int month,
            @RequestParam int year) {

        // 1. Lấy Username từ Token (An toàn tuyệt đối)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // 2. Tìm Employee ID từ Username
        Account account = accountRepo.findByUsername(username)
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy tài khoản: " + username));

        Employee employee = account.getEmployee();
        if (employee == null) {
            throw new DataNotFoundException("Tài khoản này chưa liên kết với nhân viên nào");
        }

        // 3. Tìm bảng công
        List<MonthlyTimesheet> results = monthlyRepo.findByEmployee_EmployeeIdAndMonthAndYear(
                employee.getEmployeeId(), month, year
        );

        if (results.isEmpty()) {
            throw new DataNotFoundException("Chưa có bảng công tháng " + month + "/" + year);
        }

        // 4. Convert sang DTO
        MonthlyTimesheetDTO dto = MonthlyTimesheetDTO.fromEntity(results.get(0));

        return ResponseEntity.ok(ApiResponse.success("Lấy bảng công cá nhân thành công", dto));
    }
}