package com.example.dhcnttweb2026.dto;

import java.io.Serializable;

public class CustomerRegisterDto implements Serializable {
    private String name;
    private String email;
    private String password;
    private String confirmPassword;
    private String phone;

    public CustomerRegisterDto() {
    }

    public CustomerRegisterDto(String name, String email, String password, String confirmPassword, String phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.phone = phone;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
