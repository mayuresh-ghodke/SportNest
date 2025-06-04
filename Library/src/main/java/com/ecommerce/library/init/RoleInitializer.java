package com.ecommerce.library.init;

import com.ecommerce.library.model.Role;
import com.ecommerce.library.service.RoleService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RoleInitializer {

    private final RoleService roleService;

    @Autowired
    public RoleInitializer(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostConstruct
    public void initRoles() {

        System.out.println("Checking and initializing roles if missing...");

        createRoleIfNotExists("CUSTOMER");
        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("DELIVERY_PERSON");

        System.out.println("Final roles in database: "+roleService.getAllRoles().size());
        roleService.getAllRoles().forEach(System.out::println);
    }

    private void createRoleIfNotExists(String roleName) {
        Role role = new Role();
        role.setName(roleName);

        if (!roleService.isRoleExists(role)) {
            roleService.addRole(role);
            System.out.println("Role added: " + roleName);
        } else {
            System.out.println("Role already exists: " + roleName);
        }
    }
}
