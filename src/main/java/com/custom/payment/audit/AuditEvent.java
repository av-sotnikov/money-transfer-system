package com.custom.payment.audit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuditEvent {
    USER_EMAIL_CREATED("Пользователь создал адрес электронной почты"),
    USER_EMAIL_CHANGED("Пользователь изменил название email"),
    USER_EMAIL_DEACTIVATED("Пользователь удалил email"),
    LOGIN_ACTION("Вход пользователя в систему");
    private final String description;
}
