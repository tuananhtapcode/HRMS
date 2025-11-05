package com.project.hrms.repository;

import com.project.hrms.model.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {
    List<JobPosition> findByIsActiveTrue();
    Optional<JobPosition> findByCode(String code);
    boolean existsByCode(String code);
    //Cac phuong thuc tim kiem nang cao co the duoc dinh nghia o day neu can
}
