package com.project.hrms.controller;

import com.project.hrms.dto.JobPositionDTO;
import com.project.hrms.model.JobPosition;
import com.project.hrms.service.IJobPositionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(jobPositionService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobPosition> getById(@PathVariable Long id) {
        return ResponseEntity.ok(jobPositionService.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody JobPositionDTO request,
                                    BindingResult bindingResult) {
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
