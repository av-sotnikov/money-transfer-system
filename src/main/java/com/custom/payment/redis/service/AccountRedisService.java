package com.custom.payment.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountRedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration TTL = Duration.ofMinutes(10);

    public BigDecimal getBalance(Long userId) {
        String key = "balance:user:" + userId;
        String val = redisTemplate.opsForValue().get(key);
        if (val != null) {
            redisTemplate.expire(key, TTL);
            return new BigDecimal(val);
        }
        return null;
    }

    public void setBalance(Long userId, BigDecimal amount) {
        redisTemplate.opsForValue().set("balance:user:" + userId, amount.toString(), TTL);
    }

    public LocalDateTime getLastAccrual(Long userId) {
        String key = "last_accrual:user:" + userId;
        String val = redisTemplate.opsForValue().get(key);
        if (val != null) {
            redisTemplate.expire(key, TTL);
            return LocalDateTime.parse(val);
        }
        return null;
    }


    public void setLastAccrual(Long userId, LocalDateTime time) {
        redisTemplate.opsForValue().set("last_accrual:user:" + userId, time.toString(), TTL);
    }

    public void setInitialDeposit(Long userId, BigDecimal value) {
        redisTemplate.opsForValue().set("initial_deposit:user:" + userId, value.toString(), TTL);
    }

    public BigDecimal getInitialDeposit(Long userId) {
        String key = "initial_deposit:user:" + userId;
        String val = redisTemplate.opsForValue().get(key);
        if (val != null) {
            redisTemplate.expire(key, TTL);
            return new BigDecimal(val);
        }
        return null;
    }

}