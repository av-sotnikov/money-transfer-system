package com.custom.payment.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class EmailRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email must not be blank")
    private String email;
}