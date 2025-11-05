package com.project.hrms.service;

import com.project.hrms.dto.JobPositionDTO;
import com.project.hrms.exception.DataNotFoundException;
import com.project.hrms.model.JobPosition;
import com.project.hrms.repository.JobPositionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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
    public JobPosition getById(Long id) {
        return jobPositionRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find Job Position with id: " + id));
    }

    @Override
    public JobPosition create(JobPositionDTO jobPositionDTO) {
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
        return jobPositionRepository.save(jobPosition);
    }


    public JobPosition update(Long id, JobPositionDTO jobPositionDTO) {
        JobPosition existing = getById(id);
        modelMapper.map(jobPositionDTO, existing); // cập nhật các field từ DTO vào entity
        return jobPositionRepository.save(existing);
    }

    @Override
    public List<JobPosition> getAllActive() {
        return jobPositionRepository.findByIsActiveTrue();
    }

    @Override
    public List<JobPosition> getAll() {
        return jobPositionRepository.findAll();
    }



    @Override
    public Optional<JobPosition> getByCode(String code) {
        return jobPositionRepository.findByCode(code);
    }

    @Override
    public void deleteById(Long id) {
        JobPosition existing = getById(id);
        existing.setIsActive(false); // soft delete
        jobPositionRepository.save(existing);
    }

    @Override
    public boolean activateJobPosition(Long id) {
        Optional<JobPosition> jobPositionOptional = jobPositionRepository.findById(id);
        if (jobPositionOptional.isPresent()) {
            JobPosition jobPosition = jobPositionOptional.get();
            jobPosition.setIsActive(true);
            jobPositionRepository.save(jobPosition);
            return true;
        }
        return false;
    }

    @Override
    public boolean deactivateJobPosition(Long id) {
        Optional<JobPosition> jobPositionOptional = jobPositionRepository.findById(id);
        if (jobPositionOptional.isPresent()) {
            JobPosition jobPosition = jobPositionOptional.get();
            jobPosition.setIsActive(false);
            jobPositionRepository.save(jobPosition);
            return true;
        }
        return false;
    }

    @Override
    public boolean existsByCode(String code) {
        return jobPositionRepository.existsByCode(code);
    }

    @Override
    public boolean existsById(Long id) {
        return jobPositionRepository.existsById(id);
    }

    @Override
    public List<JobPosition> searchJobPositions(String name, String level, Boolean isActive) {
        // Đây là ví dụ triển khai đơn giản.
        // Trong thực tế, bạn có thể cần dùng JPA Criteria API hoặc Querydsl
        // để xây dựng câu truy vấn phức tạp hơn.

        // Nếu cần tìm kiếm theo nhiều tiêu chí phức tạp hơn,
        // bạn có thể cần thêm các phương thức tùy chỉnh vào JobPositionRepository
        // và gọi chúng ở đây.

        if (name == null && level == null && isActive == null) {
            return jobPositionRepository.findAll();
        }

        // Ví dụ: Tìm kiếm đơn giản chỉ theo name (nếu có)
        if (name != null && level == null && isActive == null) {
            // Spring Data JPA sẽ tự tạo phương thức này nếu bạn định nghĩa
            // hoặc bạn có thể dùng @Query
            // return jobPositionRepository.findByNameContainingIgnoreCase(name);
            // Tạm thời trả về rỗng hoặc implement chi tiết hơn
            return List.of(); // Placeholder
        }

        // Nếu bạn muốn triển khai đầy đủ, bạn có thể cần:
        // return jobPositionRepository.findAll((root, query, cb) -> {
        //     List<Predicate> predicates = new ArrayList<>();
        //     if (name != null) {
        //         predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        //     }
        //     if (level != null) {
        //         predicates.add(cb.equal(root.get("level"), level));
        //     }
        //     if (isActive != null) {
        //         predicates.add(cb.equal(root.get("isActive"), isActive));
        //     }
        //     return cb.and(predicates.toArray(new Predicate[0]));
        // });

        // Tạm thời trả về rỗng để tránh lỗi biên dịch nếu không có logic cụ thể
        return List.of();
    }
}