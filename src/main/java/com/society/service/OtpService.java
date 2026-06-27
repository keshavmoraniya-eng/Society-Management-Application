package com.society.service;

import com.society.entity.OtpRecord;
import com.society.exception.BadRequestException;
import com.society.repository.OtpRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;

    @Value("${twilio.enabled}")
    private boolean twilioEnabled;

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String twilioPhone;

    @Value("${otp.expiration-minutes}")
    private int expirationMinutes;

    @Value("${otp.length}")
    private int otpLength;

    private final Random random = new Random();

    @Transactional
    public String generateAndSendOtp(String phoneNo) {
        // Mark old OTPs as used
        otpRepository.findLatestActiveOtp(phoneNo).ifPresent(otp -> {
            otp.setIsUsed(true);
            otpRepository.save(otp);
        });

        String otp = generateOtp();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(expirationMinutes);

        OtpRecord record = OtpRecord.builder()
                .phoneNo(phoneNo)
                .otp(otp)
                .expiryTime(expiry)
                .isUsed(false)
                .attempts(0)
                .build();

        otpRepository.save(record);
        sendOtpSms(phoneNo, otp);

        log.info("OTP generated for phone: {}", phoneNo);
        return otp;
    }

    @Transactional
    public boolean verifyOtp(String phoneNo, String otp) {
        Optional<OtpRecord> recordOpt = otpRepository.findLatestActiveOtp(phoneNo);

        if (recordOpt.isEmpty()) {
            throw new BadRequestException("No OTP found. Please request a new one");
        }

        OtpRecord record = recordOpt.get();

        if (record.getExpiryTime().isBefore(LocalDateTime.now())) {
            record.setIsUsed(true);
            otpRepository.save(record);
            throw new BadRequestException("OTP expired. Please request a new one");
        }

        if (record.getAttempts() >= 5) {
            record.setIsUsed(true);
            otpRepository.save(record);
            throw new BadRequestException("Too many attempts. Please request a new OTP");
        }

        record.setAttempts(record.getAttempts() + 1);

        if (!record.getOtp().equals(otp)) {
            otpRepository.save(record);
            throw new BadRequestException("Invalid OTP");
        }

        record.setIsUsed(true);
        otpRepository.save(record);
        log.info("OTP verified successfully for: {}", phoneNo);
        return true;
    }

    private String generateOtp() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private void sendOtpSms(String phoneNo, String otp) {
        if (twilioEnabled) {
            try {
                Twilio.init(accountSid, authToken);
                Message.creator(
                        new PhoneNumber("+91" + phoneNo),
                        new PhoneNumber(twilioPhone),
                        "Your Society Management OTP is: " + otp + ". Valid for " + expirationMinutes + " minutes."
                ).create();
                log.info("OTP sent via Twilio to: {}", phoneNo);
            } catch (Exception e) {
                log.error("Failed to send OTP via Twilio: {}", e.getMessage());
            }
        } else {
            log.info("OTP for {} (DEV MODE): {}", phoneNo, otp);
        }
    }
}
