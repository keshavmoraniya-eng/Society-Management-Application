package com.society.repository;

import com.society.entity.OtpRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpRecord,Long> {
    @Query("SELECT o FROM OtpRecord o WHERE o.phoneNo = ?1 AND o.isUsed = false ORDER BY o.createdAt DESC")
    Optional<OtpRecord> findLatestActiveOtp(String phoneNo);

}
