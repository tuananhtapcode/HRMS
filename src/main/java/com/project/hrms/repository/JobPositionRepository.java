package com.project.hrms.repository;

import com.project.hrms.model.JobPosition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {
}
