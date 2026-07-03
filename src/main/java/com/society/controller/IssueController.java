package com.society.controller;

import com.society.dto.request.IssueAssignRequest;
import com.society.dto.request.IssueRequest;
import com.society.dto.response.ApiResponse;
import com.society.dto.response.IssueResponse;
import com.society.entity.CustomUserDetails;
import com.society.entity.IssueStatus;
import com.society.service.IssueService;
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
import java.util.Map;

@RestController
@RequestMapping("/issue")
@RequiredArgsConstructor
@Tag(name = "Phase 3. Issue Management")
@SecurityRequirement(name = "bearerAuth")
public class IssueController {
    private final IssueService issueService;

    @PostMapping("/rental/create")
    @Operation(summary = "Create new issue (Rental only)")
    @PreAuthorize("hasRole('RENTAL')")
    public ResponseEntity<ApiResponse<IssueResponse>> createIssue(@Valid @RequestBody IssueRequest request, @AuthenticationPrincipal UserDetails userDetails){
        Long rentalId = getCurrentUserId(userDetails);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Issue created", issueService.createIssue(request, rentalId)));

    }

    @GetMapping("/rental/my-issues")
    @PreAuthorize("hasRole('RENTAL')")
    @Operation(summary = "Get my issue (Rental)")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> getMyIssue(@AuthenticationPrincipal UserDetails userDetails){
        Long rentalId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Issue fetched", issueService.getIssuesByRental(rentalId)));
    }

    @GetMapping("/manage/all")
    @PreAuthorize("hasAnyRole('SOCIETY_MANAGER','SOCIETY_OWNER')")
    @Operation(summary = "Get all issues (Manager/Owner)")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> getAllIssues(){
        return ResponseEntity.ok(ApiResponse.success("All Issues", issueService.getAllIssues()));
    }

    @GetMapping("/manage/by-status")
    @PreAuthorize("hasAnyRole('SOCIETY_MANAGER','SOCIETY_OWNER')")
    public ResponseEntity<ApiResponse<List<IssueResponse>>> getByStatus(@RequestParam IssueStatus status){
        return ResponseEntity.ok(ApiResponse.success("Issue", issueService.getIssuesByStatus(status)));
    }

    @GetMapping("/issue/{id}")
    @Operation(summary = "Get issue by id")
    public ResponseEntity<ApiResponse<IssueResponse>> getIssue(@PathVariable Long id){
        return ResponseEntity.ok(ApiResponse.success("Issue",issueService.getIssuesById(id)));
    }

    @PutMapping("/manager/{id}/approve")
    @PreAuthorize("hasAnyRole('SOCIETY_MANAGER','SOCIETY_OWNER')")
    public ResponseEntity<ApiResponse<IssueResponse>> approveIssue(@PathVariable Long id,@AuthenticationPrincipal UserDetails userDetails){
        Long managerId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Approved",issueService.approveIssue(id,managerId)));
    }

    @PutMapping("/manager/{id}/reject")
    @PreAuthorize("hasAnyRole('SOCIETY_MANAGER','SOCIETY_OWNER')")
    public ResponseEntity<ApiResponse<IssueResponse>> rejectIssue(
            @PathVariable Long id,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails
    ){
        Long managerId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Rejected", issueService.rejectIssue(id, managerId, reason)));
    }

    @PutMapping("/manager/{id}/assign")
    @PreAuthorize("hasAnyRole('SOCIETY_MANAGER','SOCIETY_OWNER')")
    public ResponseEntity<ApiResponse<IssueResponse>> assignIssue(
            @PathVariable Long id,
            @Valid @RequestBody IssueAssignRequest request,
            @AuthenticationPrincipal UserDetails userDetails
            ){
        Long managerId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(ApiResponse.success("Assigned",issueService.assignIssue(id,request,managerId)));
    }

    @GetMapping("/manager/statistics")
    @PreAuthorize("hasAnyRole('SOCIETY_MANAGER','SOCIETY_OWNER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics(){
        return ResponseEntity.ok(ApiResponse.success("Statistics",issueService.getIssueStatistics()));
    }
    

    private Long getCurrentUserId(UserDetails userDetails){

        return ((CustomUserDetails) userDetails).getId();
    }
}
