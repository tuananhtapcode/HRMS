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

    // Kiểm tra trùng đơn nghỉ đã APPROVED
    @Query("""
        SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END
        FROM LeaveRequest l
        WHERE l.employeeId = :employeeId
          AND l.status = 'APPROVED'
          AND l.leaveRequestId <> COALESCE(:excludeId, -1)
          AND l.startDate <= :end
          AND l.endDate >= :start
    """)
    boolean existsOverlap(Long employeeId, LocalDate start, LocalDate end, Long excludeId);

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
}

