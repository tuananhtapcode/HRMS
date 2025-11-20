package com.project.hrms.repository;

import com.project.hrms.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findByManager_EmployeeId(Long managerId);

    // ✅ lấy mã lớn nhất hiện có, ví dụ: D005
    Optional<Department> findTopByOrderByCodeDesc();

    Boolean existsByName(String name);

    List<Department> findByNameContainingIgnoreCase(String name);

}
