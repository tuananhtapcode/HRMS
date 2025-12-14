package com.project.hrms.controller;

import com.project.hrms.dto.AttendanceTapDTO;
import com.project.hrms.dto.MonthlySummaryDTO;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.response.AttendanceResponse;
import com.project.hrms.service.IAttendanceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'EMPLOYEE')")
public class AttendanceController {

    private final IAttendanceRecordService attendanceService;

    /**
     * API Check-in dành cho App/Web (Nút Check-in)
     * Bản chất là ghi nhận một log thời gian
     */
    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(Authentication authentication) {
        String username = authentication.getName();

        // Tạo DTO mặc định cho hành động Check-in
        AttendanceTapDTO dto = new AttendanceTapDTO();
        dto.setSource("APP");

        // Gọi hàm tapAttendance chung
        AttendanceResponse response = attendanceService.tapAttendance(username, dto);

        return ResponseEntity.ok(ApiResponse.success("Ghi nhận thời gian thành công (Check-in)", response));
    }

    /**
     * API Check-out dành cho App/Web (Nút Check-out)
     * Bản chất cũng là ghi nhận log, Service sẽ tự tính toán Min/Max để ra Check-out
     */
    @PostMapping("/check-out")
    public ResponseEntity<?> checkOut(Authentication authentication) {
        String username = authentication.getName();

        // Tạo DTO mặc định
        AttendanceTapDTO dto = new AttendanceTapDTO();
        dto.setSource("APP");

        // Gọi hàm tapAttendance chung
        AttendanceResponse response = attendanceService.tapAttendance(username, dto);

        return ResponseEntity.ok(ApiResponse.success("Ghi nhận thời gian thành công (Check-out)", response));
    }

    /**
     * API Tổng quát (Dùng cho máy chấm công hoặc API tích hợp)
     * Cho phép gửi kèm Source (VD: FINGERPRINT, FACE_ID)
     */
    @PostMapping("/tap")
    public ResponseEntity<ApiResponse<AttendanceResponse>> tapAttendance(
            @RequestBody(required = false) AttendanceTapDTO dto,
            Authentication authentication) {

        // Nếu không gửi body, tạo DTO mặc định
        if (dto == null) {
            dto = new AttendanceTapDTO();
            dto.setSource("APP");
        }

        String username = authentication.getName();

        AttendanceResponse response = attendanceService.tapAttendance(username, dto);

        return ResponseEntity.ok(ApiResponse.success("Chấm công thành công", response));
    }

    // Trong AttendanceController bảng công tổng hợp
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<?> getMonthlySummary(
            @RequestParam int month,
            @RequestParam int year) {

        List<MonthlySummaryDTO> summary = attendanceService.getMonthlySummary(month, year);
        return ResponseEntity.ok(ApiResponse.success("Lấy bảng công tổng hợp thành công", summary));
    }
}