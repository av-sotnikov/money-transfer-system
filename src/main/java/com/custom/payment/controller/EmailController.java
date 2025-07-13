package com.custom.payment.controller;

import com.custom.payment.dto.EmailDto;
import com.custom.payment.dto.EmailRequest;
import com.custom.payment.security.util.AuthUtils;
import com.custom.payment.service.EmailService;
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
public class EmailController {

    private final EmailService emailService;

    @PostMapping
    public ResponseEntity<EmailDto> addEmail(@Valid @RequestBody EmailRequest request,
                                             @AuthenticationPrincipal Object principal) {
        Long userId = AuthUtils.extractUserId(principal);
        var result = emailService.addEmail(userId, request.getEmail());
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<EmailDto>> getEmails(@AuthenticationPrincipal Object principal) {
        Long userId = AuthUtils.extractUserId(principal);
        var resp = emailService.getEmails(userId);
        return ResponseEntity.ok(resp);

    }

    @PostMapping("/{emailId}/replace")
    public ResponseEntity<Void> changeEmail(@PathVariable Long emailId,
                                            @RequestParam @Email String newEmail,
                                            @AuthenticationPrincipal Object principal) throws AccessDeniedException {
        Long userId = AuthUtils.extractUserId(principal);

        emailService.changeEmailV2(userId, emailId, newEmail);
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{emailId}/deactivate")
    public ResponseEntity<EmailDto> deleteEmail(@PathVariable Long emailId,
                                                @AuthenticationPrincipal Object principal) throws AccessDeniedException {
        Long userId = AuthUtils.extractUserId(principal);
        var result = emailService.deleteEmail(userId, emailId);
        return ResponseEntity.ok(result);
    }
}
