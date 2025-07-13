package com.custom.payment.security.util;

public class AuthUtils {
    public static Long extractUserId(Object principal) {
        if (principal instanceof Long) {
            return (Long) principal;
        } else {
            throw new IllegalArgumentException("Invalid principal type: expected Long, but was " +
                    (principal != null ? principal.getClass().getSimpleName() : "null"));
        }
    }
}
