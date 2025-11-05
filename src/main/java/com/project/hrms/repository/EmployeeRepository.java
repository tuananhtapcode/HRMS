package com.project.hrms.repository;

import com.project.hrms.model.Employee;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByBankAccount(String bankAccount);
    List<Employee> findByDepartment_DepartmentId(Long departmentId);
    List<Employee> findByJobPosition_JobPositionId(Long jobPositionId);
//    List<Employee> findByManager_ManagerId(Long managerId);
    List<Employee> findByFullNameContainingIgnoreCase(String keyword);
    List<Employee> findByStatus(String status);

    boolean existsByEmployeeCode(@NotBlank(message = "Mã nhân viên không được để trống") String employeeCode);
}
