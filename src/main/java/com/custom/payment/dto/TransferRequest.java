package com.custom.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {

    @NotNull
    @Schema(description = "User_id клиента получателя", example = "2")
    private Long toUserId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Schema(description = "Сумма перевода", example = "2000.00")
    private BigDecimal amount;
}