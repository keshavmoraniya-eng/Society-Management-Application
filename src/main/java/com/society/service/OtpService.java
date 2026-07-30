package com.society.service;

import com.society.config.TwilioProperties;
import com.society.exception.OtpException;
import com.society.util.PhoneMaskUtil;
import com.twilio.Twilio;
import com.twilio.exception.TwilioException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 *  SECURE OTP Service using Twilio Verify API
 *
 * Security Best Practices:
 * - Twilio generates the OTP (cryptographically secure)
 * - Twilio stores the OTP (encrypted, with expiration)
 * - Twilio validates the OTP (we never see it)
 * - We only get: approved/pending/failed
 * - Rate limiting per phone number
 * - Phone numbers masked in logs
 * - No OTP ever appears in logs
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OtpService {

    private final TwilioProperties twilioProperties;
    private final PhoneNumberFormatter phoneFormatter;
    private final RateLimitService rateLimitService;

    private boolean twilioInitialized = false;

    /**
     * Initialize Twilio on startup
     */
    @PostConstruct
    public void init() {
        if (twilioProperties.isEnabled() && isValidConfiguration()) {
            try {
                Twilio.init(twilioProperties.getAccountSid(), twilioProperties.getAuthToken());
                twilioInitialized = true;
                log.info("=================================================");
                log.info("   Twilio Verify API Initialized");
                log.info("   Mode: REAL OTP (Production)");
                log.info("   Account: {}***", mask(twilioProperties.getAccountSid()));
                log.info("   Service: {}***", mask(twilioProperties.getVerifyServiceSid()));
                log.info("=================================================");
            } catch (Exception e) {
                twilioInitialized = false;
                log.error("  Failed to initialize Twilio: {}", e.getMessage());
            }
        } else {
            twilioInitialized = false;
            log.warn("=================================================");
            log.warn("   Twilio NOT Initialized");
            log.warn("   Enabled: {}", twilioProperties.isEnabled());
            log.warn("   Has Account SID: {}", hasText(twilioProperties.getAccountSid()));
            log.warn("   Has Verify SID: {}", hasText(twilioProperties.getVerifyServiceSid()));
            log.warn("   Mode: OTP service unavailable");
            log.warn("=================================================");
        }
    }

    /**
     * SECURE: Send OTP via Twilio Verify API
     * - Twilio generates cryptographically secure OTP
     * - Twilio sends SMS to phone
     * - Twilio stores OTP with expiration
     * - We never see or store the OTP
     */
    public void sendOtp(String phoneNo) {
        if (!twilioInitialized) {
            log.error("Cannot send OTP - Twilio not initialized");
            throw OtpException.serviceUnavailable();
        }

        // Rate limiting
        if (!rateLimitService.isOtpRequestAllowed(phoneNo)) {
            log.warn("Rate limit exceeded for OTP request: {}", PhoneMaskUtil.mask(phoneNo));
            throw OtpException.rateLimitExceeded(rateLimitService.getRetryAfterSeconds(phoneNo));
        }

        // Format and validate phone
        String formattedPhone;
        try {
            formattedPhone = phoneFormatter.toE164(phoneNo);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid phone format: {}", e.getMessage());
            throw OtpException.invalidPhone();
        }

        try {
            log.info("Sending OTP via Twilio Verify to: {}", PhoneMaskUtil.mask(formattedPhone));

            // Twilio generates OTP, sends SMS, stores with expiration
            Verification verification = Verification.creator(
                    twilioProperties.getVerifyServiceSid(),
                    formattedPhone,
                    "sms"  // Channel
            ).create();

            log.info("   OTP sent via Twilio Verify");
            log.info("   Verification SID: {}", verification.getSid());
            log.info("   Status: {}", verification.getStatus());
            log.info("   To: {}", PhoneMaskUtil.mask(formattedPhone));
            log.info("   Channel: SMS");
            log.info("   Expires: {} minutes (Twilio controlled)", twilioProperties.getExpirationMinutes());

        } catch (TwilioException e) {
            log.error("Twilio Verify Error: code={}, message={}",e.getCause(), e.getMessage());
            handleTwilioError(e);
        } catch (Exception e) {
            log.error("Unexpected error sending OTP: {}", e.getMessage(), e);
            throw OtpException.sendFailed();
        }
    }

    /**
     *  SECURE: Verify OTP via Twilio Verify API
     * - Twilio validates the OTP
     * - We only get true/false result
     * - We never compare OTPs ourselves
     */
    public boolean verifyOtp(String phoneNo, String otp) {

        if (!twilioInitialized) {
            log.error("Cannot verify OTP - Twilio not initialized");
            throw OtpException.serviceUnavailable();
        }

        // Basic input validation (defense in depth)
        if (otp == null || !otp.matches("^[0-9]{4,8}$")) {
            log.warn("Invalid OTP format received");
            rateLimitService.recordFailedVerify(phoneNo);
            return false;
        }

        // Rate limiting on verify attempts
        if (!rateLimitService.isVerifyAttemptAllowed(phoneNo)) {
            log.warn("Rate limit exceeded for verify attempts: {}", PhoneMaskUtil.mask(phoneNo));
            throw OtpException.rateLimitExceeded(rateLimitService.getRetryAfterSeconds(phoneNo));
        }

        // Format phone
        String formattedPhone;
        try {
            formattedPhone = phoneFormatter.toE164(phoneNo);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid phone format during verify: {}", e.getMessage());
            return false;
        }

        try {
            log.info("Verifying OTP via Twilio for: {}", PhoneMaskUtil.mask(formattedPhone));

            // Send OTP to Twilio for validation
            // We don't see the actual comparison - Twilio does it
            VerificationCheck verificationCheck = VerificationCheck.creator(
                            twilioProperties.getVerifyServiceSid()
                    )
                    .setTo(formattedPhone)
                    .setCode(otp)
                    .create();

            String status = verificationCheck.getStatus();
            boolean isApproved = "approved".equalsIgnoreCase(status);

            if (isApproved) {
                log.info("OTP verified successfully");
                // Reset rate limit on success
                rateLimitService.resetRateLimit(phoneNo);
            } else {
                log.warn("OTP verification failed - Status: {}", status);
                rateLimitService.recordFailedVerify(phoneNo);
            }

            return isApproved;

        } catch (TwilioException e) {
            log.error("Twilio Verify Check Error: code={}, message={}",
                    e.getCause(), e.getMessage());
            rateLimitService.recordFailedVerify(phoneNo);

            // Specific error handling
            if (Objects.equals(e.getMessage(), "60200")) {
                // Max verification attempts reached
                log.warn("Max verification attempts reached for: {}", PhoneMaskUtil.mask(phoneNo));
            }

            return false;
        } catch (Exception e) {
            log.error("Unexpected verification error: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Handle Twilio-specific errors
     * Maps Twilio error codes to user-friendly messages
     */
    private void handleTwilioError(TwilioException e) {
        switch (e.getMessage()) {
            case "20003":
                log.error("Twilio authentication failed - check credentials");
                throw OtpException.configurationError();

            case "20404":
                log.error("Verify Service not found - check Service SID");
                throw OtpException.configurationError();

            case "21211":
                log.error("Invalid phone number format");
                throw OtpException.invalidPhone();

            case "21610":
                log.error("Phone number not verified (trial account limitation)");
                throw new OtpException(
                        "This phone number cannot receive SMS. Please contact support.",
                        org.springframework.http.HttpStatus.FORBIDDEN,
                        "OTP_PHONE_NOT_VERIFIED",
                        true
                );

            case "21614":
                log.error("Phone number cannot receive SMS");
                throw new OtpException(
                        "This phone number cannot receive SMS.",
                        org.springframework.http.HttpStatus.FORBIDDEN,
                        "OTP_SMS_BLOCKED",
                        true
                );

            case "60200":
                log.error("Too many OTP requests to Twilio");
                throw OtpException.rateLimitExceeded(300);

            case "60202":
                log.error("Verification code expired");
                throw OtpException.verifyFailed();

            default:
                log.error("Unhandled Twilio error: code={}", e.getMessage());
                throw OtpException.sendFailed();
        }
    }

    /**
     * Validate Twilio configuration
     */
    private boolean isValidConfiguration() {
        return hasText(twilioProperties.getAccountSid())
                && twilioProperties.getAccountSid().startsWith("AC")
                && hasText(twilioProperties.getAuthToken())
                && hasText(twilioProperties.getVerifyServiceSid())
                && twilioProperties.getVerifyServiceSid().startsWith("VA");
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private String mask(String value) {
        if (value == null || value.length() < 4) return "***";
        return value.substring(0, 4) + "***";
    }
}
