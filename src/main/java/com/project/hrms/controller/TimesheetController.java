package com.project.hrms.controller;

import com.project.hrms.dto.TimesheetDetailDTO;
import com.project.hrms.dto.TimesheetSummaryDTO;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.service.AuthService;
import com.project.hrms.service.ITimesheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/timesheets")
@RequiredArgsConstructor
public class TimesheetController {

    private final ITimesheetService timesheetService;
    private final AuthService authService;

    // 1. Xem bảng tổng hợp (Report)
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<TimesheetSummaryDTO>>> getTimesheetSummary(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(required = false) Long departmentId) {

        List<TimesheetSummaryDTO> summary = timesheetService.getMonthlyTimesheetSummary(month, year, departmentId);

        return ResponseEntity.ok(ApiResponse.success("Lấy bảng chấm công tổng hợp thành công", summary));
    }

    // 2. API "Kích hoạt" tính toán công (Dùng để test hoặc chạy bù)
    // Thực tế sẽ dùng Cronjob, nhưng HR cần nút này để "Cập nhật công" sau khi sửa đơn
    @PostMapping("/process-daily")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> triggerDailyProcess(@RequestParam String date) {

        timesheetService.runDailyProcessManually(date);

        return ResponseEntity.ok(ApiResponse.success("Đã chạy xử lý công cho ngày " + date, null));
    }

    // 3. Xem bảng công chi tiết (Grid view từng ngày)
    @GetMapping("/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<List<TimesheetDetailDTO.Response>>> getTimesheetDetails(
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam(required = false) Long departmentId) {

        List<TimesheetDetailDTO.Response> details = timesheetService.getMonthlyTimesheetDetails(month, year, departmentId);

        return ResponseEntity.ok(ApiResponse.success("Lấy bảng công chi tiết thành công", details));
    }
    // 4. Nhân viên tự xem bảng công chi tiết của mình
    @GetMapping("/me")
    // Cho phép tất cả user đã login (ROLE_EMPLOYEE, MANAGER, ADMIN đều xem được của chính mình)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TimesheetDetailDTO.Response>> getMyTimesheet(
            @RequestParam int month,
            @RequestParam int year) {

        // Lấy ID từ Token
        Long currentUserId = authService.getCurrentUserId();

        TimesheetDetailDTO.Response result = timesheetService.getMyMonthlyTimesheet(month, year, currentUserId);

        return ResponseEntity.ok(ApiResponse.success("Lấy bảng công cá nhân thành công", result));
    }
}