package com.society.dto.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityGuardResponse {
    private Long id;
    private String fullName;
    private String phoneNo;
    private String email;
    private String profileImage;
    private String employeeId;
    private String shiftType;
    private Integer gateNumber;
    private String emergencyContact;
    private LocalDate joiningDate;
}
