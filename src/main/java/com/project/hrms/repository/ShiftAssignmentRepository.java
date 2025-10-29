package com.project.hrms.repository;

import com.project.hrms.model.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {

    /**
     * Dùng để kiểm tra (logic UPSERT) xem nhân viên đã có ca
     * trong ngày đó chưa.
     * Tên hàm: findBy[Tên trường của Entity].[Tên trường của Entity con]...
     */
    Optional<ShiftAssignment> findByEmployee_EmployeeIdAndAssignmentDate(Long employeeId, LocalDate date);

    /**
     * Dùng để lấy lịch làm việc của nhân viên trong một khoảng thời gian
     * (ví dụ: lấy lịch làm việc tháng 11)
     */
    List<ShiftAssignment> findByEmployee_EmployeeIdAndAssignmentDateBetween(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate
    );
}