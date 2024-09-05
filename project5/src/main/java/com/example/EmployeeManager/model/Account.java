package com.example.EmployeeManager.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name="account", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
public class Account implements UserDetails {
    // tables' columns
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(name="name")
    private String name;

    @Column(name="email", nullable = false)
    private String email;

    @Column(name="job_title")
    private String jobTitle;

    @Column(name= "phone")
    private String phone;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name ="role")
    private Role role;

    @Column(name= "uuid", updatable = false)
    private UUID uuid;

    @ManyToMany
    @JoinTable(
            name = "user_tasks", // Name of the join table
            joinColumns = @JoinColumn(name = "employee_id"), // Foreign key for employee
            inverseJoinColumns = @JoinColumn(name = "task_id") // Foreign key for task
    )
    private Set<Task> tasks;

    private Account() {
    }

    private Account(String name,
                    String email,
                    String jobTitle,
                    String phone,
                    String imageUrl,
                    String password,
                    Role role,
                    UUID uuid) {
        this.name = name;
        this.email = email;
        this.jobTitle = jobTitle;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.password = password;
        this.role = role;
        this.uuid = uuid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return role;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    @PrePersist
    public void generateUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    public static Builder builder(){
        // creates and returns an instance of Account.Builder.
        return new Builder();
    }

    // Implemented methods from UserDetails .
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.email;
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

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", phone='" + phone + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", accountCode='" + uuid + '\'' +
                '}';
    }

    public static class Builder {

        private String name;
        private String email;
        private String jobTitle;
        private String password;
        private String phone;
        private String imageUrl;
        private UUID uuid;
        private Role role;

        public Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withJobTitle(String jobTitle) {
            this.jobTitle = jobTitle;
            return this;
        }

        public Builder withPhone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder withImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder withAccountCode(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withRole(Role role) {
            this.role = role;
            return this;
        }

        public Account build() {
            return new Account(name, email, jobTitle, phone, imageUrl,
                    password, role, uuid);
        }
    }
}
