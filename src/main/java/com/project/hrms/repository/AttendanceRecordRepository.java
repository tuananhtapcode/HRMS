package com.project.hrms.repository;

import com.project.hrms.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByEmployee_EmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    List<AttendanceRecord> findByEmployee_EmployeeIdAndAttendanceDateBetween(
            Long employeeId, LocalDate startDate, LocalDate endDate
    );
}