package com.project.hrms.repository;

import com.project.hrms.model.SalaryProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SalaryProfileRepository extends JpaRepository<SalaryProfile, Long>,
        JpaSpecificationExecutor<SalaryProfile> {

    // Lấy toàn bộ khoản lương của 1 nhân viên
    List<SalaryProfile> findByEmployee_EmployeeId(Long employeeId);

    // Lấy 1 khoản cụ thể của nhân viên theo component
    Optional<SalaryProfile> findByEmployee_EmployeeIdAndSalaryComponent_SalaryComponentId(Long employeeId, Long salaryComponentId);

    // Check đã có profile cho component chưa (để tránh insert trùng)
    boolean existsByEmployee_EmployeeIdAndSalaryComponent_SalaryComponentId(Long employeeId, Long salaryComponentId);

    // Xóa toàn bộ profile của 1 nhân viên (khi reset)
    void deleteByEmployee_EmployeeId(Long employeeId);

    List<SalaryProfile> findByEmployee_EmployeeIdIn(List<Long> employeeIds);

}
