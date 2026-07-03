package com.society.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueResponse {
    private Long id;
    private String title;
    private String description;
    private String issueType;
    private String status;
    private String priority;
    private String apartmentNo;
    private Long rentalId;
    private String rentalName;
    private String rentalPhone;
    private String rentalBloodGroup;
    private String rentalJobProfile;
    private Long assignedToId;
    private String assignedToName;
    private String assignedToPhone;
    private BigDecimal estimatedCost;
    private BigDecimal actualCost;
    private String imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

}
