package com.project.hrms.controller;

import com.project.hrms.dto.JobPositionDTO;
import com.project.hrms.model.JobPosition;
import com.project.hrms.response.*;
import com.project.hrms.service.IJobPositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/job-positions")
@RequiredArgsConstructor
public class JobPositionController {
    private final IJobPositionService jobPositionService;

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody JobPositionDTO request,
                                                                   BindingResult bindingResult) {
        try {
            if(bindingResult.hasErrors()) {
                List<String> errorMessages = bindingResult.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errorMessages);
            }
            JobPositionResponse newJobPosition = jobPositionService.create(request);
            return ResponseEntity.ok(ApiResponse.success("Created job position successfully", newJobPosition));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody JobPositionDTO jobPosition) {
        JobPositionResponse response = jobPositionService.update(id, jobPosition);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật chức danh thành công", response));
//        return ResponseEntity.ok(jobPositionService.update(id, jobPosition));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        jobPositionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Deleted job position (soft delete) successfully", null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            JobPositionResponse jobPosition = jobPositionService.getById(id);
            return ResponseEntity.ok(ApiResponse.success("Fetched job position successfully", jobPosition));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page, limit,
                Sort.by("createdAt").descending());
        //lay danh sach tat ca vi tri cong viec da chia trang

        Page<JobPositionResponse> jobPositionResponsePage =  jobPositionService.getAll(pageRequest);
        JobPositionListResponse listResponse = JobPositionListResponse.builder()
                .jobPositionResponses(jobPositionResponsePage.getContent())
                .totalPages(jobPositionResponsePage.getTotalPages())
                .totalElements(jobPositionResponsePage.getTotalElements())
                .build();

        return ResponseEntity.ok(ApiResponse.success("List all JobPosition: ", listResponse));
    }

    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportToExcel() {
        ByteArrayInputStream excel = jobPositionService.exportToExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=job_positions_" + LocalDate.now() + ".xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(excel));
    }

    //Search nâng cao (theo tên, level, trạng thái)
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Boolean isActive
    ) {
        List<JobPosition> result = jobPositionService.searchJobPositions(name, level, isActive);
        List<JobPositionResponse> responseList = result.stream()
                .map(JobPositionResponse::fromJobPosition)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Search job positions successfully", responseList));
    }

    // Kích hoạt / hủy kích hoạt
    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        boolean success = jobPositionService.activateJobPosition(id);
        return ResponseEntity.ok(ApiResponse.success("Activated job position successfully", success));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<?> deactivate(@PathVariable Long id) {
        boolean success = jobPositionService.deactivateJobPosition(id);
        return ResponseEntity.ok(ApiResponse.success("Deactivated job position successfully", success));
    }
}
