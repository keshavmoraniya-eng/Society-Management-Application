package com.society.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OtpException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final boolean showToUser;


    public OtpException(String message, HttpStatus status) {
        this(message, status, "OTP_ERROR", true);
    }

    public OtpException(String message, HttpStatus status, String errorCode) {
        this(message, status, errorCode, true);
    }

    public OtpException(String message, HttpStatus status, String errorCode, boolean showToUser) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
        this.showToUser = showToUser;
    }

    /**
     * Common OTP exceptions
     */
    public static OtpException sendFailed() {
        return new OtpException(
                "Unable to send OTP. Please try again later.",
                HttpStatus.SERVICE_UNAVAILABLE,
                "OTP_SEND_FAILED",
                true
        );
    }

    public static OtpException verifyFailed() {
        return new OtpException(
                "Invalid or expired OTP.",
                HttpStatus.BAD_REQUEST,
                "OTP_INVALID",
                true
        );
    }

    public static OtpException rateLimitExceeded(int seconds) {
        return new OtpException(
                "Too many OTP requests. Please try again in " + seconds + " seconds.",
                HttpStatus.TOO_MANY_REQUESTS,
                "OTP_RATE_LIMIT",
                true
        );
    }

    public static OtpException serviceUnavailable() {
        return new OtpException(
                "OTP service is temporarily unavailable. Please try again later.",
                HttpStatus.SERVICE_UNAVAILABLE,
                "OTP_SERVICE_DOWN",
                true
        );
    }

    public static OtpException invalidPhone() {
        return new OtpException(
                "Invalid phone number format.",
                HttpStatus.BAD_REQUEST,
                "OTP_INVALID_PHONE",
                true
        );
    }

    public static OtpException configurationError() {
        return new OtpException(
                "OTP service is not configured. Please contact support.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "OTP_CONFIG_ERROR",
                false  // Don't show to user
        );
    }

}