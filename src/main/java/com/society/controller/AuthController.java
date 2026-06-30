package com.society.controller;

import com.society.dto.request.OtpVerificationRequest;
import com.society.dto.request.RegisterRequest;
import com.society.dto.response.ApiResponse;
import com.society.dto.response.AuthResponse;
import com.society.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "1. Authentication", description = "User registration and OTP-based login")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user (Rental/Manager/Owner/Security)")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        String message = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, null));
    }

    @PostMapping("/login")
    @Operation(summary = "Login - sends OTP to phone")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestParam String phoneNo) {
        AuthResponse response = authService.loginWithOtp(phoneNo);
        return ResponseEntity.ok(ApiResponse.success("OTP sent to your phone", response));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and get JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        AuthResponse response = authService.verifyOtpAndLogin(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

}
