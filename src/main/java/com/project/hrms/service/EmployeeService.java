package com.project.hrms.service;

import com.project.hrms.dto.CreateEmployeeRequestDTO;
import com.project.hrms.dto.EmployeeDTO;
import com.project.hrms.dto.EmployeeSearchRequest;
import com.project.hrms.exception.DataAlreadyExistsException;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.exception.InvalidParamException;
import com.project.hrms.model.*;
import com.project.hrms.model.enums.EmployeeStatus;
import com.project.hrms.model.enums.Gender;
import com.project.hrms.repository.*;
import com.project.hrms.response.EmployeeResponse;
import com.project.hrms.utils.excel.ExcelCellParser;
import com.project.hrms.utils.excel.ExcelError;
import com.project.hrms.utils.excel.ExcelErrorResponse;
import com.project.hrms.utils.excel.ExcelUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class EmployeeService implements IEmployeeService {
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final JobPositionRepository jobPositionRepository;
    private final ModelMapper modelMapper;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    // Formatter mặc định cho ngày tháng
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    // Bộ format linh hoạt, không cần vòng for
    private static final DateTimeFormatter FLEX_DATE_FORMATTER =
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("[dd-MM-yyyy][dd/MM/yyyy][yyyy-MM-dd][d/M/yyyy][M/d/yyyy]")
                    .toFormatter();
    /**
     * PHẦN 1: MANUAL MAPPING (Thay thế ModelMapper)
     * Giúp code chạy nhanh hơn, dễ kiểm soát và tránh lỗi cấu hình
     * Convert Employee entity sang EmployeeDTO
     */
//    private EmployeeDTO toDTO(Employee employee) {
//        if (employee == null) return null;
//
//        EmployeeDTO dto = new EmployeeDTO();
//        //map các trường cơ bản
//        dto.setEmployeeId(employee.getEmployeeId());
//        dto.setEmployeeCode(employee.getEmployeeCode());
//        dto.setFullName(employee.getFullName());
//        dto.setEmail(employee.getEmail());
//        dto.setPhoneNumber(employee.getPhoneNumber());
//        dto.setGender(employee.getGender());
//        dto.setDateOfBirth(employee.getDateOfBirth());
//        dto.setHireDate(employee.getHireDate());
//        dto.setAddress(employee.getAddress());
//        dto.setBankName(employee.getBankName());
//        dto.setBankAccount(employee.getBankAccount());
//        dto.setBankNumber(employee.getBankNumber());
//        dto.setStatus(employee.getStatus());
//
//        // Map foreign keys
//        if (employee.getDepartment() != null) {
//            dto.setDepartmentId(employee.getDepartment().getDepartmentId());
//        }
//
//        if (employee.getJobPosition() != null) {
//            dto.setJobPositionId(employee.getJobPosition().getJobPositionId());
//        }
//
//        return dto;
//    }

    /**
     * Convert EmployeeDTO sang Employee entity (cho create)
     */
    private Employee toEntityForCreate(EmployeeDTO dto) {
        if (dto == null) return null;

        Employee employee = new Employee();
        employee.setEmployeeCode(dto.getEmployeeCode());
        employee.setFullName(dto.getFullName());
        employee.setEmail(dto.getEmail());
        employee.setPhoneNumber(dto.getPhoneNumber());
        employee.setGender(dto.getGender());
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setHireDate(dto.getHireDate());
        employee.setAddress(dto.getAddress());
        employee.setBankName(dto.getBankName());
        employee.setBankAccount(dto.getBankAccount());
        employee.setBankNumber(dto.getBankNumber());

        // Mặc định Active khi tạo mới
        employee.setStatus(dto.getStatus() != null ? dto.getStatus() : EmployeeStatus.ACTIVE);

        // Load Department & JobPosition từ DB
        if (dto.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new DataNotFoundException("Department not found: " + dto.getDepartmentId()));
            employee.setDepartment(dept);
        }
        if (dto.getJobPositionId() != null) {
            JobPosition job = jobPositionRepository.findById(dto.getJobPositionId())
                    .orElseThrow(() -> new DataNotFoundException("JobPosition not found: " + dto.getJobPositionId()));
            employee.setJobPosition(job);
        }

        return employee;
    }

    /**
     * Update Employee entity từ EmployeeDTO (chỉ update các field không null)
     */
    private void updateEntityFromDTO(Employee employee, EmployeeDTO dto) {
        if (dto == null || employee == null) return;

//        if (dto.getEmployeeCode() != null) {
//            employee.setEmployeeCode(dto.getEmployeeCode());
//        }
        if (dto.getFullName() != null) {
            employee.setFullName(dto.getFullName());
        }
        // Kiểm tra Email/Phone unique được xử lý ở method update chính
        if (dto.getEmail() != null) {
            employee.setEmail(dto.getEmail());
        }
        if (dto.getPhoneNumber() != null) {
            employee.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getGender() != null) {
            employee.setGender(dto.getGender());
        }
        if (dto.getDateOfBirth() != null) {
            employee.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getHireDate() != null) {
            employee.setHireDate(dto.getHireDate());
        }
        if (dto.getAddress() != null) {
            employee.setAddress(dto.getAddress());
        }
        if (dto.getBankName() != null) {
            employee.setBankName(dto.getBankName());
        }
        if (dto.getBankAccount() != null) {
            employee.setBankAccount(dto.getBankAccount());
        }
        if (dto.getBankNumber() != null) {
            employee.setBankNumber(dto.getBankNumber());
        }
        if (dto.getStatus() != null) {
            employee.setStatus(dto.getStatus());
        }

        // Update Department nếu có
        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new DataNotFoundException("Department not found with id: " + dto.getDepartmentId()));
            employee.setDepartment(department);
        }

        // Update JobPosition nếu có
        if (dto.getJobPositionId() != null) {
            JobPosition jobPosition = jobPositionRepository.findById(dto.getJobPositionId())
                    .orElseThrow(() -> new DataNotFoundException("Job position not found with id: " + dto.getJobPositionId()));
            employee.setJobPosition(jobPosition);
        }
    }

    // ==================================================================================
    // PHẦN 2: CRUD CHÍNH
    // ==================================================================================

    @Override
    public EmployeeResponse getDetail(Long id) {
        Employee employee = getById(id);
        return EmployeeResponse.fromEmployee(employee);
    }

    @Override
    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Employee not found with id: " + id));
    }

    @Override
    public Employee create(EmployeeDTO employeeDTO) {

        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new DataAlreadyExistsException("Email already exists: " + employeeDTO.getEmail());
        }

        if (employeeRepository.existsByPhoneNumber(employeeDTO.getPhoneNumber())) {
            throw new DataAlreadyExistsException("Phone number already exists: " + employeeDTO.getPhoneNumber());
        }

