package com.ecommerce.library.service;

import com.ecommerce.library.model.Role;

import java.util.List;

public interface RoleService {

    public List<Role> getAllRoles();
    public Role getRoleById(Long roleId);

    public Role addRole(Role role);

    public boolean isEmptyRoles();
    public boolean isRoleExists(Role role);
}
