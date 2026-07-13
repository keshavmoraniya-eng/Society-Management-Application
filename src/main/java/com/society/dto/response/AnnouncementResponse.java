package com.society.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String imageUrl;
    private String postedByName;
    private LocalDateTime createdAt;
}