//        Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
//                .orElseThrow(() -> new DataNotFoundException("Department not found with id: " + employeeDTO.getDepartmentId()));
//        JobPosition jobPosition = jobPositionRepository.findById(employeeDTO.getJobPositionId())
//                .orElseThrow(() -> new DataNotFoundException("Job position not found with id: " + employeeDTO.getJobPositionId()));
//        Employee employee = modelMapper.map(employeeDTO, Employee.class);
//        employee.setDepartment(department);
//        employee.setJobPosition(jobPosition);

        // Convert DTO sang Entity
        Employee employee = toEntityForCreate(employeeDTO);

        return employeeRepository.save(employee);
        // ✅ Tạo tài khoản tự động sau khi tạo nhân viên (Làm sau nayf neu can)
    }


    @Override
    public Employee update(Long id, EmployeeDTO employeeDTO) {
        Employee existing = getById(id);

        // 1. Validate Email change
        if (employeeDTO.getEmail() != null && !employeeDTO.getEmail().equals(existing.getEmail())) {
            if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
                throw new DataAlreadyExistsException("Email already exists: " + employeeDTO.getEmail());
            }
            existing.setEmail(employeeDTO.getEmail());
        }

        // 2. Validate Phone change
        if (employeeDTO.getPhoneNumber() != null && !employeeDTO.getPhoneNumber().equals(existing.getPhoneNumber())) {
            if (employeeRepository.existsByPhoneNumber(employeeDTO.getPhoneNumber())) {
                throw new DataAlreadyExistsException("Phone number already exists: " + employeeDTO.getPhoneNumber());
            }
            existing.setPhoneNumber(employeeDTO.getPhoneNumber());
        }

