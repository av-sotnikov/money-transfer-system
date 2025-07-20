package com.custom.payment.audit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
public class PaymentAuditService {

    public Object processAuditedCall(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String description = auditable.value().getDescription();
        Object[] args = joinPoint.getArgs();
        String username = args.length > 0 ? args[0].toString() : "unknown";

        try {
            Object result = joinPoint.proceed();

            log.info("✅ AUDIT SUCCESS: Action={}, Username={}, Event: {}", description, username, result);
            return result;
        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            log.warn("❌ AUDIT FAILURE: Action={}, Username={}, Reason={}", description, username, ex.getMessage());
            throw ex;

        } catch (Throwable ex) {
            log.error("❗ AUDIT ERROR: Action={}, Username={}, Unexpected={}", description, username, ex.getMessage(), ex);
            throw ex;
        }
    }

    public void logTransfer(Long fromUserId, Long toUserId, BigDecimal amount, LocalDateTime timestamp) {
        log.info("✅ TRANSFER: | fromUserId={}, toUserId={}, amount={}, time={}",
                fromUserId, toUserId, amount, timestamp);
    }
}
