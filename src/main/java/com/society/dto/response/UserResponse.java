package com.society.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String approvalStatus;

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

    // Timestamps
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

}
