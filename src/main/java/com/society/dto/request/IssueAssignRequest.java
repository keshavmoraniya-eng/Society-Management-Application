package com.society.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueAssignRequest {

    @NotNull
    private Long fixerId;

    @NotNull
    @Positive
    private BigDecimal estimatedCost;

    private String remarks;
}
