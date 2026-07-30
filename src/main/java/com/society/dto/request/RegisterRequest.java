package com.society.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9]{10}$|^\\+[0-9]{10,15}$",
            message = "Phone must be 10 digits (with optional + prefix)"
    )
    private String phoneNo;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Role is required")
    @Pattern(
            regexp = "^(RENTAL|SOCIETY_MANAGER|SOCIETY_OWNER|SECURITY_GUARD)$",
            message = "Role must be RENTAL, SOCIETY_MANAGER, SOCIETY_OWNER, or SECURITY_GUARD"
    )
    private String role;

    // Rental fields
    private String apartmentNo;
    private Integer totalMembers;
    private String jobProfile;
    private String workingLocation;
    private String bloodGroup;

    // Security guard fields
    private String employeeId;
    private String shiftType;
    private Integer gateNumber;
    private String emergencyContact;
}
