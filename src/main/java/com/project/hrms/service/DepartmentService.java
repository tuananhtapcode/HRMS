package com.project.hrms.service;

import com.project.hrms.dto.DepartmentDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.model.Department;
import com.project.hrms.model.Employee;
import com.project.hrms.repository.DepartmentRepository;
import com.project.hrms.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DepartmentService implements IDepartment {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ModelMapper modelMapper;
    @Override
    public Department createDepartment(DepartmentDTO DepartmentDTO) {
        // Bật strict mode để phát hiện field mismatch
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);

        // Map DTO -> Entity
        Department department = modelMapper.map(DepartmentDTO, Department.class);


        // Debug log để kiểm tra giá trị sau khi map
        System.out.println("Mapped DepartmentService = " + department);

        return departmentRepository.save(department);
    }


    @Override
    public Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(()->new DataNotFoundException("Cannot find Department with id: "+id));
    }

    @Override
    public Department updateDepartment(Long id, DepartmentDTO departmentDTO) {
        //tìm xem order detail có tồn tại ko roi moi lam cai khac
        Department existingDepartment= departmentRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find order detail with id: "+id));
        existingDepartment.setCode(departmentDTO.getCode());
        existingDepartment.setName(departmentDTO.getName());
        existingDepartment.setDescription(departmentDTO.getDescription());

        Employee existingEmployee = employeeRepository.findById(departmentDTO.getManagerId())
                .orElseThrow(() -> new DataNotFoundException("Cannot find manager with id: "+departmentDTO.getManagerId()));
        existingDepartment.setManager(existingEmployee);
        return departmentRepository.save(existingDepartment);
    }

    @Override
    public void deleteById(Long id) {
        departmentRepository.deleteById(id);
    }

    @Override
    public List<Department> findByManager_EmployeeId(Long orderId) {
        return departmentRepository.findByManager_EmployeeId(orderId);
    }
}
