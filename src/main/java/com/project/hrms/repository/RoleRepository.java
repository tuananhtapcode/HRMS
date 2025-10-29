package com.project.hrms.repository;

import com.project.hrms.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // Đừng quên import

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
