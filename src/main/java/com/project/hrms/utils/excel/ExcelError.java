package com.project.hrms.utils.excel;

import lombok.*;

@Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class ExcelError {
    private int rowIndex;  // dòng gây lỗi
    private String field;  // cột gây lỗi
    private String message; // mô tả lỗi
}
