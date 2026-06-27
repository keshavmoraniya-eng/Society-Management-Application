package com.society.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementRequest {

    @NotBlank
    @Size(max = 200)
    private String title;

    private String description;

    private String category;

    private String imageUrl;
}