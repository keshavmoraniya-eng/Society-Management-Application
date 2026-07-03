package com.society.repository;

import com.society.entity.RentalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RentalProfileRepository extends JpaRepository<RentalProfile,Long> {
    Optional<RentalProfile> findByUserId(Long userId);
    Optional<RentalProfile> findByApartmentNo(String apartmentNo);
}
