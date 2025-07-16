package com.custom.payment.controller;

import com.custom.payment.db.projection.CommonUserProjection;
import com.custom.payment.dto.UserDetailsDto;
import com.custom.payment.dto.UserSummaryDto;
import com.custom.payment.service.UserService;
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
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserSummaryDto> someEndpoint(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserSummary(id));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDetailsDto> getProfile(@AuthenticationPrincipal Object principal) {
        Long userId = (Long) principal;
        return ResponseEntity.ok(userService.getUserDetails(userId));
    }

    @GetMapping
    public ResponseEntity<List<CommonUserProjection>> getUsers(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth) {

        List<CommonUserProjection> users = userService.getUsersFiltered(dateOfBirth);
        return ResponseEntity.ok(users);
    }
}
