package com.society.service;

import com.society.dto.request.OtpVerificationRequest;
import com.society.dto.request.RegisterRequest;
import com.society.dto.response.AuthResponse;
import com.society.entity.*;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.*;
import com.society.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RentalProfileRepository rentalProfileRepository;
    private final SecurityGuardRepository securityGuardRepository;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Transactional
    public String register(RegisterRequest request) {
        log.info("Registration attempt for phone: {}", request.getPhoneNo());

        if (userRepository.existsByPhoneNo(request.getPhoneNo())) {
            throw new BadRequestException("Phone number already registered");
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + request.getRole());
        }

        User user = User.builder()
                .phoneNo(request.getPhoneNo())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(role)
                .isActive(true)
                .isVerified(false)
                .build();

        user = userRepository.save(user);

        // Save role-specific profile
        switch (role) {
            case RENTAL -> createRentalProfile(user, request);
            case SECURITY_GUARD -> createSecurityGuardProfile(user, request);
            default -> log.info("No additional profile required for role: {}", role);
        }

        otpService.generateAndSendOtp(request.getPhoneNo());
        log.info("Registration successful for: {}", request.getPhoneNo());

        return "Registration successful. Please verify OTP sent to your phone.";
    }

    public AuthResponse loginWithOtp(String phoneNo) {
        User user = userRepository.findByPhoneNo(phoneNo)
                .orElseThrow(() -> new ResourceNotFoundException("User not registered"));

        if (!user.getIsActive()) {
            throw new BadRequestException("Account is deactivated. Contact admin");
        }

        otpService.generateAndSendOtp(phoneNo);
        return AuthResponse.builder().phoneNo(phoneNo).build();
    }

    @Transactional
    public AuthResponse verifyOtpAndLogin(OtpVerificationRequest request) {
        otpService.verifyOtp(request.getPhoneNo(), request.getOtp());

        User user = userRepository.findByPhoneNo(request.getPhoneNo())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsVerified(true);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getPhoneNo(), user.getRole().name(), user.getId());
        log.info("User logged in: {}", user.getPhoneNo());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userId(user.getId())
                .phoneNo(user.getPhoneNo())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    private void createRentalProfile(User user, RegisterRequest request) {
        if (request.getApartmentNo() == null || request.getTotalMembers() == null) {
            throw new BadRequestException("Apartment number and total members are required for rental");
        }

        RentalProfile profile = RentalProfile.builder()
                .user(user)
                .apartmentNo(request.getApartmentNo())
                .totalMembers(request.getTotalMembers())
                .jobProfile(request.getJobProfile())
                .workingLocation(request.getWorkingLocation())
                .bloodGroup(request.getBloodGroup())
                .build();
        rentalProfileRepository.save(profile);
    }

    private void createSecurityGuardProfile(User user, RegisterRequest request) {
        if (request.getEmployeeId() == null) {
            throw new BadRequestException("Employee ID required for security guard");
        }

        SecurityGuard guard = SecurityGuard.builder()
                .user(user)
                .employeeId(request.getEmployeeId())
                .shiftType(request.getShiftType() != null ?
                        ShiftType.valueOf(request.getShiftType().toUpperCase()) : ShiftType.DAY)
                .gateNumber(request.getGateNumber())
                .emergencyContact(request.getEmergencyContact())
                .joiningDate(java.time.LocalDate.now())
                .build();
        securityGuardRepository.save(guard);
    }
}
