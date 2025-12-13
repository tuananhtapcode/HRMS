package com.project.hrms.repository;

import com.project.hrms.model.OvertimeRequest;
import com.project.hrms.model.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long> {
    // Lấy lịch sử OT của 1 nhân viên
    List<OvertimeRequest> findByEmployee_EmployeeIdOrderByCreatedAtDesc(Long employeeId);

    // Lấy list OT ĐÃ DUYỆT của 1 nhân viên trong 1 tháng
    List<OvertimeRequest> findByEmployee_EmployeeIdAndStatusAndDateBetween(
            Long employeeId, RequestStatus status, LocalDate startDate, LocalDate endDate
    );

    /**
     * Tìm các OT cùng employee, cùng ngày, trạng thái PENDING/APPROVED,
     * mà có giao nhau về thời gian với (start,end).
     * Dùng để tránh trùng giờ.
     */
//    @Query("""
//                SELECT o FROM OvertimeRequest o
//                WHERE o.employeeId = :employeeId
//                  AND o.date = :date
//                  AND o.status IN (:statuses)
//                  AND NOT (o.endTime <= :startTime OR o.startTime >= :endTime)
//            """)
    @Query("SELECT o FROM OvertimeRequest o WHERE o.employeeId = :employeeId AND o.date = :date " +
            "AND o.status IN :statuses " +
            "AND NOT (o.endTime <= :startTime OR o.startTime >= :endTime)")
    List<OvertimeRequest> findOverlapping(
            @Param("employeeId") Long employeeId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("statuses") List<RequestStatus> statuses
    );

    Page<OvertimeRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    List<OvertimeRequest> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    Optional<OvertimeRequest> findByOvertimeRequestIdAndEmployeeId(Long id, Long employeeId);

    long countByStatus(RequestStatus status);

    long countByEmployeeId(Long employeeId);

    long countByEmployeeIdAndStatus(Long employeeId, RequestStatus status);

    @Query("""
    SELECT COALESCE(SUM(o.totalHours * 60), 0)
    FROM OvertimeRequest o
    WHERE o.status = 'APPROVED'
""")
    int sumApprovedMinutesAll();

    @Query("""
    SELECT COALESCE(SUM(o.totalHours * 60), 0)
    FROM OvertimeRequest o
    WHERE o.employeeId = :empId AND o.status = 'APPROVED'
""")
    int sumApprovedMinutesByEmployee(Long empId);

    @Query("""
    SELECT MONTH(o.date) AS month, COALESCE(SUM(o.totalHours * 60), 0)
    FROM OvertimeRequest o
    WHERE YEAR(o.date) = :year AND o.status = 'APPROVED'
    GROUP BY MONTH(o.date)
""")
    List<Object[]> getMonthlyApprovedMinutes(int year);

}

