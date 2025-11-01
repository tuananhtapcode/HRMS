package com.project.hrms.controller;

import com.project.hrms.dto.JobPositionDTO;
import com.project.hrms.model.JobPosition;
import com.project.hrms.service.IJobPositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

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
            System.out.println("Received CategoryDTO: " + request);
            return ResponseEntity.ok(jobPositionService.create(request));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            JobPosition jobPosition = jobPositionService.getById(id);
            if(jobPosition == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(jobPositionService.getById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(jobPositionService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobPosition> update(@PathVariable Long id, @RequestBody JobPositionDTO request) {
        return ResponseEntity.ok(jobPositionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        jobPositionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
