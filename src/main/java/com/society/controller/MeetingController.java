package com.society.controller;

import com.society.dto.request.MeetingRequest;
import com.society.dto.response.ApiResponse;
import com.society.dto.response.MeetingResponse;
import com.society.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/meetings")
@RequiredArgsConstructor
@Tag(name = "Meeting Management", description = "APIs for managing meetings")
@SecurityRequirement(name = "bearerAuth")
public class MeetingController {
    private final MeetingService meetingService;

    @PostMapping("/schedule")
    @PreAuthorize("hasRole('SOCIETY_MANAGER'), hasRole('SOCIETY_OWNER')")
    @Operation(summary = "Schedule a new meeting", description = "Allows society managers or owners to schedule a new meeting. Meetings can only be scheduled on weekends (Saturday/Sunday).")
    public ResponseEntity<ApiResponse<MeetingResponse>> scheduleMeeting(@Valid @RequestBody MeetingRequest request, @AuthenticationPrincipal UserDetails userDetails){
        Long managerId = 1L;

        MeetingResponse response = meetingService.scheduleMeeting(request, managerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Meeting scheduled successfully",response));
    }

    @GetMapping("/all")
    @Operation(summary = "Get all meetings", description = "Retrieves a list of all scheduled meetings.")
    public ResponseEntity<ApiResponse<List<MeetingResponse>>> getAllMeetings(){
        return ResponseEntity.ok(ApiResponse.success("List of all meetings", meetingService.getAllMeetings()));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming meetings", description = "Retrieves a list of all upcoming meetings scheduled for future dates.")
    public ResponseEntity<ApiResponse<List<MeetingResponse>>> getUpcomingMeetings(){
        return ResponseEntity.ok(ApiResponse.success("List of upcoming meetings", meetingService.getUpcomingMeetings()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get meeting by ID", description = "Retrieves the details of a specific meeting by its ID.")
    public ResponseEntity<ApiResponse<MeetingResponse>> getMeetingById(@PathVariable Long id){
        return ResponseEntity.ok(ApiResponse.success("Meeting details", meetingService.getMeetingById(id)));
    }

    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("hasRole('SOCIETY_MANAGER'), hasRole('SOCIETY_OWNER')")
    @Operation(summary = "Cancel a meeting", description = "Allows society managers or owners to cancel a scheduled meeting by its ID.")
    public ResponseEntity<ApiResponse<Void>> cancelMeeting(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails){
        meetingService.cancelMeeting(id, 1L);
        return ResponseEntity.ok(ApiResponse.success("Meeting cancelled successfully", null));
    }


}
