package com.society.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "issues")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Issue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id",nullable = false)
    private User rental;

    @Column(nullable = false,length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type",nullable = false,length = 20)
    private IssueType issueType;

    @Column(name = "apartment_no",length = 20)
    private String apartmentNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private IssueStatus status=IssueStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 10)
    private Priority priority=Priority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    @Column(name = "estimated_cost")
    private BigDecimal estimatedCost;

    @Column(name = "actual_cost")
    private BigDecimal actualCost;

    @Column(name = "completion_remarks",columnDefinition = "TEXT")
    private String completionRemarks;

    @Column(name = "image_urls",columnDefinition = "TEXT")
    private String imageUrls;

    @CreatedDate
    @Column(name = "created_at",updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolveAt;

    @OneToMany(mappedBy = "issue",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<IssueFeedback> feedbacks=new ArrayList<>();

}
