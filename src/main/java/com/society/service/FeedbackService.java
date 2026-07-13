package com.society.service;

import com.society.dto.request.FeedbackRequest;
import com.society.dto.response.FeedbackResponse;
import com.society.entity.Issue;
import com.society.entity.IssueFeedback;
import com.society.entity.IssueStatus;
import com.society.entity.User;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.IssueFeedbackRepository;
import com.society.repository.IssueRepository;
import com.society.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackService {

    private final IssueFeedbackRepository issueFeedbackRepository;
    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    @Transactional
    public FeedbackResponse submitFeedback(FeedbackRequest request, Long rentalId){
        Issue issue = issueRepository.findById(request.getIssueId())
                .orElseThrow(()-> new ResourceNotFoundException("Issue not found with id: "+ request.getIssueId()));

        if (!issue.getRental().getId().equals(rentalId)){
            throw new BadRequestException("You can only give feedback on your own issues.");
        }

        if (issue.getStatus() != IssueStatus.COMPLETED){
            throw new BadRequestException("Feedback allowed only after issue is completed.");
        }

        if (issueFeedbackRepository.findByIssueIdAndRentalId(request.getIssueId(), rentalId).isPresent()){
            throw new BadRequestException("Feedback already submitted for this issue.");
        }

        User rental = userRepository.findById(rentalId)
                .orElseThrow(()-> new ResourceNotFoundException("Rental not found"));

        IssueFeedback feedback = IssueFeedback.builder()
                .issue(issue)
                .rental(rental)
                .rating(request.getRating())
                .comments(request.getComments())
                .build();

        feedback = issueFeedbackRepository.save(feedback);
        log.info("Feedback submitted for issue: {}", request.getIssueId());
        return mapToResponse(feedback);
    }

    public List<FeedbackResponse> getFeedbackForIssue(Long issueId){
        return issueFeedbackRepository.findByIssueId(issueId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public Double getFeedbackRating(Long issueId){
        List<IssueFeedback> feedbacks = issueFeedbackRepository.findByIssueId(issueId);
        if (feedbacks.isEmpty()){
            return 0.0;
        }

        return feedbacks.stream().mapToInt(IssueFeedback::getRating)
                .average()
                .orElse(0.0);
    }

    private FeedbackResponse mapToResponse(IssueFeedback feedback){
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .issueId(feedback.getIssue().getId())
                .rentalId(feedback.getRental().getId())
                .rentalName(feedback.getRental().getFullName())
                .rating(feedback.getRating())
                .comments(feedback.getComments())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
