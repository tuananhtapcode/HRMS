package com.project.hrms.repository;

import com.project.hrms.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    // Tìm bản ghi chấm công của 1 nhân viên trong 1 ngày
    Optional<AttendanceRecord> findByEmployee_EmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    List<AttendanceRecord> findByEmployee_EmployeeIdAndAttendanceDateBetween(
            Long employeeId, LocalDate startDate, LocalDate endDate
    );
}