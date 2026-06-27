package com.society.repository;

import com.society.entity.Issue;
import com.society.entity.IssueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Objects;

@Repository
public interface IssueRepository extends JpaRepository<Issue,Long> {
    List<Issue> findByRentalIdOrderByCreatedAtDesc(Long rentalId);
    List<Issue> findByStatusOrderByCreatedAtDesc(IssueStatus status);
    List<Issue> findByAssignedToIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT COUNT(i) FROM Issue i WHERE i.status = :status")
    Long countByStatus(@Param("status") IssueStatus status);

    @Query("SELECT i.issueType, COUNT(i) FROM Issue i GROUP BY i.issueType")
    List<Object[]> countByIssueType();
}
