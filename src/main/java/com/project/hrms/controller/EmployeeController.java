// src/main/java/com/project/hrms/controller/EmployeeController.java
package com.project.hrms.controller;

import com.project.hrms.dto.CreateEmployeeRequestDTO;
import com.project.hrms.dto.EmployeeDTO;
import com.project.hrms.dto.EmployeeSearchRequest;
import com.project.hrms.model.Employee;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.response.EmployeeListResponse;
import com.project.hrms.response.EmployeeResponse;
import com.project.hrms.service.IEmployeeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final IEmployeeService employeeService;

    // ===============================
    // 1) CREATE EMPLOYEE + ACCOUNT
    // ===============================
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createEmployee(
            @Valid @RequestBody CreateEmployeeRequestDTO dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errorMessages);
        }

        try {
            Employee employee = employeeService.createEmployeeAndAccount(dto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo nhân viên thành công", EmployeeResponse.fromEmployee(employee)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail(HttpStatus.BAD_REQUEST, e.getMessage()));
        }
    }

    // 2) GET ALL EMPLOYEES
    //    /api/employees GET Danh sách nhân viên
    @GetMapping
    public ResponseEntity<ApiResponse<EmployeeListResponse>> getEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        PageRequest pageRequest = PageRequest.of(page, limit).withPage(page);
        Page<EmployeeResponse> employeePage = employeeService.getAll(pageRequest);

        EmployeeListResponse listResponse = EmployeeListResponse.builder()
                .employees(employeePage.getContent())
                .totalPages(employeePage.getTotalPages())
                .totalElements(employeePage.getTotalElements())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhân viên thành công", listResponse));
    }

    // 3) QUICK FILTER (status, department, gender)
    @GetMapping("/filter")
    public ResponseEntity<?> filterEmployees(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<EmployeeResponse> employeePage = employeeService.quickFilter(
                status, departmentId, gender,
                PageRequest.of(page, size)
        );

        EmployeeListResponse listResponse = EmployeeListResponse.builder()
                .employees(employeePage.getContent())
                .totalPages(employeePage.getTotalPages())
                .totalElements(employeePage.getTotalElements())
                .build();
        // co hai kieu tra ve. 1 là theo format ta thiet ke, hai la tra ve page
        return ResponseEntity.ok(ApiResponse.success("Danh sách nhân viên (quick filter)", listResponse));
//        return ResponseEntity.ok(ApiResponse.success("Danh sách nhân viên (quick filter)", employeePage));
    }

    // 4) SEARCH ADVANCED (POST)
    // Tìm kiếm nhân viên theo nhiều tiêu chí với phân trang
    @PostMapping("/search")
    public ResponseEntity<?> advancedSearch(
            @RequestBody EmployeeSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<EmployeeResponse> employeePage = employeeService.search(
                request, PageRequest.of(page, size)
        );

        EmployeeListResponse listResponse = EmployeeListResponse.builder()
                .employees(employeePage.getContent())
                .totalPages(employeePage.getTotalPages())
                .totalElements(employeePage.getTotalElements())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Kết quả tìm kiếm nâng cao 'danh sách nhân viên' ", listResponse));
//        return ResponseEntity.ok(ApiResponse.success("Kết quả tìm kiếm nâng cao 'danh sách nhân viên' ", employeePage));
    }

    // 5) SEARCH BASIC (GET) - name / code / department
    @GetMapping("/search")
    public ResponseEntity<?> searchBasic(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        EmployeeSearchRequest req = new EmployeeSearchRequest();
        req.setName(name);
        req.setCode(code);
        req.setDepartmentId(departmentId);

        Page<EmployeeResponse> employeePage =
                employeeService.search(req, PageRequest.of(page, size));

        EmployeeListResponse listResponse = EmployeeListResponse.builder()
                .employees(employeePage.getContent())
                .totalPages(employeePage.getTotalPages())
                .totalElements(employeePage.getTotalElements())
                .build();

        return ResponseEntity.ok(ApiResponse.success("Kết quả tìm kiếm", listResponse));
//        return ResponseEntity.ok(ApiResponse.success("Kết quả tìm kiếm", employeePage));
    }

    // 6) GET BY ID

    /// api/employees/{id} GET Chi tiết nhân viên
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        EmployeeResponse employee = employeeService.getDetail(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy nhân viên thành công", employee));
    }

    // 7) UPDATE

    /// api/employees/{id} PUT Cập nhật nhân viên
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDTO employeeDTO,
            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = bindingResult.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(ApiResponse.fail(BAD_REQUEST, String.join("; ", errorMessages)));
        }
        Employee updated = employeeService.update(id, employeeDTO);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhân viên thành công", EmployeeResponse.fromEmployee(updated)));
    }

    // 8) DELETE (Soft delete)

    /// api/employees/{id} DELETE  Xóa (hoặc soft delete) nhân viên
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa nhân viên thành công", null));
    }


    // 9) TÌm kiếm tài khoản của nhân viên
    @GetMapping("/{id}/account")
//@PreAuthorize("hasAnyRole('ADMIN', 'USER')")  // Comment lại để bỏ qua xác thực token
    public ResponseEntity<?> getAccountById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy tài khoản thành công", EmployeeResponse.fromEmployee(employee)));
    }

    // 10) import tu file excel
    @PostMapping("/import")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(employeeService.importEmployees(file));
    }

    //export ra file Excel
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) throws Exception {
        Workbook wb = employeeService.exportEmployees();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=employees_" + LocalDate.now() + ".xlsx");
        wb.write(response.getOutputStream());
        wb.close();
    }

    @GetMapping("/import-template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        // 1. Lấy workbook từ service
        Workbook wb = employeeService.getImportTemplate();

        // 2. Ghi workbook ra ByteArrayOutputStream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close(); // Đóng workbook để giải phóng resource

        // 3. Cấu hình Header để trình duyệt hiểu đây là file tải về
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "employee_import_template.xlsx");

        return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
    }
}