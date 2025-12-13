package com.project.hrms.repository;

import com.project.hrms.model.AttendanceRecord;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    // Tìm bản ghi chấm công của 1 nhân viên trong 1 ngày
    Optional<AttendanceRecord> findByEmployee_EmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);
    /**
     * Lấy record của nhân viên theo ngày.
     * @return Optional để service quyết định create hay throw.
     */
    Optional<AttendanceRecord> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);

    List<AttendanceRecord> findByEmployee_EmployeeIdAndAttendanceDateBetween(
            Long employeeId, LocalDate startDate, LocalDate endDate
    );
    /**
     * Lấy record với lock để cập nhật an toàn (pessimistic lock).
     * Dùng trong add/subtract để tránh race condition.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AttendanceRecord a WHERE a.employeeId = :employeeId AND a.attendanceDate = :attendanceDate")
    Optional<AttendanceRecord> findByEmployeeIdAndAttendanceDateForUpdate(@Param("employeeId") Long employeeId,
                                                                          @Param("attendanceDate") LocalDate attendanceDate);
}

