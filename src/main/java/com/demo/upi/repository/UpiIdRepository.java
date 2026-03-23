package com.demo.upi.repository;

import com.demo.upi.entity.UpiId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UpiIdRepository extends JpaRepository<UpiId, Long> {

    Optional<UpiId> findByUserId(Long userId);

    boolean existsByUpiId(String upiId);

    // 🔥 Added this (optional but useful)
    Optional<UpiId> findByUpiId(String upiId);
}