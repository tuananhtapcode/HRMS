package com.project.hrms.repository;

import com.project.hrms.model.Employee;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long>,
        JpaSpecificationExecutor<Employee> {

    // Kiểm tra tồn tại mã nhân viên
    boolean existsByEmployeeCode(@NotBlank(message = "Mã nhân viên không được để trống") String employeeCode);

    // Kiểm tra tồn tại email
    boolean existsByEmail(String email);

    // Kiểm tra tồn tại số điện thoại
    boolean existsByPhoneNumber(String phoneNumber);

    // Kiểm tra tồn tại số tài khoản ngân hàng
    boolean existsByBankAccount(String bankAccount);

    // Tìm theo phòng ban
    List<Employee> findByDepartment_DepartmentId(Long departmentId);

    // Tìm theo vị trí công việc
    List<Employee> findByJobPosition_JobPositionId(Long jobPositionId);

//    // Tìm theo id manager (người quản lý)
//    List<Employee> findByManager_ManagerId(Long managerId);

    // Tìm theo tên chứa (keyword) không phân biệt hoa thường
    List<Employee> findByFullNameContainingIgnoreCase(String keyword);

    // Tìm theo trạng thái
    List<Employee> findByStatus(String status);

//
//    // Tìm theo tên chứa
//    Page<Employee> findByFullNameContainingIgnoreCase(String name, Pageable pageable);
//
//    // Tìm theo code
//    Page<Employee> findByEmployeeCodeContainingIgnoreCase(String code, Pageable pageable);
//
//    // Tìm theo phòng ban
//    Page<Employee> findByDepartment_NameContainingIgnoreCase(String departmentName, Pageable pageable);
//
//    // Tìm nhân viên theo department manager
//    Page<Employee> findByDepartment_Manager_Id(Long managerId, Pageable pageable);

}
