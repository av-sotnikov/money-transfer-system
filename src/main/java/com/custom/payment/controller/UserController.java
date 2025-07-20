package com.custom.payment.controller;

import com.custom.payment.db.projection.CommonUserProjection;
import com.custom.payment.dto.UserDetailsDto;
import com.custom.payment.dto.UserSummaryDto;
import com.custom.payment.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(summary = " Возвращает name по user_id", description = "Получить имя по user_id")
    @GetMapping("/{id}")
    public ResponseEntity<UserSummaryDto> getSummary(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserSummary(id));
    }

    @Operation(summary = "Возвращает информацию о профиле", description = "Синхронизация профиля")
    @GetMapping("/profile")
    public ResponseEntity<UserDetailsDto> getProfile(@AuthenticationPrincipal Object principal) {
        Long userId = (Long) principal;
        return ResponseEntity.ok(userService.getUserDetails(userId));
    }

    @Operation(summary = "Получить список пользователе < dateOfBirth", description = "Получить список пользователе < dateOfBirth")
    @GetMapping
    public ResponseEntity<List<CommonUserProjection>> getUsers(
            @Parameter(description = "Дата рождения. Возвращаются пользователи, родившиеся после этой даты",
                    example = "1990-01-01")
            @RequestParam(required = false, defaultValue = "1990-01-01") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth) {

        List<CommonUserProjection> users = userService.getUsersFiltered(dateOfBirth);
        return ResponseEntity.ok(users);
    }
}
