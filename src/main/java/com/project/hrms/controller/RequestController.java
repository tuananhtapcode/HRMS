package com.project.hrms.controller;

import com.project.hrms.dto.ApproveRequestDTO;
import com.project.hrms.dto.LeaveRequestDTO;
import com.project.hrms.dto.OvertimeRequestDTO;
import com.project.hrms.model.enums.RequestStatus;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.service.ILeaveService;
import com.project.hrms.service.IOvertimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class RequestController {

    private final ILeaveService leaveService;
    private final IOvertimeService overtimeService;

    // ==========================================
    // 1. NHÓM API: ĐƠN XIN NGHỈ (LEAVE)
    // ==========================================

    @PostMapping("/leave")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> createLeaveRequest(
            @Valid @RequestBody LeaveRequestDTO dto,
            BindingResult bindingResult,
            Authentication authentication) {

        // Xử lý lỗi Validation
        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, errorMessage));
        }

        // Gọi Service
        LeaveRequestDTO result = leaveService.createLeaveRequest(dto, authentication.getName());

        // Trả về Response chuẩn
        return ResponseEntity.ok(ApiResponse.success("Tạo đơn xin nghỉ thành công", result));
    }

    @GetMapping("/my-leaves")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<LeaveRequestDTO>>> getMyLeaveRequests(Authentication authentication) {

        List<LeaveRequestDTO> list = leaveService.getMyLeaveRequests(authentication.getName());

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn nghỉ thành công", list));
    }

    @PostMapping("/leave/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<LeaveRequestDTO>> approveLeaveRequest(
            @PathVariable Long id,
            @RequestBody ApproveRequestDTO dto,
            Authentication authentication) {

        LeaveRequestDTO result = leaveService.approveLeaveRequest(id, dto, authentication.getName());

        return ResponseEntity.ok(ApiResponse.success("Phê duyệt đơn nghỉ thành công", result));
    }
    // 1. (BỔ SUNG) ADMIN XEM DANH SÁCH ĐƠN NGHỈ
    // ==========================================
    @GetMapping("/leaves")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<LeaveRequestDTO>>> getAllLeaveRequests(
            @RequestParam(required = false) RequestStatus status) {

        // Gọi service
        List<LeaveRequestDTO> list = leaveService.getAllLeaveRequests(status);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn nghỉ thành công", list));
    }

    // ==========================================
    // 2. NHÓM API: LÀM THÊM GIỜ (OVERTIME)
    // ==========================================

    @PostMapping("/overtime")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<OvertimeRequestDTO>> createOvertimeRequest(
            @Valid @RequestBody OvertimeRequestDTO dto,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail(HttpStatus.BAD_REQUEST, errorMessage));
        }

        OvertimeRequestDTO result = overtimeService.createOvertimeRequest(dto, authentication.getName());

        return ResponseEntity.ok(ApiResponse.success("Đăng ký làm thêm giờ thành công", result));
    }

    @GetMapping("/my-overtimes")
    @PreAuthorize("hasAnyRole('USER', 'EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<OvertimeRequestDTO>>> getMyOvertimeRequests(Authentication authentication) {

        List<OvertimeRequestDTO> list = overtimeService.getMyOvertimeRequests(authentication.getName());

        return ResponseEntity.ok(ApiResponse.success("Lấy lịch sử làm thêm giờ thành công", list));
    }

    @PostMapping("/overtime/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<OvertimeRequestDTO>> approveOvertimeRequest(
            @PathVariable Long id,
            @RequestBody ApproveRequestDTO dto,
            Authentication authentication) {

        OvertimeRequestDTO result = overtimeService.approveOvertimeRequest(id, dto, authentication.getName());

        return ResponseEntity.ok(ApiResponse.success("Phê duyệt đơn làm thêm thành công", result));
    }
    // 2. (BỔ SUNG) ADMIN XEM DANH SÁCH OT
    // ==========================================
    @GetMapping("/overtimes")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<OvertimeRequestDTO>>> getAllOvertimeRequests(
            @RequestParam(required = false) RequestStatus status) {

        List<OvertimeRequestDTO> list = overtimeService.getAllOvertimeRequests(status);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách đơn làm thêm thành công", list));
    }
}