package com.demo.upi.service;

import com.demo.upi.entity.User;
import com.demo.upi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 🔐 1. Send Reset OTP
    public String sendResetOtp(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email"));

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        user.setResetOtp(otp);
        user.setResetOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        // ✅ Correct parameter order
        emailService.sendOtpEmail(email, otp, "RESET_PASSWORD");

        return "Reset OTP sent to your email";
    }

    // 🔐 2. Verify Reset OTP
    public String verifyResetOtp(String email, String otp) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getResetOtp() == null || !user.getResetOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        if (user.getResetOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        return "OTP verified successfully";
    }

    // 🔐 3. Reset Password
    public String resetPassword(String email, String newPassword) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));

        user.setResetOtp(null);
        user.setResetOtpExpiry(null);

        userRepository.save(user);

        return "Password reset successful";
    }
}