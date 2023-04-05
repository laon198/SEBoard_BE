package com.seproject.seboard.oauth2.model;

import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;

@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role implements GrantedAuthority {
    @Id
    @GeneratedValue
    private Long roleId;

    @Column(nullable=false, unique=true)
    private String name;

//    @ManyToMany(mappedBy = "authorities",fetch = FetchType.LAZY)
//    private List<Account> accounts;

    public Role(String name) {
        this.name = name;
    }

    @Override
    public String getAuthority() {
        return name;
    }
}