package com.society.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpVerificationRequest {

    @NotBlank
    @Pattern(
            regexp = "^[0-9]{10,15}$|^\\+[0-9]{10,15}$",
            message = "Phone must be 10-15 digits (with optional + prefix)"
    )
    private String phoneNo;

    @NotBlank
    @Pattern(regexp = "^[0-9]{4,8}$", message = "OTP must be 4-8 digits")
    private String otp;
}
