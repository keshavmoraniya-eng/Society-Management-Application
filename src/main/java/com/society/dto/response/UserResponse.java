package com.society.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String phoneNo;
    private String email;
    private String fullName;
    private String profileImageUrl;
    private String role;
    private Boolean isActive;
    private Boolean isVerified;

    //Rental Details
    private String apartmentNo;
    private Integer totalMembers;
    private String jobProfile;
    private String workingLocation;
    private String bloodGroup;
    private LocalDate moveInDate;

    //Security guard details
    private String employeeId;
    private String shiftType;
    private Integer gateNumber;

}
