package com.project.hrms.repository;

import com.project.hrms.model.Employee;
import com.project.hrms.model.Workday;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkdayRepository extends JpaRepository<Workday, Long> {

    // Tìm Workday của 1 nhân viên vào 1 ngày cụ thể (để xử lý daily)
    Optional<Workday> findByEmployeeAndDate(Employee employee, LocalDate date);

    // Tìm list Workday của 1 nhân viên trong khoảng thời gian (để tổng hợp tháng)
    List<Workday> findByEmployee_EmployeeIdAndDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    // (Tùy chọn) Tìm Workday theo phòng ban và khoảng thời gian
    @Query("SELECT w FROM Workday w WHERE w.employee.department.departmentId = :deptId AND w.date BETWEEN :startDate AND :endDate")
    List<Workday> findByDepartmentAndDateBetween(@Param("deptId") Long deptId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}