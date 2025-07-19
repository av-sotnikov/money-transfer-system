package com.custom.payment.service;

import com.custom.payment.audit.PaymentAuditService;
import com.custom.payment.db.enums.TransactionStatus;
import com.custom.payment.db.model.Transaction;
import com.custom.payment.db.repository.AccountRepository;
import com.custom.payment.db.repository.TransactionRepository;
import com.custom.payment.redis.service.AccountRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final BalanceService balanceService;
    private final AccountRedisService redisService;
    private final AccountRepository accountRepository;
    private final RedissonClient redissonClient;
    private final TransactionRepository transactionRepository;
    private final PaymentAuditService paymentAuditService;

    @Transactional
    public void transfer(Long fromUserId, Long toUserId, BigDecimal amount) throws InterruptedException {
        List<Long> ids = Stream.of(fromUserId, toUserId).sorted().collect(Collectors.toList());

        RLock lock1 = redissonClient.getLock("lock:user:" + ids.get(0));
        RLock lock2 = redissonClient.getLock("lock:user:" + ids.get(1));
        RedissonMultiLock multiLock = createMultiLock(lock1, lock2);

        boolean locked = false;

        try {
            locked = multiLock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!locked) {
                throw new RuntimeException("Transfer temporarily locked after timeout");
            }

            // бизнес-логика
            BigDecimal fromBalance = balanceService.getActualBalance(fromUserId, false);
            BigDecimal toBalance = balanceService.getActualBalance(toUserId, false);

            if (fromBalance.compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient funds");
            }

            BigDecimal newFrom = fromBalance.subtract(amount);
            BigDecimal newTo = toBalance.add(amount);

            redisService.setBalance(fromUserId, newFrom);
            redisService.setBalance(toUserId, newTo);

            LocalDateTime now = LocalDateTime.now();
            accountRepository.updateBalance(fromUserId, newFrom, now);
            accountRepository.updateBalance(toUserId, newTo, now);

            transactionRepository.save(Transaction.ofTransfer(
                    fromUserId, toUserId, amount, TransactionStatus.SUCCESS));

            paymentAuditService.logTransfer(fromUserId, toUserId, amount, now);

        } finally {
            if (locked) {
                try {
                    if (lock1.isHeldByCurrentThread()) {
                        lock1.unlock();
                    }
                } catch (Exception e) {
                    log.warn("Failed to unlock lock1", e);
                }
                try {
                    if (lock2.isHeldByCurrentThread()) {
                        lock2.unlock();
                    }
                } catch (Exception e) {
                    log.warn("Failed to unlock lock2", e);
                }
            }
        }
    }

    public RedissonMultiLock createMultiLock(RLock lock1, RLock lock2) {
        return new RedissonMultiLock(lock1, lock2);
    }
}
