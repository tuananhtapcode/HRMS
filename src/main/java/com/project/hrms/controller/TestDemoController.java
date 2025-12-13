package com.project.hrms.controller;

import com.project.hrms.dto.AttendanceTapDTO;
import com.project.hrms.model.AttendanceLog;
import com.project.hrms.repository.AccountRepository;
import com.project.hrms.repository.AttendanceLogRepository;
import com.project.hrms.service.IAttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/test-demo")
@RequiredArgsConstructor
public class TestDemoController {

    private final AttendanceLogRepository logRepository;
    private final IAttendanceService attendanceService; // Inject Service của bạn
    private final AccountRepository accountRepository;

    @PostMapping("/simulate-full-day")
    public ResponseEntity<?> simulateFullDay(@RequestParam String username) {
        // 1. Tìm user
        var account = accountRepository.findByUsername(username).orElseThrow();
        var emp = account.getEmployee();
        LocalDate today = LocalDate.now();

        // 2. Tạo list giờ fake
        List<String> times = List.of(
                "07:55:00", // Sáng vào
                "12:05:00", // Sáng ra
                "13:25:00", // Chiều vào
                "19:00:00"  // Tối về (OT)
        );

        // 3. Lưu log giả vào DB
        for (String timeStr : times) {
            LocalDateTime fakeTime = LocalDateTime.of(today, LocalTime.parse(timeStr));

            AttendanceLog log = new AttendanceLog();
            log.setEmployee(emp);
            log.setTime(fakeTime);
            log.setSource("SIMULATION");
            logRepository.save(log);
        }

        // 4. Gọi hàm Tap 1 phát để trigger tính toán (Dùng DTO giả)
        // Lưu ý: Hàm tapAttendance của bạn sẽ lấy LocalDateTime.now(), 
        // nhưng bên trong nó sẽ query lại toàn bộ log trong ngày -> Nên nó sẽ thấy các log fake ở trên.
        AttendanceTapDTO dummyDto = new AttendanceTapDTO();
        dummyDto.setSource("TRIGGER_CALC");
        var result = attendanceService.tapAttendance(username, dummyDto);

        return ResponseEntity.ok("Đã giả lập dữ liệu xong! Kiểm tra bảng Record. Kết quả lần tính cuối: " + result);
    }
}