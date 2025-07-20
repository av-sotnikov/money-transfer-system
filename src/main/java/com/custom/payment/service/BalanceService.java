package com.custom.payment.service;

import com.custom.payment.db.enums.TransactionStatus;
import com.custom.payment.db.model.Account;
import com.custom.payment.db.model.Transaction;
import com.custom.payment.db.repository.AccountRepository;
import com.custom.payment.db.repository.TransactionRepository;
import com.custom.payment.redis.service.AccountRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class BalanceService {

    private final AccountRedisService redisService;
    private final AccountRepository accountRepository;
    private final RedissonClient redissonClient;
    private final TransactionRepository transactionRepository;

    @Transactional
    public BigDecimal getActualBalance(Long userId) {
        return getActualBalance(userId, true);
    }

    @Transactional
    public BigDecimal getActualBalance(Long userId, boolean withLock) {
        RLock lock = withLock ? redissonClient.getLock("lock:user:" + userId) : null;
        boolean locked = false;

        try {
            if (withLock && lock != null) {
                locked = lock.tryLock(5, TimeUnit.SECONDS);
                if (!locked) {
                    throw new IllegalStateException("Could not acquire lock for user " + userId);
                }
            }

            BigDecimal balance = redisService.getBalance(userId);
            LocalDateTime last = redisService.getLastAccrual(userId);
            BigDecimal initial = redisService.getInitialDeposit(userId);

            if (balance == null || last == null || initial == null) {
                Account acc = accountRepository.findByUserId(userId).orElseThrow();
                balance = acc.getBalance();
                last = acc.getLastAccrualTime();
                initial = acc.getInitialDeposit();
                redisService.setBalance(userId, balance);
                redisService.setLastAccrual(userId, last);
                redisService.setInitialDeposit(userId, initial);
            }

            BigDecimal max = initial.multiply(BigDecimal.valueOf(2.07));
            long elapsed = Duration.between(last, LocalDateTime.now()).getSeconds() / 30;
            if (elapsed <= 0 || balance.compareTo(max) >= 0) {
                return balance.setScale(2, RoundingMode.HALF_UP);
            }

            LocalDateTime updatedAccrual = last.plusSeconds(elapsed * 30);
            BigDecimal updated = balance;

            for (int i = 0; i < elapsed && updated.compareTo(max) < 0; i++) {
                updated = updated.multiply(BigDecimal.valueOf(1.1))
                        .min(max)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            if (updated.compareTo(balance) > 0) {
                accountRepository.updateBalance(userId, updated, updatedAccrual);
                transactionRepository.save(Transaction.ofAccrual(
                        userId,
                        updated.subtract(balance),
                        TransactionStatus.SUCCESS
                ));

                BigDecimal finalUpdated = updated;
                LocalDateTime finalUpdatedAccrual = updatedAccrual;
                BigDecimal finalAccrualAmount = updated.subtract(balance);

                if (TransactionSynchronizationManager.isSynchronizationActive()) {
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            redisService.setBalance(userId, finalUpdated);
                            redisService.setLastAccrual(userId, finalUpdatedAccrual);
                            log.info("✅ ACCRUAL: | userId={}, amount={}, time={}", userId, finalAccrualAmount, LocalDateTime.now());
                        }
                    });
                } else {
                    log.warn("⚠️ Transaction synchronization is not active — skipping Redis sync");
                }
            }
            return updated.setScale(2, RoundingMode.HALF_UP);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Lock interrupted for user " + userId, e);
        } finally {
            if (withLock && lock != null && locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}