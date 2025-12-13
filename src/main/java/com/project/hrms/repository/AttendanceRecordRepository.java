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
//    Optional<AttendanceRecord> findByEmployee_EmployeeIdAndAttendanceDate(
//            Long employeeId,
//            LocalDate attendanceDate
//    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select ar
        from AttendanceRecord ar
        where ar.employee.employeeId = :employeeId
          and ar.attendanceDate = :date
    """)
    Optional<AttendanceRecord> findByEmployeeIdAndAttendanceDateForUpdate(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date
    );
}

