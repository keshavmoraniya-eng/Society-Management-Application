package com.society.service;

import com.society.dto.request.FeedbackRequest;
import com.society.dto.response.FeedbackResponse;
import com.society.entity.*;
import com.society.exception.BadRequestException;
import com.society.repository.IssueFeedbackRepository;
import com.society.repository.IssueRepository;
import com.society.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private IssueFeedbackRepository issueFeedbackRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    @Test
    @DisplayName("Submit feedback - Success")
    void submitFeedback_Success() {

        User rental = User.builder()
                .id(1L)
                .fullName("Keshav Moraniya")
                .phoneNo("9876543210")
                .role(Role.RENTAL)
                .build();

        Issue issue = Issue.builder()
                .id(1L)
                .rental(rental)
                .status(IssueStatus.COMPLETED)
                .build();

        FeedbackRequest request = FeedbackRequest.builder()
                .issueId(1L)
                .rating(5)
                .comments("Great Service")
                .build();

        when(issueRepository.findById(1L))
                .thenReturn(Optional.of(issue));

        when(issueFeedbackRepository.findByIssueIdAndRentalId(1L, 1L))
                .thenReturn(Optional.empty());

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(rental));

        when(issueFeedbackRepository.save(any(IssueFeedback.class)))
                .thenAnswer(invocation -> {
                    IssueFeedback feedback = invocation.getArgument(0);
                    feedback.setId(1L);
                    return feedback;
                });

        FeedbackResponse response =
                feedbackService.submitFeedback(request, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getIssueId()).isEqualTo(1L);
        assertThat(response.getRentalId()).isEqualTo(1L);
        assertThat(response.getRentalName()).isEqualTo("Keshav Moraniya");
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getComments()).isEqualTo("Great Service");
    }

    @Test
    @DisplayName("Submit feedback for incomplete issue - Throws")
    void submitFeedback_IncompleteIssue_Throws() {

        User rental = User.builder()
                .id(1L)
                .fullName("Keshav Moraniya")
                .role(Role.RENTAL)
                .build();

        Issue issue = Issue.builder()
                .id(1L)
                .rental(rental)
                .status(IssueStatus.IN_PROGRESS)
                .build();

        FeedbackRequest request = FeedbackRequest.builder()
                .issueId(1L)
                .rating(5)
                .comments("Good")
                .build();

        when(issueRepository.findById(1L))
                .thenReturn(Optional.of(issue));

        assertThatThrownBy(() ->
                feedbackService.submitFeedback(request, 1L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Feedback allowed only after issue is completed.");
    }
}