package com.society.repository;

import com.society.entity.IssueFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueFeedbackRepository extends JpaRepository<IssueFeedback,Long> {
    List<IssueFeedback> findByIssueId(Long issueId);
    Optional<IssueFeedback> findByIssueIdAndRentalId(Long issueId,Long rentalId);
}
