package com.society.service;

import com.society.dto.request.IssueAssignRequest;
import com.society.dto.request.IssueRequest;
import com.society.dto.response.IssueResponse;
import com.society.entity.*;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.IssueRepository;
import com.society.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final UserRepository userRepository;

    @Transactional
    public IssueResponse createIssue(IssueRequest request, Long rentalId){
        User rental = userRepository.findById(rentalId)
                .orElseThrow(()->new ResourceNotFoundException("Rental not found"));

        if (rental.getRole() != Role.RENTAL){
            throw new BadRequestException("Only rentals can create issue");
        }

        IssueType type;
        Priority priority;
        try {
            type = IssueType.valueOf(request.getIssueType().toUpperCase());
            priority = request.getPriority() != null ? Priority.valueOf(request.getPriority().toUpperCase()) : Priority.MEDIUM;
        }catch (IllegalArgumentException exception){
            throw new BadRequestException("Invalid issue type or priority");
        }

        Issue issue = Issue.builder()
                .rental(rental)
                .title(request.getTitle())
                .description(request.getDescription())
                .issueType(type)
                .priority(priority)
                .apartmentNo(request.getApartmentNo())
                .imageUrls(request.getImageUrls())
                .status(IssueStatus.PENDING)
                .build();

        issue = issueRepository.save(issue);
        log.info("Issue created: {} by rental: {}", issue.getId(), rentalId);
        return mapToResponse(issue);
    }

    public List<IssueResponse> getIssuesByRental(Long rentalId){
        return issueRepository.findByRentalIdOrderByCreatedAtDesc(rentalId)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<IssueResponse> getAllIssues(){
        return issueRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<IssueResponse> getIssuesByStatus(IssueStatus status){
        return issueRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public IssueResponse getIssuesById(Long id){
        Issue issue = issueRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Issue not found"));
        return mapToResponse(issue);
    }

    @Transactional
    public IssueResponse approveIssue(Long issueId, Long managerId){
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(()-> new ResourceNotFoundException("Issue not found"));

        validateManager(managerId);

        if (issue.getStatus() != IssueStatus.PENDING){
            throw new BadRequestException("Only pending issues can be approved");
        }
        issue.setStatus(IssueStatus.APPROVED);
        issue = issueRepository.save(issue);
        log.info("Issue approved: {} by manager: {}", issueId, managerId);
        return mapToResponse(issue);
    }

    @Transactional
    public IssueResponse rejectIssue(Long issueId, Long managerId, String reason){
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(()-> new ResourceNotFoundException("Issue not found"));

        validateManager(managerId);
        issue.setStatus(IssueStatus.REJECTED);
        issue.setCompletionRemarks("Rejected by manager" + reason);
        issue = issueRepository.save(issue);
        log.info("Issue rejected: {} by manager: {}", issueId, managerId);
        return mapToResponse(issue);
    }

    @Transactional
    public IssueResponse assignIssue(Long issueId, IssueAssignRequest request, Long managerId){
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(()-> new ResourceNotFoundException("Issue not found"));

        validateManager(managerId);

        User fixer = userRepository.findById(request.getFixerId())
                .orElseThrow(()-> new ResourceNotFoundException("Fixer not found"));

        issue.setAssignedTo(fixer);
        issue.setEstimatedCost(request.getEstimatedCost());
        issue.setStatus(IssueStatus.ASSIGNED);
        issue.setCompletionRemarks(request.getRemarks());
        issue = issueRepository.save(issue);
        log.info("Issue assigned: {} to fixer: {} by manager: {}", issueId, request.getFixerId(), managerId);
        return mapToResponse(issue);
    }

    @Transactional
    public IssueResponse markInProgress(Long issueId, Long fixerId){
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(()-> new ResourceNotFoundException("Issue not found"));

        if (issue.getAssignedTo() == null || !issue.getAssignedTo().getId().equals(fixerId)){
            throw new BadRequestException("Only assigned fixer can mark issue in progress");
        }

        issue.setStatus(IssueStatus.IN_PROGRESS);
        return mapToResponse(issueRepository.save(issue));
    }

    public IssueResponse markCompleted(Long issueId, Long fixerId, BigDecimal actualCost, String remarks){
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(()-> new ResourceNotFoundException("Issue not found"));

        if (issue.getAssignedTo() == null || !issue.getAssignedTo().getId().equals(fixerId)){
            throw new BadRequestException("Only assigned fixer can mark issue completed");
        }

        issue.setStatus(IssueStatus.COMPLETED);
        issue.setActualCost(actualCost);
        issue.setCompletionRemarks(remarks);
        issue.setResolveAt(LocalDateTime.now());

        return mapToResponse(issueRepository.save(issue));
        }

        public Map<String, Object> getIssueStatistics(){
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", issueRepository.count());
        stats.put("pending", issueRepository.countByStatus(IssueStatus.PENDING));
        stats.put("approved", issueRepository.countByStatus(IssueStatus.APPROVED));
        stats.put("assigned", issueRepository.countByStatus(IssueStatus.ASSIGNED));
        stats.put("inProgress", issueRepository.countByStatus(IssueStatus.IN_PROGRESS));
        stats.put("completed", issueRepository.countByStatus(IssueStatus.COMPLETED));
        stats.put("rejected", issueRepository.countByStatus(IssueStatus.REJECTED));

        Map<String, Long> typeStats = new HashMap<>();
        issueRepository.countByIssueType().forEach(arr ->
                typeStats.put(((IssueType) arr[0]).name(), (Long) arr[1]));
        stats.put("byType", typeStats);
        return stats;
    }

    private void validateManager(Long managerId){
        User manager = userRepository.findById(managerId)
                .orElseThrow(()-> new ResourceNotFoundException("Manager not found"));
        if (manager.getRole() != Role.SOCIETY_MANAGER && manager.getRole() != Role.SOCIETY_OWNER){
            throw new BadRequestException("Only society manager or owner can approved issues");
        }
    }


    private IssueResponse mapToResponse(Issue issue){
        IssueResponse.IssueResponseBuilder builder = IssueResponse.builder()
                .id(issue.getId())
                .title(issue.getTitle())
                .description(issue.getDescription())
                .issueType(issue.getIssueType().name())
                .status(issue.getStatus().name())
                .priority(issue.getPriority().name())
                .apartmentNo(issue.getApartmentNo())
                .imageUrls(issue.getImageUrls())
                .estimatedCost(issue.getEstimatedCost())
                .actualCost(issue.getActualCost())
                .createdAt(issue.getCreatedAt())
                .resolvedAt(issue.getResolveAt());

        if (issue.getRental() != null){
            builder.rentalId(issue.getRental().getId())
                    .rentalName(issue.getRental().getFullName())
                    .rentalPhone(issue.getRental().getPhoneNo());

            if (issue.getRental().getRentalProfile() != null){
                builder.rentalBloodGroup(issue.getRental().getRentalProfile().getBloodGroup())
                        .rentalJobProfile(issue.getRental().getRentalProfile().getJobProfile());;
            }
        }

        if (issue.getAssignedTo() != null){
            builder.assignedToId(issue.getAssignedTo().getId())
                    .assignedToName(issue.getAssignedTo().getFullName())
                    .assignedToPhone(issue.getAssignedTo().getPhoneNo());
        }

        return builder.build();
    }

}
