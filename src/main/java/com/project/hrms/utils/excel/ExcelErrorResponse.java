package com.project.hrms.utils.excel;

import lombok.*;

import java.util.List;

/**
 * Response đồng nhất cho import Excel.
 * - success = true/false
 * - importedCount = số bản ghi import thành công
 * - errors = danh sách lỗi chi tiết
 */
@Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class ExcelErrorResponse {
    private boolean success;
    private int importedCount;
    private List<ExcelError> errors;
}
