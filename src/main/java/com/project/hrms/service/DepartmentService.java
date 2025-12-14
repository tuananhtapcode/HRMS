package com.project.hrms.service;

import com.project.hrms.dto.AccountDTO;
import com.project.hrms.dto.DepartmentDTO;
import com.project.hrms.exception.DataAlreadyExistsException;
import com.project.hrms.model.Account;
import com.project.hrms.model.Department;
import com.project.hrms.model.JobPosition;
import com.project.hrms.repository.DepartmentRepository;
import com.project.hrms.repository.EmployeeRepository;
import com.project.hrms.response.AccountResponse;
import com.project.hrms.response.DepartmentResponse;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class DepartmentService implements IDepartmentService {
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;

    @PostConstruct
    public void setupMapper(){
        modelMapper.typeMap(DepartmentDTO.class, Department.class)
                .addMappings(mapper -> mapper.skip(Department::setDepartmentId));
    }

    @Override
    @Transactional
    public Department create(DepartmentDTO newDepartmentDTO) {
        // Kiểm tra trùng tên
        if (departmentRepository.existsByName(newDepartmentDTO.getName())) {
            throw new DataAlreadyExistsException("Department name already exists");
        }

        // Kiểm tra trùng code
        if (departmentRepository.existsByCode(newDepartmentDTO.getCode())) {
            throw new DataAlreadyExistsException("Department code already exists");
        }

        Department department = modelMapper.map(newDepartmentDTO, Department.class);

        // Set manager nếu có
        if (newDepartmentDTO.getManagerId() != null) {
            employeeRepository.findById(newDepartmentDTO.getManagerId())
                    .ifPresent(department::setManager);
        }
        return departmentRepository.save(department);
    }

    @Override
    @Transactional
    public Department update(Long id, DepartmentDTO departmentDTO) {
        Department existingDepartment = getById(id);

        modelMapper.map(departmentDTO, existingDepartment);

        if (departmentDTO.getManagerId() != null) {
            employeeRepository.findById(departmentDTO.getManagerId())
                    .ifPresent(existingDepartment::setManager);
        }

        return departmentRepository.save(existingDepartment);
    }

    @Override
    @Transactional
    public void delete(Long departmentId) {
        Department department = getById(departmentId);
        department.setIsActive(false);
        departmentRepository.save(department);
    }

    @Override
    public Page<DepartmentResponse> getAllPaged(PageRequest pageRequest) {
        return departmentRepository.findAll(pageRequest).map(DepartmentResponse::fromDepartment);
    }
    @Override
    public Department getById(Long departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Department not found with id: " + departmentId));
    }

    @Override
    public List<Department> searchByName(String name) {
        return departmentRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    public Department findByManager_EmployeeId(Long managerId) {
        return departmentRepository.findByManager_EmployeeId(managerId);
//        return null;
    }

    // --- THÊM PHẦN LOGIC THỐNG KÊ Ở ĐÂY ---
    @Override
    public Map<String, Long> getDepartmentStats() {
        // Gọi query từ Repository
        List<Object[]> results = departmentRepository.countEmployeesPerDepartment();

        // Chuyển đổi List<Object[]> thành Map<String, Long>
        Map<String, Long> stats = new HashMap<>();
        for (Object[] row : results) {
            String deptName = (String) row[0];
            Long count = (Long) row[1];
            stats.put(deptName, count);
        }

        return stats;
    }

    @Override
    public ByteArrayInputStream exportToExcel() {
        List<Department> list = departmentRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Department");
            Row header = sheet.createRow(0);

            String[] columns = {"department Id", "Code", "Name", "Description", "Manager", "Active"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (Department department : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(department.getDepartmentId());
                row.createCell(1).setCellValue(department.getCode());
                row.createCell(2).setCellValue(department.getName());
                row.createCell(3).setCellValue(department.getDescription() != null ? department.getDescription() : "");
                row.createCell(4).setCellValue(department.getManager() != null ? department.getManager().getFullName() : "");
                row.createCell(7).setCellValue(Boolean.TRUE.equals(department.getIsActive()) ? "Yes" : "No");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to export Excel: " + e.getMessage());
        }
    }
}
