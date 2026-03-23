package com.demo.upi.controller;

import com.demo.upi.entity.User;
import com.demo.upi.entity.OtpVerification;
import com.demo.upi.entity.UpiId; // 🔥 IMPORT ADDED
import com.demo.upi.repository.UserRepository;
import com.demo.upi.repository.OtpRepository;
import com.demo.upi.repository.UpiIdRepository; // 🔥 IMPORT ADDED
import com.demo.upi.service.EmailService;
import com.demo.upi.security.JwtUtil;
import com.demo.upi.dto.RegisterRequest;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // 🔥 NEW
    @Autowired
    private UpiIdRepository upiIdRepository;

    /* ---------------- UPI GENERATOR ---------------- */
    public String generateUpiId(String fullName) {

        String base = fullName.toLowerCase().replace(" ", "");
        String upi = base + "@payflow";

        int count = 1;

        while (upiIdRepository.existsByUpiId(upi)) {
            upi = base + count + "@payflow";
            count++;
        }

        return upi;
    }

    /* ---------------- REGISTER STEP 1: SEND OTP ---------------- */
    @PostMapping("/check-and-send-otp")
    public String checkAndSendOtp(@RequestBody User user) {

        if (user.getEmail() == null || user.getMobileNumber() == null) {
            return "Invalid input data";
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            return "Email already registered";
        }

        if (userRepository.existsByMobileNumber(user.getMobileNumber())) {
            return "Mobile number already registered";
        }

        otpRepository.deleteByEmail(user.getEmail());

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        OtpVerification otpData = new OtpVerification();
        otpData.setEmail(user.getEmail());
        otpData.setOtp(otp);
        otpData.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepository.save(otpData);

        emailService.sendOtpEmail(user.getEmail(), otp, "REGISTER");

        return "OTP sent successfully";
    }

    /* ---------------- REGISTER STEP 2: VERIFY OTP + REGISTER ---------------- */
    @PostMapping("/verify-otp-and-register")
    public String verifyOtpAndRegister(
            @RequestParam String otp,
            @Valid @RequestBody RegisterRequest request) {

        if (!otp.matches("^[0-9]{6}$")) {
            return "OTP must be 6 digits";
        }

        Optional<OtpVerification> otpOpt =
                otpRepository.findTopByEmailOrderByExpiryTimeDesc(request.getEmail());

        if (otpOpt.isEmpty()) {
            return "No OTP found";
        }

        OtpVerification otpData = otpOpt.get();

        if (!otpData.getOtp().equals(otp)) {
            return "Invalid OTP";
        }

        if (otpData.getExpiryTime().isBefore(LocalDateTime.now())) {
            return "OTP expired";
        }

        // 🔥 CREATE USER
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);

        User savedUser = userRepository.save(user);

        // 🔥 GENERATE UPI
        String upi = generateUpiId(savedUser.getFullName());

        // 🔥 CREATE UPI ENTITY
        UpiId upiId = new UpiId();
        upiId.setUpiId(upi);
        upiId.setUser(savedUser);

        // 🔥 SAVE UPI
        upiIdRepository.save(upiId);

        // 🔥 LINK BACK (optional but good)
        savedUser.setUpiId(upiId);

        otpRepository.deleteByEmail(request.getEmail());

        return "User registered successfully with UPI ID: " + upi;
    }

    /* ---------------- LOGIN STEP 1: SEND OTP ---------------- */
    @PostMapping("/login-send-otp")
    public String loginSendOtp(@RequestBody User loginRequest) {

        Optional<User> userOpt =
                userRepository.findByEmail(loginRequest.getEmail());

        if (userOpt.isEmpty()) {
            return "Email not registered";
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(
                loginRequest.getPassword(),
                user.getPassword())) {
            return "Invalid password";
        }

        otpRepository.deleteByEmail(user.getEmail());

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        OtpVerification otpData = new OtpVerification();
        otpData.setEmail(user.getEmail());
        otpData.setOtp(otp);
        otpData.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepository.save(otpData);

        emailService.sendOtpEmail(user.getEmail(), otp, "LOGIN");

        return "OTP sent to your email";
    }

    /* ---------------- LOGIN STEP 2: VERIFY OTP ---------------- */
@PostMapping("/login-verify-otp")
    public ResponseEntity<?> loginVerifyOtp(
            @RequestParam String otp,
            @RequestBody User loginRequest) {

        if (!otp.matches("^[0-9]{6}$")) {
            return ResponseEntity.badRequest().body("OTP must be 6 digits");
        }

        Optional<OtpVerification> otpOpt =
                otpRepository.findTopByEmailOrderByExpiryTimeDesc(loginRequest.getEmail());

        if (otpOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No OTP found");
        }

        OtpVerification otpData = otpOpt.get();

        if (!otpData.getOtp().equals(otp)) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        if (otpData.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("OTP expired");
        }

        Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = jwtUtil.generateToken(user);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", token);
            otpRepository.deleteByEmail(loginRequest.getEmail());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    /* ---------------- FORGOT PASSWORD ---------------- */
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return "Email not found";
        }
        User user = userOpt.get();

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        user.setResetOtp(otp);
        user.setResetOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendOtpEmail(email, otp, "RESET");
        return "Reset OTP sent to your email";
    }

    @PostMapping("/verify-reset-otp")
    public String verifyResetOtp(@RequestParam String email, @RequestParam String otp) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return "Email not found";
        }
        User user = userOpt.get();
        if (user.getResetOtp() == null || !user.getResetOtp().equals(otp) || user.getResetOtpExpiry().isBefore(LocalDateTime.now())) {
            return "Invalid or expired OTP";
        }
        return "OTP verified successfully";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String newPassword = request.get("newPassword");
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return "Email not found";
        }
        User user = userOpt.get();
        if (user.getResetOtp() == null || user.getResetOtpExpiry().isBefore(LocalDateTime.now())) {
            return "OTP expired or invalid";
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetOtp(null);
        user.setResetOtpExpiry(null);
        userRepository.save(user);
        return "Password reset successful";
    }

    /* ---------------- TEST PROTECTED API ---------------- */
    @GetMapping("/test-protected")
    public String testProtected() {
        return "Access granted to protected API";
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            org.springframework.security.core.Authentication authentication) {

        String email = authentication.getName();
        System.out.println("UserController /me - Looking up user by email: " + email);

        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            System.out.println("UserController /me - User NOT FOUND for email: " + email);
            return ResponseEntity.status(404).body("User not found");
        }

        User user = userOpt.get();
        System.out.println("UserController /me - User found: " + user.getFullName() + " (" + user.getEmail() + ")");

        // FIX: Prevent Jackson infinite recursion (User ↔ UpiId bidirectional)
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("fullName", user.getFullName());
        userData.put("email", user.getEmail());
        userData.put("mobileNumber", user.getMobileNumber());
        userData.put("role", user.getRole().name());
        userData.put("isBlocked", user.isBlocked());
        
        // Fetch UPI ID
        Optional<UpiId> upiOpt = upiIdRepository.findByUserId(user.getId());
        userData.put("upiId", upiOpt.map(UpiId::getUpiId).orElse("No UPI ID"));

        return ResponseEntity.ok(userData);
    }
}

