package com.demo.upi.dto;

import com.demo.upi.entity.User;

public class UpdateUserRequest {

    private User.Role role;
    private Boolean isBlocked;

    public UpdateUserRequest() {}

    //  ROLE
    public User.Role getRole() {
        return role;
    }

    public void setRole(User.Role role) {
        this.role = role;
    }

    // Optional: allow string role input
    public void setRole(String roleName) {
        try {
            this.role = User.Role.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.role = User.Role.USER; // fallback
        }
    }

    //  FIXED BOOLEAN MAPPING
    public Boolean getIsBlocked() {
        return isBlocked;
    }

    public void setIsBlocked(Boolean isBlocked) {
        this.isBlocked = isBlocked;
    }
}