//        if (employeeDTO.getDepartmentId() != null) {
//            Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
//                    .orElseThrow(() -> new DataNotFoundException("Department not found with id: " + employeeDTO.getDepartmentId()));
//            existing.setDepartment(department);
//        }
//        if (employeeDTO.getJobPositionId() != null) {
//            JobPosition jobPosition = jobPositionRepository.findById(employeeDTO.getJobPositionId())
//                    .orElseThrow(() -> new DataNotFoundException("Job position not found with id: " + employeeDTO.getJobPositionId()));
//            existing.setJobPosition(jobPosition);
//        }
//        modelMapper.map(employeeDTO, existing);

        // 3. Update & Save (Manual mapping)
        updateEntityFromDTO(existing, employeeDTO);

        return employeeRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Employee existingEmployee = getById(id);
        // Soft delete: Chỉ chuyển trạng thái sang TERMINATED
        if (existingEmployee.getStatus() == EmployeeStatus.TERMINATED) {
            throw new InvalidParamException("Employee is already terminated");
        }
        existingEmployee.setStatus(EmployeeStatus.TERMINATED);
        employeeRepository.save(existingEmployee);
    }

    @Override
    public Page<EmployeeResponse> getAll(PageRequest pageRequest) {
        return employeeRepository.findAll(pageRequest)
                .map(EmployeeResponse::fromEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> search(EmployeeSearchRequest request, Pageable pageable) {

        Specification<Employee> spec = EmployeeSpecification.filter(request);

        // Thực hiện truy vấn với Specification và phân trang
        Page<Employee> employeePage = employeeRepository.findAll(spec, pageable);
        //cách 1
//        return page.map(emp -> modelMapper.map(emp, EmployeeDTO.class));
        //cách 2
        return employeePage.map(EmployeeResponse::fromEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> quickFilter(String status, Long departmentId, String gender, Pageable pageable) {
        EmployeeSearchRequest req = new EmployeeSearchRequest();
        req.setStatus(status);
        req.setDepartmentId(departmentId);
        req.setGender(gender != null ? gender.toUpperCase() : null);

        Specification<Employee> spec = EmployeeSpecification.filter(req);
        return employeeRepository.findAll(spec, pageable)
                .map(EmployeeResponse::fromEmployee);
    }

    @Override
    public Employee createEmployeeAndAccount(CreateEmployeeRequestDTO dto) throws Exception {
        // Logic tạo Employee kèm Account (Transactional đảm bảo cả 2 cùng thành công hoặc cùng thất bại)
        if (accountRepository.existsByEmail(dto.getEmail())) {
            throw new DataAlreadyExistsException("Email đã được sử dụng");
        }
        if (employeeRepository.existsByEmployeeCode(dto.getEmployeeCode())) {
            throw new DataAlreadyExistsException("Mã nhân viên đã tồn tại");
        }

        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy Department"));
        JobPosition job = jobPositionRepository.findById(dto.getJobPositionId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy JobPosition"));
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new DataNotFoundException("Không tìm thấy Role"));

        // 1. Tạo Employee
        Employee newEmployee = new Employee();
        newEmployee.setEmployeeCode(dto.getEmployeeCode());
        newEmployee.setFullName(dto.getFullName());
        newEmployee.setEmail(dto.getEmail());
        newEmployee.setPhoneNumber(dto.getPhoneNumber());
        newEmployee.setDepartment(dept);
        newEmployee.setJobPosition(job);
        newEmployee.setStatus(EmployeeStatus.valueOf("ACTIVE"));
        Employee savedEmployee = employeeRepository.save(newEmployee);

        // 2. Tạo Token Kích hoạt
        String token = UUID.randomUUID().toString();

        // 3. Tạo Account
        Account newAccount = new Account();
        newAccount.setUsername(dto.getEmail());
        newAccount.setEmail(dto.getEmail());
        newAccount.setPassword(null); // Rỗng
        newAccount.setRole(role);
        newAccount.setEmployee(savedEmployee); // Liên kết
        newAccount.setIsActive(false); // Chưa kích hoạt
        newAccount.setActivationToken(token);
        newAccount.setActivationTokenExpires(LocalDateTime.now().plusDays(3)); // Hạn 3 ngày

        accountRepository.save(newAccount);

        // 4. Gửi Email
        emailService.sendActivationEmail(savedEmployee.getEmail(), token);

        return savedEmployee;
    }

    // IMPORT Excel
    // ================================================================
    @Override
    public ExcelErrorResponse importEmployees(MultipartFile file) {
        List<ExcelError> errors = new ArrayList<>();
        List<Employee> toSave = new ArrayList<>();

        try (InputStream is = file.getInputStream()) {

            ExcelUtils.read(is, 1, row -> { // Bỏ row header
                try {
                    Employee emp = parseRow(row);
                    toSave.add(emp);
                } catch (Exception ex) {
                    // Nếu lỗi dòng nào, ghi lại dòng đó để báo cáo
                    errors.add(ExcelError.builder()
                            .rowIndex(row.getRowNum() + 1) // +1 cho user nhìn số dòng Excel
                            .field("UNKNOWN")
                            .message(ex.getMessage())
                            .build());
                }
            });
            // Nếu có lỗi, không lưu gì cả, trả về danh sách lỗi
            if (!errors.isEmpty()) {
                return ExcelErrorResponse.builder()
                        .success(false)
                        .errors(errors)
                        .build();
            }

            // Nếu ok hết -> Lưu DB
            employeeRepository.saveAll(toSave);

            return ExcelErrorResponse.builder()
                    .success(true)
                    .importedCount(toSave.size())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Không thể import file" + e.getMessage(), e);
        }
    }

    /**
     * Map từng cột Excel sang fields của Employee
     * Thứ tự cột:
     * 0: Employee Code
     * 1: Full Name
     * 2: Gender (MALE/FEMALE/OTHER)
     * 3: Date of Birth (dd-MM-yyyy)
     * 4: Email
     * 5: Phone
     * 6: Hire Date (dd-MM-yyyy)
     * 7: Department ID
     * 8: Job Position ID
     * 9: Bank Name
     * 10: Bank Account
     * 11: Bank Number
     * 12: Address
     * 13: Status (ACTIVE, INACTIVE, ON_LEAVE)
     */
    private Employee parseRow(Row row) {
        Employee emp = new Employee();

        // 1. Basic Fields
        String code = ExcelCellParser.getString(row.getCell(0));
        if (code == null || code.isBlank()) throw new RuntimeException("Mã nhân viên thiếu");
        emp.setEmployeeCode(code);

        String fullName = ExcelCellParser.getString(row.getCell(1));
        if (fullName == null || fullName.isBlank()) throw new RuntimeException("Họ tên thiếu");
        emp.setFullName(fullName);

        // 2. Gender (Map thông minh: Nam/Nữ/Male/Female...)
        emp.setGender(mapGenderSmart(ExcelCellParser.getString(row.getCell(2))));

        // 3. Dates (Thử nhiều định dạng: dd-MM-yyyy, yyyy-MM-dd...)
        emp.setDateOfBirth(parseDateSmart(ExcelCellParser.getString(row.getCell(3))));
        emp.setHireDate(parseDateSmart(ExcelCellParser.getString(row.getCell(6))));

        // 4. Contact
        emp.setEmail(ExcelCellParser.getString(row.getCell(4)));

        // SỬA LỖI: Xử lý số điện thoại trước khi set
        String rawPhone = ExcelCellParser.getString(row.getCell(5));
        if (rawPhone != null && !rawPhone.isBlank()) {
            // 1. Xóa các ký tự thừa (khoảng trắng, gạch ngang, dấu chấm...) chỉ giữ lại số và dấu +
            String cleanPhone = rawPhone.replaceAll("[^0-9+]", "");

            // 2. Nếu không bắt đầu bằng '0' và không bắt đầu bằng '+84', tự động thêm '0'
            if (!cleanPhone.startsWith("0") && !cleanPhone.startsWith("+84")) {
                cleanPhone = "0" + cleanPhone;
            }

            emp.setPhoneNumber(cleanPhone);
        }

        // 5. Department (Tìm theo ID hoặc Tên)
        String deptInput = ExcelCellParser.getString(row.getCell(7));
        if (deptInput != null) {
            emp.setDepartment(findDepartmentSmart(deptInput));
        }

        // 6. JobPosition (Tìm theo ID hoặc Tên)
        String jobInput = ExcelCellParser.getString(row.getCell(8));
        if (jobInput != null) {
            emp.setJobPosition(findJobPositionSmart(jobInput));
        }

        // 7. Bank Info & Address
        emp.setBankName(ExcelCellParser.getString(row.getCell(9)));
        emp.setBankAccount(ExcelCellParser.getString(row.getCell(10)));
        emp.setBankNumber(ExcelCellParser.getString(row.getCell(11)));
        emp.setAddress(ExcelCellParser.getString(row.getCell(12)));

        // 8. Status
        emp.setStatus(mapStatusSmart(ExcelCellParser.getString(row.getCell(13))));

        return emp;
    }

    // --- Helper Methods cho Excel Smart ---

    private LocalDate parseDateSmart(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        dateStr = dateStr.trim();

        // 1. Xử lý trường hợp Excel trả về số (VD: "43840.0" hoặc "43840")
        try {
            double excelDateNum = Double.parseDouble(dateStr);
            // Dùng DateUtil của POI để convert số thành Java Date, sau đó sang LocalDate
            // Lưu ý: Cần import org.apache.poi.ss.usermodel.DateUtil;
            java.util.Date javaDate = org.apache.poi.ss.usermodel.DateUtil.getJavaDate(excelDateNum);
            return javaDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        } catch (NumberFormatException ignored) {
            // Không phải số -> đi tiếp xuống dưới để parse chuỗi
        }

        // 2. Xử lý các định dạng chuỗi ngày tháng thông thường
        List<String> formats = Arrays.asList(
                "dd-MM-yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "d/M/yyyy", "M/d/yyyy"
        );

        for (String fmt : formats) {
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(fmt));
            } catch (DateTimeParseException ignored) {}
        }

        throw new RuntimeException("Ngày không hợp lệ: " + dateStr);
    }

    private Gender mapGenderSmart(String input) {
        if (input == null) return null;
        String s = input.trim().toUpperCase();
        if (s.equals("NAM") || s.equals("MALE") || s.equals("M")) return Gender.MALE;
        if (s.equals("NỮ") || s.equals("NU") || s.equals("FEMALE") || s.equals("F")) return Gender.FEMALE;
        return Gender.OTHER;
    }

    private EmployeeStatus mapStatusSmart(String input) {
        if (input == null) return EmployeeStatus.ACTIVE; // Mặc định
        String s = input.trim().toUpperCase();
        if (s.contains("ACTIVE") || s.contains("LÀM")) return EmployeeStatus.ACTIVE;
        if (s.contains("INACTIVE") || s.contains("NGHỈ")) return EmployeeStatus.TERMINATED;
        if (s.contains("LEAVE") || s.contains("PHÉP")) return EmployeeStatus.ON_LEAVE;
        try { return EmployeeStatus.valueOf(s); } catch (Exception e) { return EmployeeStatus.ACTIVE; }
    }

    private Department findDepartmentSmart(String input) {
        if (input == null || input.isBlank()) return null;
        // Case 1: Input là số ID
        try {
            Long id = Long.parseLong(input.trim());
            return departmentRepository.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            // Case 2: Input là Tên -> Tìm trong list (Tạm thời load all để tìm, tối ưu sau bằng query)
            return departmentRepository.findAll().stream()
                    .filter(d -> d.getName().equalsIgnoreCase(input.trim()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ban: " + input));
        }
    }

    private JobPosition findJobPositionSmart(String input) {
        if (input == null || input.isBlank()) return null;
        try {
            Long id = Long.parseLong(input.trim());
            return jobPositionRepository.findById(id).orElse(null);
        } catch (NumberFormatException e) {
            return jobPositionRepository.findAll().stream()
                    .filter(j -> j.getName().equalsIgnoreCase(input.trim()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy vị trí: " + input));
        }
    }

    // ================================================================
    // EXPORT Excel
    // ================================================================
    @Override
    public Workbook exportEmployees() {
        List<Employee> list = employeeRepository.findAll();
        Workbook wb = ExcelUtils.createWorkbook();
        var sheet = ExcelUtils.createSheetWithHeader(wb, "Employees",
                "Employee Code", "Full Name", "Gender", "Date of Birth", "Email", "Phone",
                "Hire Date", "Department ID", "Job Position ID",
                "Bank Name", "Bank Account", "Bank Number", "Address", "Status"
        );

        int rowIndex = 1;
        for (Employee e : list) {
            var row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(e.getEmployeeCode());
            row.createCell(1).setCellValue(e.getFullName());
            row.createCell(2).setCellValue(e.getGender() != null ? e.getGender().name() : "");
            row.createCell(3).setCellValue(e.getDateOfBirth() != null ? e.getDateOfBirth().format(dateFormatter) : "");
            row.createCell(4).setCellValue(e.getEmail() != null ? e.getEmail() : "");
            row.createCell(5).setCellValue(e.getPhoneNumber() != null ? e.getPhoneNumber() : "");
            row.createCell(6).setCellValue(e.getHireDate() != null ? e.getHireDate().format(dateFormatter) : "");
            // Export tên cho dễ đọc
            row.createCell(7).setCellValue(e.getDepartment() != null ? e.getDepartment().getName() : "");
            row.createCell(8).setCellValue(e.getJobPosition() != null ? e.getJobPosition().getName() : "");
//            row.createCell(7).setCellValue(e.getDepartment() != null ? e.getDepartment().getDepartmentId() : 0);
//            row.createCell(8).setCellValue(e.getJobPosition() != null ? e.getJobPosition().getJobPositionId() : 0);
            row.createCell(9).setCellValue(e.getBankName() != null ? e.getBankName() : "");
            row.createCell(10).setCellValue(e.getBankAccount() != null ? e.getBankAccount() : "");
            row.createCell(11).setCellValue(e.getBankNumber() != null ? e.getBankNumber() : "");
            row.createCell(12).setCellValue(e.getAddress() != null ? e.getAddress() : "");
            row.createCell(13).setCellValue(e.getStatus() != null ? e.getStatus().name() : "");
        }

        return wb;
    }

    // ==================================================================================
    // PHẦN 4: TẠO TEMPLATE CÓ DROPDOWN
    // ==================================================================================

    @Override
    public Workbook getImportTemplate() {
        Workbook wb = new XSSFWorkbook(); // Dùng .xlsx
        Sheet sheet = wb.createSheet("Template");

        // 1. Tạo Header với style nổi bật
        Row header = sheet.createRow(0);
        String[] headers = {
                "Mã NV (*)", "Họ Tên (*)", "Giới tính (Chọn)", "Ngày sinh (dd-mm-yyyy)", "Email", "SĐT",
                "Ngày vào làm", "Phòng ban (Chọn)", "Vị trí (Chọn)",
                "Tên NH", "Tên TK", "Số TK", "Địa chỉ", "Trạng thái (Chọn)"
        };

        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
            sheet.setColumnWidth(i, 5000); // Mở rộng cột
        }

        // 2. Dữ liệu mẫu dòng 2
        Row ex = sheet.createRow(1);
        ex.createCell(0).setCellValue("E001");
        ex.createCell(1).setCellValue("Nguyễn Văn Mẫu");
        ex.createCell(2).setCellValue("Nam");
        ex.createCell(3).setCellValue("01-01-2000");
        ex.createCell(7).setCellValue("Chọn phòng ban từ list..."); // Gợi ý

        // 3. TẠO DROPDOWN (Data Validation)
        // Kỹ thuật: Tạo 1 sheet ẩn chứa dữ liệu list, sau đó tham chiếu tới
        Sheet hidden = wb.createSheet("DATA_HIDDEN");

        // Lấy list tên từ DB
        List<String> depts = departmentRepository.findAll().stream().map(Department::getName).toList();
        List<String> jobs = jobPositionRepository.findAll().stream().map(JobPosition::getName).toList();
        List<String> genders = Arrays.asList("Nam", "Nữ", "Khác");
        List<String> status = Arrays.asList("ACTIVE", "INACTIVE", "ON_LEAVE");

        // Ghi data vào sheet ẩn
        int maxRow = Math.max(depts.size(), jobs.size());
        maxRow = Math.max(maxRow, 10); // ít nhất 10 dòng
        for (int i = 0; i < maxRow; i++) {
            Row r = hidden.createRow(i);
            if (i < genders.size()) r.createCell(0).setCellValue(genders.get(i));
            if (i < status.size()) r.createCell(1).setCellValue(status.get(i));
            if (i < depts.size()) r.createCell(2).setCellValue(depts.get(i));
            if (i < jobs.size()) r.createCell(3).setCellValue(jobs.get(i));
        }

        // Tạo Named Ranges (Vùng đặt tên) để công thức validation gọn hơn
        createNamedRange(wb, "GenderList", "DATA_HIDDEN", 0, genders.size());
        createNamedRange(wb, "StatusList", "DATA_HIDDEN", 1, status.size());
        // Nếu DB chưa có phòng ban nào thì không tạo range lỗi
        if (!depts.isEmpty()) createNamedRange(wb, "DeptList", "DATA_HIDDEN", 2, depts.size());
        if (!jobs.isEmpty()) createNamedRange(wb, "JobList", "DATA_HIDDEN", 3, jobs.size());

        // Áp dụng Validation vào sheet chính (Từ dòng 2 đến 1000)
        addValidation(sheet, "GenderList", 2, 1, 1000);   // Cột Giới tính (C)
        addValidation(sheet, "StatusList", 13, 1, 1000);  // Cột Status (N)
        if (!depts.isEmpty()) addValidation(sheet, "DeptList", 7, 1, 1000); // Cột Dept (H)
        if (!jobs.isEmpty()) addValidation(sheet, "JobList", 8, 1, 1000);   // Cột Job (I)

        // Ẩn sheet data đi cho gọn
        wb.setSheetHidden(wb.getSheetIndex(hidden), true);

        return wb;
    }

    // Helper tạo Named Range
    private void createNamedRange(Workbook wb, String name, String sheetName, int col, int rows) {
        Name namedRange = wb.createName();
        namedRange.setNameName(name);
        String colChar = org.apache.poi.ss.util.CellReference.convertNumToColString(col);
        // Ví dụ: DATA_HIDDEN!$A$1:$A$5
        String ref = sheetName + "!$" + colChar + "$1:$" + colChar + "$" + Math.max(rows, 1);
        namedRange.setRefersToFormula(ref);
    }

    // Helper áp dụng Validation
    private void addValidation(Sheet sheet, String formulaName, int col, int firstRow, int lastRow) {
        DataValidationHelper helper = sheet.getDataValidationHelper();
        DataValidationConstraint constraint = helper.createFormulaListConstraint(formulaName);
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, lastRow, col, col);
        DataValidation validation = helper.createValidation(constraint, regions);
        validation.setShowErrorBox(true);
        validation.createErrorBox("Sai dữ liệu", "Vui lòng chọn từ danh sách.");
        sheet.addValidationData(validation);
    }
}

