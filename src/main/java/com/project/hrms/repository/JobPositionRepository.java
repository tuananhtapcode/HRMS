package com.project.hrms.repository;

import com.project.hrms.model.JobPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobPositionRepository extends JpaRepository<JobPosition, Long> {
    List<JobPosition> findByIsActiveTrue();

    Optional<JobPosition> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByName(String name);

    //Cac phuong thuc tim kiem nang cao co the duoc dinh nghia o day neu can
    @Query("""
        SELECT j FROM JobPosition j 
        WHERE LOWER(j.code) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(j.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
           OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    Page<JobPosition> search(String keyword, Pageable pageable);

    @Query("""
    SELECT j FROM JobPosition j
    WHERE (:name IS NULL OR LOWER(j.name) LIKE LOWER(CONCAT('%', :name, '%')))
      AND (:level IS NULL OR j.level = :level)
      AND (:isActive IS NULL OR j.isActive = :isActive)
""")
    List<JobPosition> searchAdvanced(@Param("name") String name,
                                     @Param("level") String level,
                                     @Param("isActive") Boolean isActive);

}
