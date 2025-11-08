package com.project.hrms.service;

import com.project.hrms.dto.JobPositionDTO;
import com.project.hrms.exception.DataAlreadyExistsException;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.model.JobPosition;
import com.project.hrms.repository.JobPositionRepository;
import com.project.hrms.response.JobPositionResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class JobPositionService implements IJobPositionService {

    private final JobPositionRepository jobPositionRepository;
    private final ModelMapper modelMapper;

    @PostConstruct
    public void setupMapper() {
        modelMapper.typeMap(JobPositionDTO.class, JobPosition.class)
                .addMappings(mapper -> mapper.skip(JobPosition::setJobPositionId));
    }

    @Override
    @Transactional
    public JobPositionResponse create(JobPositionDTO jobPositionDTO) {
        if (jobPositionRepository.existsByCode(jobPositionDTO.getCode())) {
            throw new DataAlreadyExistsException("Mã chức danh đã tồn tại: " + jobPositionDTO.getCode());
        }
        if (jobPositionRepository.existsByName(jobPositionDTO.getName())) {
            throw new DataAlreadyExistsException("Job position name already exists");
        }
        //Cach 1
//        JobPosition jobPosition = JobPosition.builder()
//                .name(jobPositionDTO.getName())
//                .level(jobPositionDTO.getLevel())
//                .code(jobPositionDTO.getCode())
//                .description(jobPositionDTO.getDescription())
//                .minSalary(jobPositionDTO.getMinSalary())
//                .maxSalary(jobPositionDTO.getMaxSalary())
//                .build();

//    Cach 2
        JobPosition jobPosition = modelMapper.map(jobPositionDTO, JobPosition.class);
        jobPosition.setIsActive(true);
        jobPositionRepository.save(jobPosition);
        return JobPositionResponse.fromJobPosition(jobPosition);
    }

    @Override
    @Transactional
    public JobPositionResponse update(Long id, JobPositionDTO jobPositionDTO) {
        JobPosition existing = jobPositionRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Job position not found with id: " + id));

        // Nếu code mới trùng với code của entity khác
        // BUT Code không cho phép thay đổi sau khi tạo nen không cần kiểm tra nữa
//        if (!existing.getCode().equals(jobPositionDTO.getCode()) &&
//                jobPositionRepository.existsByCode(jobPositionDTO.getCode())) {
//            throw new DataAlreadyExistsException("Job position ID already exists: " + jobPositionDTO.getCode());
//        }

        if (!existing.getName().equals(jobPositionDTO.getName()) && jobPositionRepository.existsByName(jobPositionDTO.getName())) {
            throw new DataAlreadyExistsException("Job position name already exists");
        }

//        modelMapper.map(jobPositionDTO, existing); // cập nhật các field từ DTO vào entity
        existing.setName(jobPositionDTO.getName());
        existing.setDescription(jobPositionDTO.getDescription());
        existing.setMinSalary(jobPositionDTO.getMinSalary());
        existing.setMaxSalary(jobPositionDTO.getMaxSalary());

        JobPosition updated = jobPositionRepository.save(existing);
        return JobPositionResponse.fromJobPosition(updated);
    }

    @Override
    public void delete(Long id) {
        JobPosition jobPosition = jobPositionRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Job position not found with id: " + id));

        if (Boolean.FALSE.equals(jobPosition.getIsActive())) {
            throw new IllegalStateException("Job position is already inactive");
        }

        jobPosition.setIsActive(false); // soft delete
        jobPositionRepository.save(jobPosition);
    }

    @Override
    public JobPositionResponse getById(Long id) {
        JobPosition job = jobPositionRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Job position not found with id: " + id));
        return JobPositionResponse.fromJobPosition(job);
    }

    @Override
    public List<JobPosition> getAllActive() {
        return jobPositionRepository.findByIsActiveTrue();
    }

    @Override
    public Page<JobPositionResponse> getAll(PageRequest pageRequest) {
        return jobPositionRepository.findAll(pageRequest).map(JobPositionResponse::fromJobPosition);

        //chuyen tu JobPosition sang JobPositionResponse bang cach su dung map cua Page. Tam thoi ko dung
//        return jobPositionRepository.findAll()
//                .stream()
//                .map(JobPositionResponse::fromJobPosition)
//                .collect(Collectors.toList());
    }

    @Override
    public Page<JobPositionResponse> search(String keyword, PageRequest pageRequest) {
        return jobPositionRepository.search(keyword, pageRequest).map(JobPositionResponse::fromJobPosition);
    }

    @Override
    public ByteArrayInputStream exportToExcel() {
        List<JobPosition> list = jobPositionRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Job Positions");
            Row header = sheet.createRow(0);

            String[] columns = {"ID", "Code", "Name", "Description", "Level", "Min Salary", "Max Salary", "Active"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (JobPosition job : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(job.getJobPositionId());
                row.createCell(1).setCellValue(job.getCode());
                row.createCell(2).setCellValue(job.getName());
                row.createCell(3).setCellValue(job.getDescription() != null ? job.getDescription() : "");
                row.createCell(4).setCellValue(job.getLevel() != null ? job.getLevel() : "");
                row.createCell(5).setCellValue(job.getMinSalary() != null ? job.getMinSalary().doubleValue() : 0);
                row.createCell(6).setCellValue(job.getMaxSalary() != null ? job.getMaxSalary().doubleValue() : 0);
                row.createCell(7).setCellValue(Boolean.TRUE.equals(job.getIsActive()) ? "Yes" : "No");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Failed to export Excel: " + e.getMessage());
        }
    }

    @Override
    public Optional<JobPosition> getByCode(String code) {
        return jobPositionRepository.findByCode(code);
    }


    @Override
    @Transactional
    public boolean activateJobPosition(Long id) {
        JobPosition jobPosition = jobPositionRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Job position not found with id: " + id));

        if (Boolean.TRUE.equals(jobPosition.getIsActive())) {
            throw new IllegalStateException("Job position is already active");
        }

        jobPosition.setIsActive(true);
        jobPositionRepository.save(jobPosition);
        return true;
    }

    @Override
    @Transactional
    public boolean deactivateJobPosition(Long id) {
        JobPosition jobPosition = jobPositionRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Job position not found with id: " + id));

        if (Boolean.FALSE.equals(jobPosition.getIsActive())) {
            throw new IllegalStateException("Job position is already inactive");
        }

        jobPosition.setIsActive(false);
        jobPositionRepository.save(jobPosition);
        return true;
    }


    @Override
    public boolean existsByCode(String code) {
        return jobPositionRepository.existsByCode(code);
    }

    @Override
    public boolean existsById(Long id) {
        return jobPositionRepository.existsById(id);
    }

    //code test search advanced
    @Override
    public List<JobPosition> searchJobPositions(String name, String level, Boolean isActive) {
        // Gọi custom repository query (định nghĩa bằng @Query trong JobPositionRepository)
        return jobPositionRepository.searchAdvanced(name, level, isActive);
    }

    //code test search advanced - cach 2
//    @Override
//    public List<JobPosition> searchJobPositions(String name, String level, Boolean isActive) {
//        // Đây là ví dụ triển khai đơn giản.
//        // Trong thực tế, bạn có thể cần dùng JPA Criteria API hoặc Querydsl
//        // để xây dựng câu truy vấn phức tạp hơn.
//
//        // Nếu cần tìm kiếm theo nhiều tiêu chí phức tạp hơn,
//        // bạn có thể cần thêm các phương thức tùy chỉnh vào JobPositionRepository
//        // và gọi chúng ở đây.
//
//        if (name == null && level == null && isActive == null) {
//            return jobPositionRepository.findAll();
//        }
//
//        // Ví dụ: Tìm kiếm đơn giản chỉ theo name (nếu có)
//        if (name != null && level == null && isActive == null) {
//            // Spring Data JPA sẽ tự tạo phương thức này nếu bạn định nghĩa
//            // hoặc bạn có thể dùng @Query
//            // return jobPositionRepository.findByNameContainingIgnoreCase(name);
//            // Tạm thời trả về rỗng hoặc implement chi tiết hơn
//            return List.of(); // Placeholder
//        }
//
//        // Nếu bạn muốn triển khai đầy đủ, bạn có thể cần:
//        // return jobPositionRepository.findAll((root, query, cb) -> {
//        //     List<Predicate> predicates = new ArrayList<>();
//        //     if (name != null) {
//        //         predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
//        //     }
//        //     if (level != null) {
//        //         predicates.add(cb.equal(root.get("level"), level));
//        //     }
//        //     if (isActive != null) {
//        //         predicates.add(cb.equal(root.get("isActive"), isActive));
//        //     }
//        //     return cb.and(predicates.toArray(new Predicate[0]));
//        // });
//
//        // Tạm thời trả về rỗng để tránh lỗi biên dịch nếu không có logic cụ thể
//        return List.of();
//    }
}