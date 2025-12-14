package com.project.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor // ✅ Dòng này sẽ tạo constructor: new FundStructureDTO(String name, BigDecimal amount)
@NoArgsConstructor  // ✅ Dòng này tạo constructor mặc định (nếu cần cho JSON/JPA)
public class FundStructureDTO {
    private String label;
    private BigDecimal value;
}