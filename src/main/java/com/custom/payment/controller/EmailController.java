package com.custom.payment.controller;

import com.custom.payment.dto.EmailDto;
import com.custom.payment.dto.EmailRequest;
import com.custom.payment.security.util.AuthUtils;
import com.custom.payment.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class EmailController {

    private final EmailService emailService;

    @Operation(summary = "Добавить новый email", description = "Возвращает данные добавленного email")
    @PostMapping
    public ResponseEntity<EmailDto> addEmail(@Valid @RequestBody EmailRequest request,
                                             @Parameter(hidden = true) @AuthenticationPrincipal Object principal) {
        Long userId = AuthUtils.extractUserId(principal);
        var result = emailService.addEmail(userId, request.getEmail());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Получить список emals", description = "Возвращает все почтовые адреса клиента")
    @GetMapping
    public ResponseEntity<List<EmailDto>> getEmails(@AuthenticationPrincipal Object principal) {
        Long userId = AuthUtils.extractUserId(principal);
        var resp = emailService.getEmails(userId);
        return ResponseEntity.ok(resp);

    }

    @Operation(summary = "Изменить email", description = "Возвращает новый измененный email")
    @PostMapping("/{emailId}/replace")
    public ResponseEntity<EmailDto> changeEmail(
            @Parameter(description = "ID email-а, который заменяется", example = "1")
            @PathVariable Long emailId,
            @Parameter(description = "Новый email", example = "new@example.com")
            @RequestParam @Email String newEmail,
            @AuthenticationPrincipal Object principal) throws AccessDeniedException {
        Long userId = AuthUtils.extractUserId(principal);
        var result = emailService.changeEmail(userId, emailId, newEmail);
        return ResponseEntity.ok(result);
    }


    @Operation(summary = "Деактивирует email", description = "Подтверждает операцию деактивации с названием email")
    @PatchMapping("/{emailId}/deactivate")
    public ResponseEntity<EmailDto> deleteEmail(
            @Parameter(description = "ID email-а, деактивируется", example = "1")
            @PathVariable Long emailId,
            @AuthenticationPrincipal Object principal) throws AccessDeniedException {
        Long userId = AuthUtils.extractUserId(principal);
        var result = emailService.deleteEmail(userId, emailId);
        return ResponseEntity.ok(result);
    }
}
