package com.society.repository;

import com.society.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting,Long> {
    List<Meeting> findAllByOrderByMeetingDateDesc();

    @Query("SELECT m FROM Meeting m WHERE m.meetingDate >= :today ORDER BY m.meetingDate ASC")
    List<Meeting> findUpComingMeetings(LocalDate today);
}
