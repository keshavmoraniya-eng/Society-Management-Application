package com.society.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank(message = "Full name required")
    @Size(min = 2,max = 100)
    private String fullName;

    @NotBlank(message = "Phone required")
    @Pattern(regexp = "^[0-9]{10}$",message = "Phone must be 10 digits")
    private String phoneNo;

    @Email
    private String email;

    @NotBlank(message = "Role required")
    private String role;

    //Rental-specific fields
    private String apartmentNo;

    private Integer totalMembers;

    private String jobProfile;

    private String workingLocation;

    private String bloodGroup;

    //Security guard fields
    private String employeeId;

    private String shiftType;

    private Integer gateNumber;

    private String emergencyContact;


}
