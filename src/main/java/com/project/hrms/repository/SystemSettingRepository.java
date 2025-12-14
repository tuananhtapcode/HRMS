package com.project.hrms.repository;

import com.project.hrms.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Repository; // ✅ Đảm bảo có import này

import java.util.Collection;
import java.util.List;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {

    default String getValue(String key, String defaultValue) {
        return findById(key).map(SystemSetting::getValue).orElse(defaultValue);
    }

    // ✅ Tối ưu: load nhiều setting 1 query
    List<SystemSetting> findBySettingKeyIn(Collection<String> keys);
}
