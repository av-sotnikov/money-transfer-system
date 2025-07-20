package com.custom.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class EmailRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email must not be blank")
    @Schema(description = "Новый email пользователя", example = "test@example.com")
    private String email;
}