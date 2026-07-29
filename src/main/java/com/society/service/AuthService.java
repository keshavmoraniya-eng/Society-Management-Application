package com.society.service;

import com.society.config.TwilioProperties;
import com.society.dto.request.OtpVerificationRequest;
import com.society.dto.request.RegisterRequest;
import com.society.dto.response.AuthResponse;
import com.society.entity.RentalProfile;
import com.society.entity.Role;
import com.society.entity.User;
import com.society.exception.BadRequestException;
import com.society.exception.OtpException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.RentalProfileRepository;
import com.society.repository.UserRepository;
import com.society.security.JwtUtil;
import com.society.util.PhoneMaskUtil;
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
    private final OtpService otpService;
    private final JwtUtil jwtUtil;
    private final TwilioProperties twilioProperties;
    private final RentalProfileRepository rentalProfileRepository;


    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Transactional
    public String register(RegisterRequest request) {
        log.info("Registration attempt for phone");

        // Clean phone
        String cleanPhone = request.getPhoneNo().replaceAll("[^0-9]", "");

        if (cleanPhone.length() < 10 || cleanPhone.length() > 15) {
            throw new BadRequestException("Phone number must be 10-15 digits");
        }

        if (userRepository.existsByPhoneNo(cleanPhone)) {
            throw new BadRequestException("Phone number already registered");
        }

        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role specified");
        }

        User user = User.builder()
                .phoneNo(cleanPhone)
                .email(request.getEmail())
                .fullName(request.getFullName())
                .role(role)
                .isActive(true)
                .isVerified(false)
                .build();

        user = userRepository.save(user);

        // Save rental profile if applicable
        if (role == Role.RENTAL) {
            if (request.getApartmentNo() == null || request.getTotalMembers() == null) {
                throw new BadRequestException("Apartment number and total members required for rental");
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

        log.info("User registered: ID={}, Role={}", user.getId(), user.getRole());
        return "Registration successful. Please login with OTP.";
    }


    public String sendOtp(String phoneNo) {
        //Clean phone number
        String cleanPhone = cleanPhoneNumber(phoneNo);
        log.info("OTP request for: {}", PhoneMaskUtil.mask(cleanPhone));

        User user = userRepository.findByPhoneNo(cleanPhone)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Phone number not registered. Please register first."));

        if (!user.getIsActive()) {
            throw new BadRequestException("Account is deactivated. Contact admin.");
        }

        otpService.sendOtp(cleanPhone);
        return "OTP sent successfully. Valid for " + twilioProperties.getExpirationMinutes() + " minutes.";
    }

    @Transactional
    public AuthResponse verifyOtpAndLogin(OtpVerificationRequest request) {
        //Clean phone number
        String cleanPhone = cleanPhoneNumber(request.getPhoneNo());
        log.info("OTP verification attempt for: {}", PhoneMaskUtil.mask(cleanPhone));

        boolean isValid = otpService.verifyOtp(cleanPhone, request.getOtp());

        if (!isValid) {
            log.warn("OTP verification failed for: {}", PhoneMaskUtil.mask(cleanPhone));
            throw OtpException.verifyFailed();
        }

        User user = userRepository.findByPhoneNo(cleanPhone)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getIsVerified()) {
            user.setIsVerified(true);
            userRepository.save(user);
        }

        String token = jwtUtil.generateToken(
                user.getPhoneNo(),
                user.getRole().name(),
                user.getId()
        );

        log.info("User logged in: ID={}, Role={}", user.getId(), user.getRole());

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

    /**
     * Clean phone number
     * Removes: +, spaces, dashes, parentheses
     * Keeps: only digits
     */
    private String cleanPhoneNumber(String phoneNo) {
        if (phoneNo == null) {
            throw new BadRequestException("Phone number is required");
        }
        // Remove all non-digit characters
        String cleaned = phoneNo.replaceAll("[^0-9]", "");

        if (cleaned.length() < 10 || cleaned.length() > 15) {
            throw new BadRequestException("Phone number must be 10-15 digits");
        }

        return cleaned;
    }
}
