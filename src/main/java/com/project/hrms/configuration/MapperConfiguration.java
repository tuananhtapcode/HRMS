package com.project.hrms.configuration;

import com.project.hrms.dto.ShiftAssignmentDTO;
import com.project.hrms.model.ShiftAssignment;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfiguration {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // GỘP TẤT CẢ VÀO ĐÂY
        modelMapper.addMappings(new PropertyMap<ShiftAssignment, ShiftAssignmentDTO>() {
            @Override
            protected void configure() {
                // 1. Các mapping ID cũ của bạn
                map(source.getShift().getShiftId()).setShiftId(null);
                map(source.getEmployee().getEmployeeId()).setEmployeeId(null);

                // 2. THÊM DÒNG NÀY ĐỂ SỬA LỖI
                // Chỉ định rõ: Lấy FullName từ Employee để gán vào EmployeeName của DTO
                // Cú pháp: map(nguồn).đích(null);
                map(source.getEmployee().getFullName()).setEmployeeName(null);
            }
        });

        // Xóa đoạn modelMapper.typeMap(...) ở dưới đi vì đã gộp lên trên rồi

        return modelMapper;
    }
}