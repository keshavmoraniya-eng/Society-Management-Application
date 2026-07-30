package com.society.service;

import com.society.dto.request.MeetingRequest;
import com.society.dto.response.MeetingResponse;
import com.society.entity.Meeting;
import com.society.entity.Role;
import com.society.entity.User;
import com.society.repository.MeetingRepository;
import com.society.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MeetingServiceTest {
    @Mock private MeetingRepository meetingRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private MeetingService meetingService;

    @Test
    @DisplayName("Schedule meeting on Saturday - Success")
    void scheduleMeeting_Saturday_Success() {

        User manager = User.builder()
                .id(1L)
                .fullName("Keshav Moraniya")
                .role(Role.SOCIETY_MANAGER)
                .build();

        LocalDate saturday = LocalDate.now()
                .with(java.time.temporal.TemporalAdjusters.next(DayOfWeek.SATURDAY));

        MeetingRequest request = MeetingRequest.builder()
                .title("Annual Meet")
                .venue("Clubhouse")
                .meetingDate(saturday)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(manager));

        when(meetingRepository.save(any(Meeting.class)))
                .thenAnswer(invocation -> {
                    Meeting meeting = invocation.getArgument(0);
                    meeting.setId(1L);
                    return meeting;
                });

        MeetingResponse response =
                meetingService.scheduleMeeting(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Annual Meet");
        assertThat(response.getVenue()).isEqualTo("Clubhouse");
        assertThat(response.getDayOfWeek()).isEqualTo("SATURDAY");
        assertThat(response.getOrganizedByName()).isEqualTo("Keshav Moraniya");
    }

    
}
