package com.society.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "security_guards")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SecurityGuard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "employee_id", unique = true, length = 30)
    private String employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", length = 20)
    private ShiftType shiftType;

    @Column(name = "gate_number")
    private Integer gateNumber;

    @Column(name = "emergency_contact", length = 15)
    private String emergencyContact;

    @Column(name = "joining_date")
    private java.time.LocalDate joiningDate;
}
