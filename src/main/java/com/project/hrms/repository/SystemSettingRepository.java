package com.project.hrms.repository;

import com.project.hrms.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
    // Hàm tiện ích lấy giá trị cấu hình
    default String getValue(String key, String defaultValue) {
        return findById(key).map(SystemSetting::getValue).orElse(defaultValue);
    }
}