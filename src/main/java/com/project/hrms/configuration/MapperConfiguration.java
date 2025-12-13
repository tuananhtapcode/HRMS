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
        modelMapper.addMappings(new PropertyMap<ShiftAssignment, ShiftAssignmentDTO>() {
            @Override
            protected void configure() {
                map(source.getShift().getShiftId()).setShiftId(null);

                map(source.getEmployee().getEmployeeId()).setEmployeeId(null);
            }
        });
        return modelMapper;
    }
}