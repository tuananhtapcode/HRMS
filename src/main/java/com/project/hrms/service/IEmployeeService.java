package com.project.hrms.service;

import com.project.hrms.dto.CreateEmployeeRequestDTO;
import com.project.hrms.dto.EmployeeDTO;
import com.project.hrms.dto.EmployeeSearchRequest;
import com.project.hrms.model.Employee;
import com.project.hrms.response.EmployeeResponse;
import com.project.hrms.utils.excel.ExcelErrorResponse;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface IEmployeeService {
    // Constant cho tên ngân hàng mặc định của công ty
    String COMPANY_BANK_NAME = "BIDV"; // Có thể thay đổi tên ngân hàng theo yêu cầu

    // Tạo mới nhân viên
    Employee create(EmployeeDTO employeeDTO);

    // Cập nhật thông tin nhân viên
    Employee update(Long id, EmployeeDTO employeeDTO);

    // Xóa nhân viên theo id
    void delete(Long id);

    EmployeeResponse getDetail(Long id);

    // Lấy thông tin nhân viên theo id
    Employee getById(Long id);

    // Lấy danh sách tất cả nhân viên có phân trang
    Page<EmployeeResponse> getAll(PageRequest pageRequest);

    Page<EmployeeResponse> search(EmployeeSearchRequest request, Pageable pageable);

    Page<EmployeeResponse> quickFilter(
            String status,
            Long departmentId,
            String gender,
            Pageable pageable
    );

    //Test tao tai khoan
    Employee createEmployeeAndAccount(CreateEmployeeRequestDTO dto) throws Exception;

    /**
     * Import danh sách nhân viên từ file Excel.
     * @param file: file Excel upload từ FE
     * @return ExcelErrorResponse: báo cáo lỗi + số dòng thành công
     */
    ExcelErrorResponse importEmployees(MultipartFile file);

    /**
     * Export toàn bộ nhân viên ra file Excel.
     * @return Workbook: để controller tự stream ra response
     */
    Workbook exportEmployees();

    /**
     * Tải file mẫu import nhân viên
     * Kèm theo Dropdown list dữ liệu có sẵn
     */
    Workbook getImportTemplate();
}
