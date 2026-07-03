package com.society.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerificationRequest {
    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$")
    private String phoneNo;

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$")
    private String otp;
}
