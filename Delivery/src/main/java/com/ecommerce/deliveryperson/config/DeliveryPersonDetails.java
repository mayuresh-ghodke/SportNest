package com.ecommerce.deliveryperson.config;


import com.ecommerce.library.model.DeliveryPerson;
import com.ecommerce.library.model.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DeliveryPersonDetails implements UserDetails {

    private DeliveryPerson deliveryPerson;

    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for(Role role : deliveryPerson.getRoles()){
            authorities.add(new SimpleGrantedAuthority(role.getName()));
            
        }
        return authorities;
    }
    @Override
    public String getPassword() {
        return deliveryPerson.getPassword();
    }

    @Override
    public String getUsername() {
        return deliveryPerson.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
