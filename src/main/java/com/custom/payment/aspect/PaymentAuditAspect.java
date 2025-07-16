package com.custom.payment.aspect;


import com.custom.payment.audit.Auditable;
import com.custom.payment.audit.PaymentAuditService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@AllArgsConstructor
@Slf4j
public class PaymentAuditAspect {

    private final PaymentAuditService paymentAuditService;


    @Around("@annotation(auditable)")
    public Object logAudit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        return paymentAuditService.processAuditedCall(joinPoint, auditable);
    }
}
