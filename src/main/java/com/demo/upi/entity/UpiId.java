package com.demo.upi.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "upi_ids")
public class UpiId {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String upiId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true) // 🔥 added unique
    private User user;

    // Constructors
    public UpiId() {
    }

    public UpiId(String upiId, User user) {
        this.upiId = upiId;
        this.user = user;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}