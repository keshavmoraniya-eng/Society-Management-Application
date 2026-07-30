package com.society.service;

import com.society.dto.request.OtpVerificationRequest;
import com.society.dto.request.RegisterRequest;
import com.society.dto.response.AuthResponse;
import com.society.entity.Role;
import com.society.entity.User;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.UserRepository;
import com.society.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private OtpService otpService;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "jwtExpiration", 86400000L);
    }

    @Test
    @DisplayName("Register rental - Success")
    void registerRental_Success() {
        RegisterRequest req = RegisterRequest.builder()
                .fullName("John")
                .phoneNo("9876543210")
                .role("RENTAL")
                .apartmentNo("A-101")
                .totalMembers(4)
                .bloodGroup("O+")
                .build();

        User saved = User.builder()
                .id(1L)
                .phoneNo("9876543210")
                .role(Role.RENTAL)
                .build();

        when(userRepository.existsByPhoneNo("9876543210")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        String result = authService.register(req);

        assertThat(result).contains("Registration successful");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register - Phone already exists")
    void register_PhoneExists_Throws() {
        RegisterRequest req = RegisterRequest.builder()
                .phoneNo("9876543210")
                .role("RENTAL")
                .build();

        when(userRepository.existsByPhoneNo(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Verify OTP - Success returns token")
    void verifyOtp_Success() {
        User user = User.builder()
                .id(1L)
                .phoneNo("9876543210")
                .role(Role.RENTAL)
                .fullName("John")
                .isVerified(false)
                .build();

        when(otpService.verifyOtp(anyString(), anyString())).thenReturn(true);
        when(userRepository.findByPhoneNo("9876543210")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(anyString(), anyString(), anyLong())).thenReturn("jwt-token");

        AuthResponse response = authService.verifyOtpAndLogin(
                OtpVerificationRequest.builder()
                        .phoneNo("9876543210")
                        .otp("123456")
                        .build());

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("RENTAL");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Verify OTP - Invalid throws exception")
    void verifyOtp_Invalid_Throws() {
        when(otpService.verifyOtp(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.verifyOtpAndLogin(
                OtpVerificationRequest.builder()
                        .phoneNo("9876543210")
                        .otp("wrong")
                        .build()))
                .isInstanceOf(BadRequestException.class);
    }
}
