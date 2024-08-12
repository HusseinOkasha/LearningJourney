package com.project3.appuser;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


@Entity
@Table(name = "app_user", uniqueConstraints = {@UniqueConstraint(name="app_user_email_unique", columnNames = "email")})
@Getter
@Setter
@NoArgsConstructor
public class AppUser implements UserDetails {

    // fields
    @Id
    @SequenceGenerator(
            name ="app_user_sequence",
            sequenceName = "app_user_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            generator = "app_user_sequence",
            strategy = GenerationType.SEQUENCE
    )
    @Column(name="id")
    private Long id;

    @Column(name = "first_name", nullable = false, columnDefinition = "TEXT")
    private String firstName;

    @Column(name = "last_name", nullable = false, columnDefinition = "TEXT")
    private String lastName;

    @Column(name = "email", nullable = false, columnDefinition = "TEXT")
    private String email;

    @Column(name="password", nullable = false, columnDefinition = "TEXT")
    private String password;

    @Column(name="app_user_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private AppUserRole appUserRole;

    @Column(name="locked", nullable = false)
    private Boolean locked = false;

    @Column(name="enabled", nullable = false)
    private Boolean enabled = false;


    // Constructors
    public AppUser(String firstName, String lastName, String email, String password, AppUserRole appUserRole) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.appUserRole = appUserRole;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority(appUserRole.name());
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
     public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
