package com.society.service;

import com.society.exception.OtpException;
import com.twilio.Twilio;
import com.twilio.exception.TwilioException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private static final String SMS = "sms";
    private static final String APPROVED = "approved";

    private static final String AUTH_ERROR = "20003";
    private static final String SERVICE_NOT_FOUND = "20404";
    private static final String INVALID_PHONE = "21211";

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.verify-service-sid}")
    private String verifyServiceSid;

    @Value("${twilio.enabled:false}")
    private boolean enabled;

    @Value("${twilio.country-code:+91}")
    private String countryCode;

    @PostConstruct
    public void initialize() {

        if (!isConfigured()) {
            log.warn("Twilio is disabled or configuration is missing.");
            return;
        }

        Twilio.init(accountSid, authToken);

        log.info("Twilio Verify initialized successfully.");
    }

    public String sendOtp(String phoneNumber) {

        validateConfiguration();

        String phone = formatPhoneNumber(phoneNumber);

        try {

            Verification verification = Verification.creator(
                    verifyServiceSid,
                    phone,
                    SMS
            ).create();

            log.info("OTP sent successfully to {}", phone);

            return verification.getSid();

        } catch (TwilioException ex) {

            throw mapException(ex);

        }

    }

    public boolean verifyOtp(String phoneNumber, String otp) {

        validateConfiguration();

        String phone = formatPhoneNumber(phoneNumber);

        try {

            VerificationCheck verification = VerificationCheck.creator(verifyServiceSid)
                    .setTo(phone)
                    .setCode(otp)
                    .create();

            boolean approved = APPROVED.equalsIgnoreCase(verification.getStatus());

            log.info("OTP verification status : {}", verification.getStatus());

            return approved;

        } catch (TwilioException ex) {

            log.error("OTP verification failed : {}", ex.getMessage());

            return false;

        }

    }

    private void validateConfiguration() {

        if (!isConfigured()) {
            throw new OtpException("Twilio is not configured.");
        }

    }

    private boolean isConfigured() {

        return enabled
                && isValid(accountSid, "AC")
                && isValid(verifyServiceSid, "VA")
                && authToken != null
                && !authToken.isBlank();

    }

    private boolean isValid(String value, String prefix) {

        return value != null
                && !value.isBlank()
                && value.startsWith(prefix);

    }

    private RuntimeException mapException(TwilioException ex) {

        return switch (ex.getMessage()) {

            case AUTH_ERROR ->
                    new OtpException("Invalid Twilio credentials.");

            case SERVICE_NOT_FOUND ->
                    new OtpException("Verify Service SID not found.");

            case INVALID_PHONE ->
                    new OtpException("Invalid phone number.");

            default ->
                    new OtpException("Failed to send OTP.", ex);

        };

    }

    private String formatPhoneNumber(String phoneNumber) {

        String digits = phoneNumber.replaceAll("\\D", "");

        if (digits.startsWith("0")) {
            digits = digits.substring(1);
        }

        if (!digits.startsWith(countryCode.replace("+", ""))) {
            digits = countryCode.replace("+", "") + digits;
        }

        return "+" + digits;

    }

}