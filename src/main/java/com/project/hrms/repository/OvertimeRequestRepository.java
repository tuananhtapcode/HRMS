package com.project.hrms.repository;

import com.project.hrms.model.OvertimeRequest;
import com.project.hrms.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OvertimeRequestRepository extends JpaRepository<OvertimeRequest, Long> {
    // Lấy lịch sử OT của 1 nhân viên
    List<OvertimeRequest> findByEmployee_EmployeeIdOrderByCreatedAtDesc(Long employeeId);

    // Lấy danh sách đơn theo trạng thái
    List<OvertimeRequest> findByStatus(RequestStatus status);
    // Lấy list OT ĐÃ DUYỆT của 1 nhân viên trong 1 tháng
    List<OvertimeRequest> findByEmployee_EmployeeIdAndStatusAndDateBetween(
            Long employeeId, RequestStatus status, LocalDate startDate, LocalDate endDate
    );
}