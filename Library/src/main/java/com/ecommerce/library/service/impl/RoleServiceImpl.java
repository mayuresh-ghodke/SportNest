package com.ecommerce.library.service.impl;

import com.ecommerce.library.model.Role;
import com.ecommerce.library.repository.RoleRepository;
import com.ecommerce.library.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public Role addRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public boolean isEmptyRoles(){
        List<Role> allRoles = getAllRoles();
        if(allRoles.size()==0)
            return true;
        return false;
    }

    @Override
    public boolean isRoleExists(Role role) {
        Role existingRole = roleRepository.findByName(role.getName());
        if(existingRole==null){
            System.out.println("Role '"+role.getName()+"' is not present.");
            return false;
        }
        System.out.println("Role '"+role.getName()+"' is present.");
        return true;
    }


    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role getRoleById(Long roleId) {
        return roleRepository.findById(roleId).get();
    }



}
