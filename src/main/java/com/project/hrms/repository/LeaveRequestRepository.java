package com.project.hrms.repository;

import com.project.hrms.model.LeaveRequest;
import com.project.hrms.model.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    // Lấy lịch sử đơn nghỉ của 1 nhân viên (sắp xếp mới nhất trước)
    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<LeaveRequest> findByEmployeeIdAndStatusAndDateBetween(
          Long employeeId, RequestStatus requestStatus, LocalDate fromDate, LocalDate toDate
);

    // Kiểm tra trùng đơn nghỉ đã APPROVED
//    bỏ AND l.leaveType = :leaveType
    @Query("""
                SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END
                FROM LeaveRequest l
                WHERE l.employeeId = :employeeId
                  AND l.status IN ('PENDING', 'APPROVED')
                  AND l.leaveRequestId <> COALESCE(:excludeId, -1)
                  AND l.startDate <= :end
                  AND l.endDate >= :start
            """)
    boolean existsOverlap(
            @Param("employeeId") Long employeeId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("excludeId") Long excludeId
//            @Param("leaveType") LeaveType leaveType
    );

    // Các hàm bạn đã có
    List<LeaveRequest> findByStatus(RequestStatus status);

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    long countByStatus(RequestStatus status);

    long countByEmployeeId(Long employeeId);

    // Lấy list đơn nghỉ ĐÃ DUYỆT của 1 nhân viên trong khoảng thời gian
    // (Query này hơi phức tạp vì ngày nghỉ có thể vắt qua 2 tháng, nhưng để MVP ta check start date)
    @Query("SELECT l FROM LeaveRequest l WHERE l.employeeId = :empId " +
            "AND l.status = 'APPROVED' " +
            "AND (l.startDate BETWEEN :startDate AND :endDate OR l.endDate BETWEEN :startDate AND :endDate)")
    List<LeaveRequest> findApprovedLeaveInMonth(
            @Param("empId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    long countByEmployeeIdAndStatus(Long employeeId, RequestStatus status);

    Page<LeaveRequest> findByStatus(
            RequestStatus status,
            Pageable pageable
    );

    Page<LeaveRequest> findByEmployeeIdAndStatus(
            Long employeeId,
            RequestStatus status,
            Pageable pageable
    );
}

