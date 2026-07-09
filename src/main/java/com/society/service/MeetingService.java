package com.society.service;

import com.society.dto.request.MeetingRequest;
import com.society.dto.response.MeetingResponse;
import com.society.entity.Meeting;
import com.society.entity.Role;
import com.society.entity.User;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.MeetingRepository;
import com.society.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;

    @Transactional
    public MeetingResponse scheduleMeeting(MeetingRequest request, Long managerId){
        User manager = userRepository.findById(managerId)
                .orElseThrow(()->new ResourceNotFoundException("Manager not found"));

        if (manager.getRole() != Role.SOCIETY_MANAGER && manager.getRole() != Role.SOCIETY_OWNER){
            throw new BadRequestException("Only managers can schedule meetings");
        }

        DayOfWeek day = request.getMeetingDate().getDayOfWeek();
        if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY){
            throw new BadRequestException("Meetings can only be schedule on weekends (Saturday/Sunday)");
        }

        if (request.getEndTime().isBefore(request.getStartTime()) || request.getEndTime().equals(request.getStartTime())){
            throw new BadRequestException("End time must be after start time");
        }

        Meeting meeting = Meeting.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .venue(request.getVenue())
                .meetingDate(request.getMeetingDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .agenda(request.getAgenda())
                .organizedBy(manager)
                .build();

        meeting = meetingRepository.save(meeting);
        log.info("Meeting scheduled: {} on {}", meeting.getId(), meeting.getMeetingDate());
        return mapToResponse(meeting);
    }


    public List<MeetingResponse> getAllMeetings(){
        return meetingRepository.findAllByOrderByMeetingDateDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<MeetingResponse> getUpcomingMeetings(){
        return meetingRepository.findUpComingMeetings(LocalDate.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public MeetingResponse getMeetingById(Long id){
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Meeting not found"));
        return mapToResponse(meeting);
    }

    @Transactional
    public void cancelMeeting(Long id, Long managerId){
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Meeting not found"));

        User manager = userRepository.findById(managerId)
                .orElseThrow(()->new ResourceNotFoundException("Manager not found"));

        if (!meeting.getOrganizedBy().getId().equals(managerId) && manager.getRole() != Role.SOCIETY_OWNER){
            throw new BadRequestException("Cannot cancel meeting organized by another manager");
        }
        meetingRepository.delete(meeting);
        log.info("Meeting canceled: {} on {}", meeting.getId(), meeting.getMeetingDate());
    }

    private MeetingResponse mapToResponse(Meeting meeting){
        return MeetingResponse.builder()
                .id(meeting.getId())
                .title(meeting.getTitle())
                .description(meeting.getDescription())
                .venue(meeting.getVenue())
                .meetingDate(meeting.getMeetingDate())
                .startTime(meeting.getStartTime())
                .endTime(meeting.getEndTime())
                .agenda(meeting.getAgenda())
                .dayOfWeek(meeting.getMeetingDate().getDayOfWeek().name())
                .organizedByName(meeting.getOrganizedBy().getFullName())
                .createdAt(meeting.getCreatedAt())
                .build();
    }
}
