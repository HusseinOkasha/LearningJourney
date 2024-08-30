package com.example.EmployeeManager.model;

public class EmployeeBuilder {

    private String name;
    private String email;
    private String jobTitle;
    private String password;
    private String phone;
    private String imageUrl;
    private String employeeCode;
    private Role role;

    public EmployeeBuilder() {
    }

    public EmployeeBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public EmployeeBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public EmployeeBuilder withJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    public EmployeeBuilder withPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public EmployeeBuilder withImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    public EmployeeBuilder withEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
        return this;
    }
    public EmployeeBuilder withPassword(String password){
        this.password = password;
        return this;
    }
    public EmployeeBuilder withRole(Role role){
        this.role = role;
        return this ;
    }

    public Employee build() {
        return new Employee(name, email, jobTitle, phone, imageUrl,
                password, role, employeeCode);
    }
}
