package com.custom.payment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginRequest {

    @Schema(description = "Имя пользователя", example = "RRoss")
    private String username;

    @Schema(description = "Пароль", example = "p4q!9lGgL4t$")
    private String password;
}
