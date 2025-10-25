package com.project.hrms.service;

import com.project.hrms.dto.RoleDTO;
import com.project.hrms.model.Role;
import com.project.hrms.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService implements IRoleService {
    RoleRepository roleRepository;

    @Override
    public Role createRole(RoleDTO RoleDTO) {
        Role newRole = Role
                .builder()
                .name(RoleDTO.getName())
                .build();
        return roleRepository.save(newRole);
    }

    @Override
    public Role findRoleById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
//        return null;
    }

    @Override
    public List<Role> getAllRole() {
        return roleRepository.findAll();
    }

    @Override
    public Role updateRole(Long RoleId, RoleDTO RoleDTO) {
        Optional<Role> existingRole = roleRepository.findById(RoleId);
        if (existingRole.isPresent()) {
            Role Role = existingRole.get();
            Role.setName(RoleDTO.getName());
            return roleRepository.save(Role);
        }
        return null;
    }

}
