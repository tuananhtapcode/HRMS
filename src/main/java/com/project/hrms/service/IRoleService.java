package com.project.hrms.service;

import com.project.hrms.dto.RoleDTO;
import com.project.hrms.model.Role;

import java.util.List;

public interface IRoleService {
    Role createRole(RoleDTO roleDTO);

    Role findRoleById(Long roleId);

    List<Role> getAllRole();

    Role updateRole(Long roleId, RoleDTO roleDTO);

//    void deleteRole(Long roleId);
}
