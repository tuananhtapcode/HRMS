// src/main/java/com/project/hrms/controller/AttendanceController.java
package com.project.hrms.controller;

import com.project.hrms.response.ApiResponse;
import com.project.hrms.response.AttendanceResponse;
import com.project.hrms.service.IAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'EMPLOYEE')")
public class AttendanceController {

    // Inject Interface thay vì Class cụ thể
    private final IAttendanceService attendanceService;

    @PostMapping("/check-in")
    public ResponseEntity<?> checkIn(Authentication authentication) {

        String username = authentication.getName();

        // Gọi service
        AttendanceResponse response = attendanceService.performCheckIn(username);

        // Trả về theo format ApiResponse
        return ResponseEntity.ok(ApiResponse.success("Check-in thành công!", response));
    }

    @PostMapping("/check-out")
    public ResponseEntity<?> checkOut(Authentication authentication) {

        String username = authentication.getName();

        AttendanceResponse response = attendanceService.performCheckOut(username);

        return ResponseEntity.ok(ApiResponse.success("Check-out thành công!", response));
    }
}