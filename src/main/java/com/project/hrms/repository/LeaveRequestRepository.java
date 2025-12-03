package com.project.hrms.repository;

import com.project.hrms.model.LeaveRequest;
import com.project.hrms.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    // Lấy lịch sử đơn nghỉ của 1 nhân viên (sắp xếp mới nhất trước)
    List<LeaveRequest> findByEmployee_EmployeeIdOrderByCreatedAtDesc(Long employeeId);

    // (Dành cho Quản lý) Lấy danh sách đơn theo trạng thái (VD: lấy list Pending để duyệt)
    List<LeaveRequest> findByStatus(RequestStatus status);


    // Lấy list đơn nghỉ ĐÃ DUYỆT của 1 nhân viên trong khoảng thời gian
    // (Query này hơi phức tạp vì ngày nghỉ có thể vắt qua 2 tháng, nhưng để MVP ta check start date)
    @Query("SELECT l FROM LeaveRequest l WHERE l.employee.employeeId = :empId " +
            "AND l.status = 'Approved' " +
            "AND (l.startDate BETWEEN :startDate AND :endDate OR l.endDate BETWEEN :startDate AND :endDate)")
    List<LeaveRequest> findApprovedLeaveInMonth(
            @Param("empId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}