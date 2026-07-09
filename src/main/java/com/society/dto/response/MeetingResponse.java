package com.society.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingResponse {
    private Long id;
    private String title;
    private String description;
    private String venue;
    private LocalDate meetingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String agenda;
    private String dayOfWeek;
    private String organizedByName;
    private LocalDateTime createdAt;
}
