package com.ecommerce.library.model;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class DeliveryPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_person_id")
    private Long id;
    private String firstName; 
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private boolean isAvailable;
    

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "delivery_person_roles",
            joinColumns = @JoinColumn(name = "delivery_person_id", referencedColumnName = "delivery_person_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "role_id"))
    private List<Role> roles;

    public String getUsername() {
        return this.getEmail();
    }
}
