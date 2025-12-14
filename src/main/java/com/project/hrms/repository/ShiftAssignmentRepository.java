package com.project.hrms.repository;

import com.project.hrms.model.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<ShiftAssignment> findAllByEmployee_EmployeeIdAndAssignmentDate(Long employeeId, LocalDate date);
    /**
     * Dùng để lấy lịch làm việc của nhân viên trong một khoảng thời gian
     * (ví dụ: lấy lịch làm việc tháng 11)
     */
    List<ShiftAssignment> findByEmployee_EmployeeIdAndAssignmentDateBetweenAndIsApproved(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate,
            Boolean isApproved
    );

    // Trong interface ShiftAssignmentRepository
    @Query("SELECT s FROM ShiftAssignment s " +
            "WHERE s.employee.department.departmentId = :deptId " +
            "AND s.assignmentDate BETWEEN :startDate AND :endDate " +
            "ORDER BY s.employee.fullName ASC, s.assignmentDate ASC")
    List<ShiftAssignment> findByDepartmentAndDateBetween(
            @Param("deptId") Long deptId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // --- THÊM ĐOẠN NÀY VÀO REPOSITORY ---

    // Tìm tất cả các ca làm việc nằm trong khoảng ngày start và end

    List<ShiftAssignment> findByAssignmentDateBetween(LocalDate startDate, LocalDate endDate);

    List<ShiftAssignment> findByIsApprovedFalseOrderByAssignmentDateAsc();
}