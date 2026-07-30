package com.society.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Pattern;

@Configuration
@ConfigurationProperties(prefix = "twilio")
@Validated
@Data
public class TwilioProperties {

    private boolean enabled = false;

    //Pattern allows empty OR valid format
    @Pattern(
            regexp = "^AC[a-zA-Z0-9]{32}$|^$",
            message = "Invalid Twilio Account SID"
    )
    private String accountSid;

    private String authToken;

    @Pattern(
            regexp = "^VA[a-zA-Z0-9]{32}$|^$",
            message = "Invalid Verify Service SID"
    )
    private String verifyServiceSid;


    private String defaultCountryCode = "91";
    private int expirationMinutes = 5;
    private int maxRequestsPerWindow = 3;
    private int maxVerifyAttempts = 5;
    private int rateLimitWindowMinutes = 5;

    public boolean isValidConfiguration() {
        if (!enabled) return false;
        return accountSid != null
                && accountSid.matches("^AC[a-zA-Z0-9]{32}$")
                && authToken != null
                && !authToken.trim().isEmpty()
                && verifyServiceSid != null
                && verifyServiceSid.matches("^VA[a-zA-Z0-9]{32}$");
    }
}
