package com.demo.upi.repository;

import com.demo.upi.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findByEmailAndOtp(String email, String otp);

    Optional<OtpVerification> findByMobileNumberAndOtp(String mobileNumber, String otp);

    Optional<OtpVerification> findTopByEmailOrderByExpiryTimeDesc(String email);

    @Transactional
    void deleteByEmail(String email);
}
