package com.demo.upi.controller;

import com.demo.upi.entity.UpiId;
import com.demo.upi.entity.User;
import com.demo.upi.repository.UpiIdRepository;
import com.demo.upi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/upi")
public class UpiController {

    @Autowired
    private UpiIdRepository upiIdRepository;

    @Autowired
    private UserRepository userRepository;

    // STEP 5.7 – Create UPI ID for user
    @PostMapping("/create/{userId}")
    public String createUpiId(@PathVariable Long userId) {

        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            return "User not found";
        }

        // Check if UPI already exists
        if (upiIdRepository.findByUserId(userId).isPresent()) {
            return "UPI ID already exists for this user";
        }

        User user = userOptional.get();

        // Generate base UPI ID
        String baseUpi = user.getEmail().split("@")[0];
        String upiId = baseUpi + "@demo";

        // Ensure uniqueness
        int counter = 1;
        while (upiIdRepository.existsByUpiId(upiId)) {
            upiId = baseUpi + counter + "@demo";
            counter++;
        }

        // Save UPI ID
        UpiId upi = new UpiId(upiId, user);
        upiIdRepository.save(upi);

        return "UPI ID created successfully: " + upiId;
    }

    // STEP 5.6 – Get UPI ID by userId
    @GetMapping("/{userId}")
    public String getUpiId(@PathVariable Long userId) {

        return upiIdRepository.findByUserId(userId)
                .map(UpiId::getUpiId)
                .orElse("UPI ID not found for user");
    }
}
