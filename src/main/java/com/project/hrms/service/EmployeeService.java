package com.project.hrms.service;

import com.project.hrms.dto.CreateEmployeeRequestDTO;
import com.project.hrms.dto.EmployeeDTO;
import com.project.hrms.dto.JobPositionDTO;
import com.project.hrms.exception.DataAlreadyExistsException;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.model.*;
import com.project.hrms.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @PostConstruct
    public void setupMapper() {
        modelMapper.typeMap(JobPositionDTO.class, JobPosition.class)
                .addMappings(mapper -> mapper.skip(JobPosition::setJobPositionId));
    }

    @Override
    public Employee create(EmployeeDTO employeeDTO) {

        if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
            throw new DataAlreadyExistsException("Email already exists: " + employeeDTO.getEmail());
        }

        if (employeeRepository.existsByPhoneNumber(employeeDTO.getPhoneNumber())) {
            throw new DataAlreadyExistsException("Phone number already exists: " + employeeDTO.getPhoneNumber());
        }

        Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
                .orElseThrow(() -> new DataNotFoundException("Department not found with id: " + employeeDTO.getDepartmentId()));

        JobPosition jobPosition = jobPositionRepository.findById(employeeDTO.getJobPositionId())
                .orElseThrow(() -> new DataNotFoundException("Job position not found with id: " + employeeDTO.getJobPositionId()));

        Employee employee = modelMapper.map(employeeDTO, Employee.class);

        employee.setDepartment(department);
        employee.setJobPosition(jobPosition);

        return employeeRepository.save(employee);
        // ✅ Tạo tài khoản tự động sau khi tạo nhân viên (Làm sau nayf neu can)
    }


    @Override
    public Employee update(Long id, EmployeeDTO employeeDTO) {
        Employee existing = getById(id);

        if (employeeDTO.getEmail() != null && !employeeDTO.getEmail().equals(existing.getEmail())) {
            if (employeeRepository.existsByEmail(employeeDTO.getEmail())) {
                throw new DataAlreadyExistsException("Email already exists: " + employeeDTO.getEmail());
            }
            existing.setEmail(employeeDTO.getEmail());
        }

        if (employeeDTO.getPhoneNumber() != null && !employeeDTO.getPhoneNumber().equals(existing.getPhoneNumber())) {
            if (employeeRepository.existsByPhoneNumber(employeeDTO.getPhoneNumber())) {
                throw new DataAlreadyExistsException("Phone number already exists: " + employeeDTO.getPhoneNumber());
            }
            existing.setPhoneNumber(employeeDTO.getPhoneNumber());
        }

        if (employeeDTO.getDepartmentId() != null) {
            Department department = departmentRepository.findById(employeeDTO.getDepartmentId())
                    .orElseThrow(() -> new DataNotFoundException("Department not found with id: " + employeeDTO.getDepartmentId()));
            existing.setDepartment(department);
        }

        if (employeeDTO.getJobPositionId() != null) {
            JobPosition jobPosition = jobPositionRepository.findById(employeeDTO.getJobPositionId())
                    .orElseThrow(() -> new DataNotFoundException("Job position not found with id: " + employeeDTO.getJobPositionId()));
            existing.setJobPosition(jobPosition);
        }

        modelMapper.map(employeeDTO, existing);
        return employeeRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        Employee existingEmployee = getById(id);
        existingEmployee.setStatus(Employee.EmployeeStatus.Terminated);
        employeeRepository.save(existingEmployee);
    }

    @Override
    public Employee getById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Employee not found with id: " + id));
    }

    @Override
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    @Override
    public List<Employee> findByDepartmentId(Long departmentId) {
        return employeeRepository.findByDepartment_DepartmentId(departmentId);
    }

    @Override
    public List<Employee> findByJobPositionId(Long jobPositionId) {
        return employeeRepository.findByJobPosition_JobPositionId(jobPositionId);
    }

//    @Override
//    public List<Employee> findByManagerId(Long managerId) {
//        return employeeRepository.findByManager_ManagerId(managerId);
//    }

    @Override
    public List<Employee> searchByName(String keyword) {
        return employeeRepository.findByFullNameContainingIgnoreCase(keyword);
    }

    @Override
    public List<Employee> findByStatus(String status) {
        return employeeRepository.findByStatus(status);
    }

    @Override
    public boolean existsByEmail(String email) {
        return employeeRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return employeeRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public Employee updateBankInfo(Long employeeId, String bankAccount) {
        Employee existing = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new DataNotFoundException("Employee not found with id: " + employeeId));
        existing.setBankAccount(bankAccount);
        return employeeRepository.save(existing);
    }

    @Override
    public boolean existsByBankAccount(String bankAccount) {
        return employeeRepository.existsByBankAccount(bankAccount);
    }

    @Override
    public boolean verifyBankAccount(String bankAccount) {
        // logic kiểm tra hợp lệ tài khoản ngân hàng (giả sử regex hoặc API bên thứ 3)
        return bankAccount != null && bankAccount.matches("\\d{10,16}");
    }

    @Override
    public List<String> getBankInfoHistory(Long employeeId) {
        // demo tạm, nếu có bảng lịch sử bank thì query ở đây
        return List.of("Techcombank - 0123456789", "Vietcombank - 0987654321");
    }

    @Override
    public Employee createEmployeeAndAccount(CreateEmployeeRequestDTO dto) throws Exception {
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
        newEmployee.setStatus(Employee.EmployeeStatus.valueOf("Active"));
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
    }

