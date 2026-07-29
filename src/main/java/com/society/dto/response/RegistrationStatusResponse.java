package com.society.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime submittedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewedAt;

    private String reviewerComment;
    private String buildingName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime moveInDate;

    private String leaseTerm;
}