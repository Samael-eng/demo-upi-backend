package com.demo.upi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String mobileNumber;

@Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    // Reset Password Fields
    @Column(name = "reset_otp")
    private String resetOtp;

    @Column(name = "reset_otp_expiry")
    private LocalDateTime resetOtpExpiry;

    // 🔥 ADD THIS (UPI RELATION)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UpiId upiId;

@Column(name = "is_blocked", columnDefinition = "boolean default false")
    private Boolean isBlocked = false;

    // Constructors
public enum Role {
        USER, ADMIN
    }

    public User() {
    }

    public User(String fullName, String email, String mobileNumber, String password) {
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.password = password;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getResetOtp() {
        return resetOtp;
    }

    public void setResetOtp(String resetOtp) {
        this.resetOtp = resetOtp;
    }

    public LocalDateTime getResetOtpExpiry() {
        return resetOtpExpiry;
    }

    public void setResetOtpExpiry(LocalDateTime resetOtpExpiry) {
        this.resetOtpExpiry = resetOtpExpiry;
    }

    // 🔥 NEW GETTER & SETTER
    public UpiId getUpiId() {
        return upiId;
    }

    public void setUpiId(UpiId upiId) {
        this.upiId = upiId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(Boolean blocked) {
        isBlocked = blocked != null ? blocked : false;
    }
}
