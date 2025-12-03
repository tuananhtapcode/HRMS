package com.project.hrms.service;

import java.time.LocalDate;

public interface IDailyTimesheetService {
    // Hàm xử lý lại công cho 1 nhân viên vào 1 ngày cụ thể
    void processDailyWorkday(Long employeeId, LocalDate date);

    // Hàm chạy xử lý cho toàn bộ nhân viên (Dùng cho Cronjob cuối ngày)
    void processAllDailyWorkday(LocalDate date);
}