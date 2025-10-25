package com.project.hrms.service;

import com.project.hrms.dto.DepartmentDTO;
import com.project.hrms.model.Department;

import java.util.List;

public interface IDepartment {
    Department createDepartment(DepartmentDTO newDepartmentDTO);

    Department getDepartment(Long departmentId);

    Department updateDepartment(Long id, DepartmentDTO departmentDTO);

    void deleteById(Long departmentId);

    List<Department> findByManager_EmployeeId(Long managerId);

}
