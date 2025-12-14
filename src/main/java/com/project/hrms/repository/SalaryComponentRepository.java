package com.project.hrms.repository;

import com.project.hrms.model.SalaryComponent;
import com.project.hrms.model.enums.SalaryComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SalaryComponentRepository extends JpaRepository<SalaryComponent, Long>,
        JpaSpecificationExecutor<SalaryComponent> {

    Optional<SalaryComponent> findByCode(String code);

    // ✅ Tối ưu: load 1 lần nhiều code
    List<SalaryComponent> findByCodeIn(Collection<String> codes);

    boolean existsByCode(String code);
    List<SalaryComponent> findByType(SalaryComponentType type);


    // ✅ Phương thức tương đương để lấy danh sách các thành phần lương
    @Query("SELECT s FROM SalaryComponent s")
    List<SalaryComponent> findBySalaryComponents();
}
