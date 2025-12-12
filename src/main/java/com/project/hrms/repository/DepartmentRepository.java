package com.project.hrms.repository;

import com.project.hrms.model.Department;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Department findByManager_EmployeeId(Long managerId);

    Boolean existsByName(String name);

    boolean existsByCode(String code);

    List<Department> findByNameContainingIgnoreCase(String name);

}
