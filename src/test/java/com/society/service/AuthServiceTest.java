package com.society.service;


import com.society.dto.request.OtpVerificationRequest;
import com.society.dto.request.RegisterRequest;
import com.society.dto.response.AuthResponse;
import com.society.entity.*;
import com.society.exception.BadRequestException;
import com.society.repository.*;
import com.society.security.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RentalProfileRepository rentalProfileRepository;
    @Mock private SecurityGuardRepository securityGuardRepository;
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
                .fullName("keshav").phoneNo("7987948810").role("RENTAL")
                .apartmentNo("A-101").totalMembers(4).bloodGroup("O+").build();

        User saved = User.builder().id(1L).phoneNo("7987948810").role(Role.RENTAL).build();

        when(userRepository.existsByPhoneNo("7987948810")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        String result = authService.register(req);

        assertThat(result).contains("Registration successful");
        verify(rentalProfileRepository).save(any(RentalProfile.class));
    }

    @Test
    @DisplayName("Register - Phone exists throws exception")
    void register_PhoneExists_Throws() {
        RegisterRequest req = RegisterRequest.builder().phoneNo("7987948810").role("RENTAL").build();
        when(userRepository.existsByPhoneNo(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Login with OTP - Success")
    void loginWithOtp_Success() {
        User user = User.builder().id(1L).phoneNo("7987948810").isActive(true).build();
        when(userRepository.findByPhoneNo("7987948810")).thenReturn(Optional.of(user));

        AuthResponse res = authService.loginWithOtp("7987948810");

        assertThat(res.getPhoneNo()).isEqualTo("7987948810");
        verify(otpService).generateAndSendOtp("7987948810");
    }

    @Test
    @DisplayName("Verify OTP - Success returns token")
    void verifyOtp_Success() {
        User user = User.builder()
                .id(1L).phoneNo("7987948810").role(Role.RENTAL)
                .fullName("keshav").isVerified(false).build();

        when(otpService.verifyOtp(anyString(), anyString())).thenReturn(true);
        when(userRepository.findByPhoneNo("7987948810")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(anyString(), anyString(), anyLong())).thenReturn("token");

        AuthResponse res = authService.verifyOtpAndLogin(
                OtpVerificationRequest.builder().phoneNo("7987948810").otp("123456").build());

        assertThat(res.getToken()).isEqualTo("token");
        assertThat(res.getRole()).isEqualTo("RENTAL");
    }
}
