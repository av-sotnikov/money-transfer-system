package com.custom.payment.controller;

import com.custom.payment.dto.TransferRequest;
import com.custom.payment.security.util.AuthUtils;
import com.custom.payment.service.BalanceService;
import com.custom.payment.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final BalanceService balanceService;
    private final TransferService transferService;

    /**
     * Получить текущий баланс пользователя (из Redis или БД)
     */
    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(@AuthenticationPrincipal Object principal) {
        Long userId = AuthUtils.extractUserId(principal);
        BigDecimal balance = balanceService.getActualBalance(userId);
        return ResponseEntity.ok(balance);
    }

    /**
     * Выполнить перевод средств между пользователями
     */
    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @AuthenticationPrincipal Object principal,
            @RequestBody TransferRequest request
    ) {
        Long fromUserId = AuthUtils.extractUserId(principal);
        transferService.transfer(fromUserId, request.getToUserId(), request.getAmount());
        return ResponseEntity.ok("Transfer successful");
    }
}