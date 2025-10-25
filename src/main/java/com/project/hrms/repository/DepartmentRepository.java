package com.project.hrms.repository;

import com.project.hrms.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    public List<Department> findByManager_EmployeeId(Long id);
}
