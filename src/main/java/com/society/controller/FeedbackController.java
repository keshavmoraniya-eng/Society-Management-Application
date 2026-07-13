package com.society.controller;

import com.society.dto.request.FeedbackRequest;
import com.society.dto.response.ApiResponse;
import com.society.dto.response.FeedbackResponse;
import com.society.service.FeedbackService;
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
@RequestMapping("/feedback")
@RequiredArgsConstructor
@Tag(name = "Feedback", description = "Endpoints for submitting feedback on issues")
public class FeedbackController {
    private final FeedbackService feedbackService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('RENTAL')")
    public ResponseEntity<ApiResponse<FeedbackResponse>> create(@Valid @RequestBody FeedbackRequest request, @AuthenticationPrincipal UserDetails userDetails){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feedback submitted successfully", feedbackService.submitFeedback(request, 1L)));

    }

    @GetMapping("/issue/{issueId}")
    @PreAuthorize("hasRole('RENTAL') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getForIssue(@PathVariable Long issueId){
        return ResponseEntity.ok(ApiResponse.success("Feedbacks retrieved successfully", feedbackService.getFeedbackForIssue(issueId)));
    }
}
