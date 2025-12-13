package com.project.hrms.repository;

import com.project.hrms.model.AttendanceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    // Lấy tất cả log của nhân viên trong 1 khoảng thời gian, sắp xếp tăng dần
    List<AttendanceLog> findByEmployee_EmployeeIdAndTimeBetweenOrderByTimeAsc(
            Long employeeId, LocalDateTime startTime, LocalDateTime endTime);
}