package com.society.service;

import com.society.dto.request.IssueAssignRequest;
import com.society.dto.request.IssueRequest;
import com.society.dto.response.IssueResponse;
import com.society.entity.*;
import com.society.exception.BadRequestException;
import com.society.repository.IssueRepository;
import com.society.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private IssueService issueService;

    @Test
    @DisplayName("Create issue - Success")
    void createIssue_Success() {

        User rental = User.builder()
                .id(1L)
                .phoneNo("7987948819")
                .fullName("John")
                .role(Role.RENTAL)
                .build();

        IssueRequest request = IssueRequest.builder()
                .title("Leakage")
                .description("Kitchen sink")
                .issueType("PLUMBING")
                .priority("HIGH")
                .apartmentNo("A-101")
                .build();

        Issue saved = Issue.builder()
                .id(1L)
                .title("Leakage")
                .description("Kitchen sink")
                .rental(rental)
                .issueType(IssueType.PLUMBING)
                .priority(Priority.HIGH)
                .status(IssueStatus.PENDING)
                .apartmentNo("A-101")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(issueRepository.save(any(Issue.class))).thenReturn(saved);

        IssueResponse response = issueService.createIssue(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Leakage");
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getPriority()).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("Create issue - Non rental user throws exception")
    void createIssue_NonRental_Throws() {

        User manager = User.builder()
                .id(1L)
                .role(Role.SOCIETY_MANAGER)
                .build();

        IssueRequest request = IssueRequest.builder()
                .title("Leakage")
                .issueType("OTHER")
                .priority("LOW")
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(manager));

        assertThatThrownBy(() ->
                issueService.createIssue(request, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Only rentals can create issue");
    }

    @Test
    @DisplayName("Approve issue - Success")
    void approveIssue_Success() {

        User rental = User.builder()
                .id(1L)
                .fullName("John")
                .phoneNo("9999999999")
                .role(Role.RENTAL)
                .build();

        User manager = User.builder()
                .id(2L)
                .role(Role.SOCIETY_MANAGER)
                .build();

        Issue issue = Issue.builder()
                .id(1L)
                .title("Leakage")
                .description("Kitchen sink")
                .rental(rental)
                .issueType(IssueType.PLUMBING)
                .priority(Priority.HIGH)
                .status(IssueStatus.PENDING)
                .apartmentNo("A-101")
                .build();

        when(issueRepository.findById(1L))
                .thenReturn(Optional.of(issue));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(manager));

        when(issueRepository.save(any(Issue.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IssueResponse response = issueService.approveIssue(1L, 2L);

        assertThat(response.getStatus()).isEqualTo("APPROVED");
    }

    @Test
    @DisplayName("Assign issue - Success")
    void assignIssue_Success() {

        User rental = User.builder()
                .id(1L)
                .fullName("John")
                .phoneNo("9999999999")
                .role(Role.RENTAL)
                .build();

        User manager = User.builder()
                .id(2L)
                .role(Role.SOCIETY_MANAGER)
                .build();

        User fixer = User.builder()
                .id(3L)
                .fullName("Security")
                .phoneNo("8888888888")
                .role(Role.SECURITY_GUARD)
                .build();

        Issue issue = Issue.builder()
                .id(1L)
                .title("Leakage")
                .description("Kitchen sink")
                .rental(rental)
                .issueType(IssueType.PLUMBING)
                .priority(Priority.HIGH)
                .status(IssueStatus.APPROVED)
                .apartmentNo("A-101")
                .build();

        IssueAssignRequest request = IssueAssignRequest.builder()
                .fixerId(3L)
                .estimatedCost(new BigDecimal("500"))
                .remarks("Assign to security")
                .build();

        when(issueRepository.findById(1L))
                .thenReturn(Optional.of(issue));

        when(userRepository.findById(2L))
                .thenReturn(Optional.of(manager));

        when(userRepository.findById(3L))
                .thenReturn(Optional.of(fixer));

        when(issueRepository.save(any(Issue.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        IssueResponse response =
                issueService.assignIssue(1L, request, 2L);

        assertThat(response.getStatus()).isEqualTo("ASSIGNED");
        assertThat(response.getAssignedToId()).isEqualTo(3L);
    }
}