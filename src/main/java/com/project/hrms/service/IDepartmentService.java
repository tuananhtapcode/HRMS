package com.project.hrms.service;

import com.project.hrms.dto.DepartmentDTO;
import com.project.hrms.model.Department;

import java.util.List;

public interface IDepartmentService {
    Department create(DepartmentDTO newDepartmentDTO);

    Department getById(Long departmentId);

    Department update(Long id, DepartmentDTO departmentDTO);

    void deleteById(Long departmentId);

    List<Department> findByManager_EmployeeId(Long managerId);

}
