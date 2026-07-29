package com.society.dto.response;

import com.society.entity.ApprovalStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationStatusResponse {
    private Long userId;
    private String fullName;
    private String phoneNo;
    private String email;
    private ApprovalStatus approvalStatus;
    private String apartmentNo;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewerComment;
    private String buildingName;
    private LocalDateTime moveInDate;
    private String leaseTerm;
}