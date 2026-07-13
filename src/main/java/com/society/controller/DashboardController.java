package com.society.controller;

import com.society.dto.response.ApiResponse;
import com.society.entity.IssueStatus;
import com.society.entity.Role;
import com.society.entity.User;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.AnnouncementRepository;
import com.society.repository.IssueRepository;
import com.society.repository.MeetingRepository;
import com.society.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Controller")
public class DashboardController {

    private final UserRepository userRepository;
    private final IssueRepository issueRepository;
    private final MeetingRepository meetingRepository;
    private final AnnouncementRepository announcementRepository;

    @GetMapping("/rental")
    @PreAuthorize("hasRole('RENTAL')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rentalDashboard(@AuthenticationPrincipal UserDetails userDetails){
        User user = userRepository.findByPhoneNo(userDetails.getUsername())
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("user", user);
        dashboard.put("myIssues", issueRepository.findByRentalIdOrderByCreatedAtDesc(user.getId()));
        dashboard.put("upcomingMeetings", meetingRepository.findUpComingMeetings(LocalDate.now()));
        dashboard.put("recentAnnouncements", announcementRepository.findAllByOrderByCreatedAtDesc());
        return  ResponseEntity.ok(ApiResponse.success("Dashboard data", dashboard));
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('SOCIETY_MANAGER', 'SOCIETY_OWNER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> managerDashboard(){
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalRentals", userRepository.countByRole(Role.RENTAL));
        dashboard.put("totalGuards", userRepository.countByRole(Role.SECURITY_GUARD));
        dashboard.put("pendingIssues", issueRepository.countByStatus(IssueStatus.PENDING));
        dashboard.put("inProgressIssues", issueRepository.countByStatus(IssueStatus.IN_PROGRESS));
        dashboard.put("upcomingMeetings", meetingRepository.findUpComingMeetings(LocalDate.now()));
        return ResponseEntity.ok(ApiResponse.success("Manager dashboard", dashboard));

    }
}
