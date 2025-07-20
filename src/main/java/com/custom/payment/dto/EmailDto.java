package com.custom.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailDto {
    private Long id;
    private Long userId;
    private String email;
    private Boolean isActive;
}
