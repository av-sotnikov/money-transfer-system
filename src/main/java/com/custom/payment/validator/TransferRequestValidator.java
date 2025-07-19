package com.custom.payment.validator;

import com.custom.payment.dto.TransferRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransferRequestValidator {

    public void validate(Long fromUserId, TransferRequest request) {
        if (request.getToUserId() == null) {
            throw new IllegalArgumentException("Target user ID cannot be null");
        }
        if (request.getToUserId().equals(fromUserId)) {
            throw new IllegalArgumentException("Cannot transfer to the same user");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than 0");
        }
    }
}
