package com.society.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "rental_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RentalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "apartment_no", nullable = false, length = 20)
    private String apartmentNo;

    @Column(name = "total_members", nullable = false)
    private Integer totalMembers;

    @Column(name = "job_profile", length = 100)
    private String jobProfile;

    @Column(name = "working_location", length = 200)
    private String workingLocation;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Column(name = "move_in_date")
    private LocalDate moveInDate;
}
