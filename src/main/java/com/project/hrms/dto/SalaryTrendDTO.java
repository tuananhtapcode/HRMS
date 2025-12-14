package com.project.hrms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor // ✅ Dòng này tạo constructor(String, BigDecimal) để khớp với câu query
@NoArgsConstructor
public class SalaryTrendDTO {
    private String month;
    private BigDecimal total;
}