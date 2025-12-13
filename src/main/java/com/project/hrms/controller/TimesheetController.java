package com.project.hrms.controller;

import com.project.hrms.dto.TimesheetSummaryDTO;
import com.project.hrms.response.ApiResponse;
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
}