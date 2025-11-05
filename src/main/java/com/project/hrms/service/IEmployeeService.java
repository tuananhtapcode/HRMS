package com.project.hrms.service;

import com.project.hrms.dto.CreateEmployeeRequestDTO;
import com.project.hrms.dto.EmployeeDTO;
import com.project.hrms.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IEmployeeService {
    // Constant cho tên ngân hàng mặc định của công ty
    String COMPANY_BANK_NAME = "BIDV"; // Có thể thay đổi tên ngân hàng theo yêu cầu

    // Tạo mới nhân viên
    Employee create(EmployeeDTO employeeDTO);

    // Cập nhật thông tin nhân viên
    Employee update(Long id, EmployeeDTO employeeDTO);

    // Xóa nhân viên theo id
    void deleteById(Long id);

    // Lấy thông tin nhân viên theo id
    Employee getById(Long id);

    // Lấy danh sách tất cả nhân viên có phân trang
    Page<Employee> getAllEmployees(Pageable pageable);

    // Tìm nhân viên theo phòng ban
    List<Employee> findByDepartmentId(Long departmentId);

    // Tìm nhân viên theo vị trí công việc
    List<Employee> findByJobPositionId(Long jobPositionId);

    // Tìm nhân viên theo manager
//    List<Employee> findByManagerId(Long managerId);

    // Tìm kiếm nhân viên theo tên
    List<Employee> searchByName(String keyword);

    // Lấy danh sách nhân viên theo trạng thái
    List<Employee> findByStatus(String status);

    // Kiểm tra email đã tồn tại
    boolean existsByEmail(String email);

    // Kiểm tra số điện thoại đã tồn tại
    boolean existsByPhoneNumber(String phoneNumber);

    // Cập nhật thông tin tài khoản ngân hàng
    Employee updateBankInfo(Long employeeId, String bankAccount);

    // Kiểm tra số tài khoản ngân hàng đã tồn tại
    boolean existsByBankAccount(String bankAccount);

    // Xác thực tài khoản ngân hàng với ngân hàng mặc định của công ty
    boolean verifyBankAccount(String bankAccount);

    // Lấy lịch sử thay đổi thông tin ngân hàng
    List<String> getBankInfoHistory(Long employeeId);

    //Test tao tai khoan
    Employee createEmployeeAndAccount(CreateEmployeeRequestDTO dto) throws Exception;
}
