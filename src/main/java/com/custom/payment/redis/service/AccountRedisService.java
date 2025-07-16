package com.custom.payment.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public BigDecimal getBalance(Long userId) {
        String val = redisTemplate.opsForValue().get("balance:user:" + userId);
        return val == null ? null : new BigDecimal(val);
    }

    public void setBalance(Long userId, BigDecimal amount) {
        redisTemplate.opsForValue().set("balance:user:" + userId, amount.toString());
    }

    public LocalDateTime getLastAccrual(Long userId) {
        String val = redisTemplate.opsForValue().get("last_accrual:user:" + userId);
        return val == null ? null : LocalDateTime.parse(val);
    }

    public void setLastAccrual(Long userId, LocalDateTime time) {
        redisTemplate.opsForValue().set("last_accrual:user:" + userId, time.toString());
    }

    public void setInitialDeposit(Long userId, BigDecimal value) {
        redisTemplate.opsForValue().set("initial_deposit:user:" + userId, value.toString());
    }

    public BigDecimal getInitialDeposit(Long userId) {
        String val = redisTemplate.opsForValue().get("initial_deposit:user:" + userId);
        return val == null ? null : new BigDecimal(val);
    }
}