package com.project.hrms.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * 🎯 API Response chuẩn HRMS
 * Dùng chung cho tất cả response (thành công và lỗi)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private Integer status;
    private String error;

    // ✅ Response khi thành công
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .status(200)
                .error(null)
                .build();
    }

    // ❌ Response khi thất bại
    public static <T> ApiResponse<T> fail(HttpStatus status, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .build();
    }

//    {
//        "success": true,
//            "message": "Tạo chức danh thành công",
//            "data": {
//        "id": 3,
//                "code": "DEV01",
//                "name": "Lập trình viên Backend",
//                "minSalary": 10000000,
//                "maxSalary": 15000000,
//                "isActive": true
//    },
//        "timestamp": "2025-10-23T16:41:12.493",
//            "status": 200,
//            "error": null
//    }

//    {
//        "success": false,
//            "message": "Không tìm thấy chức danh có ID = 99",
//            "data": null,
//            "timestamp": "2025-10-23T16:42:10.312",
//            "status": 404,
//            "error": "Not Found"
//    }

}
