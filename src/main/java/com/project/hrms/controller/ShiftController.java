package com.project.hrms.controller;

import com.project.hrms.dto.ShiftDTO;
import com.project.hrms.response.ApiResponse;
import com.project.hrms.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/timeworking/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    // API tạo ca làm việc
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShiftDTO>> createShift(
            @Valid @RequestBody ShiftDTO dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
            return ResponseEntity.badRequest().body(ApiResponse.fail(HttpStatus.BAD_REQUEST, errorMessage));
        }

        ShiftDTO result = shiftService.createShift(dto);
        return ResponseEntity.ok(ApiResponse.success("Tạo ca làm việc thành công", result));
    }

    // API cập nhật ca
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ShiftDTO>> updateShift(
            @PathVariable Long id,
            @Valid @RequestBody ShiftDTO dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            String errorMessage = Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage();
            return ResponseEntity.badRequest().body(ApiResponse.fail(HttpStatus.BAD_REQUEST, errorMessage));
        }

        ShiftDTO result = shiftService.updateShift(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật ca làm việc thành công", result));
    }

    // API xóa ca
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteShift(@PathVariable Long id) {
        shiftService.deleteShift(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa ca làm việc thành công", null));
    }

    // API lấy danh sách ca
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<ShiftDTO>>> getAllShifts() {
        List<ShiftDTO> list = shiftService.getAllShifts();
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách ca thành công", list));
    }

    // API lấy chi tiết 1 ca
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<ShiftDTO>> getShiftById(@PathVariable Long id) {
        ShiftDTO result = shiftService.getShiftById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết ca thành công", result));
    }
}