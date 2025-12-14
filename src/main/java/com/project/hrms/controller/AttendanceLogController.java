package com.project.hrms.controller;

import com.project.hrms.model.AttendanceLog;
import com.project.hrms.repository.AttendanceLogRepository;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.response.AttendanceLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/attendance-logs")
@RequiredArgsConstructor
public class AttendanceLogController {

    private final AttendanceLogRepository logRepository;

    // API cho Menu: "Dữ liệu chấm công"
    @GetMapping
    public ResponseEntity<?> getLogs(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        // Sắp xếp mặc định: Mới nhất lên đầu
        Pageable pageable = PageRequest.of(page, size, Sort.by("time").descending());

        Page<AttendanceLog> entityPage;

        // 1. Lấy dữ liệu Entity từ DB
        if (employeeId != null && date != null) {
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.plusDays(1).atStartOfDay();
            entityPage = logRepository.findByEmployee_EmployeeIdAndTimeBetween(employeeId, start, end, pageable);
        } else if (employeeId != null) {
            entityPage = logRepository.findByEmployee_EmployeeId(employeeId, pageable);
        } else {
            entityPage = logRepository.findAll(pageable);
        }

        // 2. CHUYỂN ĐỔI SANG DTO (Quan trọng)
        // map() sẽ chạy qua từng phần tử và gọi hàm fromEntity
        Page<AttendanceLogResponse> responsePage = entityPage.map(AttendanceLogResponse::fromEntity);

        return ResponseEntity.ok(ApiResponse.success("Lấy dữ liệu chấm công thành công", responsePage));
    }
}