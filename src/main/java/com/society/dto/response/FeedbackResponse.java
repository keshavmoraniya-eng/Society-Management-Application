package com.society.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {
    private Long id;
    private Long issueId;
    private Long rentalId;
    private String rentalName;
    private Integer rating;
    private String comments;
    private LocalDateTime createdAt;
}
