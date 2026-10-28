package com.example.dhcnttweb2026.dto;

import java.io.Serializable;

public class CustomerLoginDto implements Serializable {
    private String email;
    private String password;

    public CustomerLoginDto() {
    }

    public CustomerLoginDto(String email, String password) {
        this.email = email;
        this.password = password;
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
}
