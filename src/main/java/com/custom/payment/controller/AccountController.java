package com.custom.payment.controller;

import com.custom.payment.dto.TransferRequest;
import com.custom.payment.security.util.AuthUtils;
import com.custom.payment.service.BalanceService;
import com.custom.payment.service.TransferService;
import com.custom.payment.validator.TransferRequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class AccountController {

    private final BalanceService balanceService;
    private final TransferService transferService;
    private final TransferRequestValidator transferRequestValidator;

    /**
     * Получить текущий баланс пользователя (из Redis или БД)
     */
    @Operation(summary = "Запрос актуального баланса счета", description = "Возвращает сумму")
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
    @Operation(summary = "Выполнить перевод другому клиенту", description = "Осуществляет проводку")
    public ResponseEntity<String> transfer(
            @Parameter(hidden = true) @AuthenticationPrincipal Object principal,
            @Valid @RequestBody TransferRequest request
    ) throws InterruptedException {
        Long fromUserId = AuthUtils.extractUserId(principal);
        transferRequestValidator.validate(fromUserId, request);
        transferService.transfer(fromUserId, request.getToUserId(), request.getAmount());
        return ResponseEntity.ok("Transfer successful");
    }
}