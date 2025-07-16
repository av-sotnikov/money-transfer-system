package com.custom.payment.service;

import com.custom.payment.db.enums.TransactionStatus;
import com.custom.payment.db.model.Transaction;
import com.custom.payment.db.repository.AccountRepository;
import com.custom.payment.db.repository.TransactionRepository;
import com.custom.payment.redis.service.AccountRedisService;
import lombok.RequiredArgsConstructor;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final BalanceService balanceService;
    private final AccountRedisService redisService;
    private final AccountRepository accountRepository;
    private final RedissonClient redissonClient;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void transfer(Long fromUserId, Long toUserId, BigDecimal amount) {
        List<Long> ids = List.of(fromUserId, toUserId).stream().sorted().collect(Collectors.toList());
        RLock lock1 = redissonClient.getLock("lock:user:" + ids.get(0));
        RLock lock2 = redissonClient.getLock("lock:user:" + ids.get(1));
        RedissonMultiLock multiLock = new RedissonMultiLock(lock1, lock2);

        if (!multiLock.tryLock()) {
            throw new RuntimeException("Transfer temporarily locked");
        }

        try {
            BigDecimal fromBalance = balanceService.getActualBalance(fromUserId, false);
            BigDecimal toBalance = balanceService.getActualBalance(toUserId, false);

            if (fromBalance.compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient funds");
            }

            BigDecimal newFrom = fromBalance.subtract(amount);
            BigDecimal newTo = toBalance.add(amount);

            redisService.setBalance(fromUserId, newFrom);
            redisService.setBalance(toUserId, newTo);

            accountRepository.updateBalance(fromUserId, newFrom, LocalDateTime.now());
            accountRepository.updateBalance(toUserId, newTo, LocalDateTime.now());

            transactionRepository.save(Transaction.ofTransfer(
                    fromUserId,
                    toUserId,
                    amount,
                    TransactionStatus.SUCCESS
            ));

        } finally {
            multiLock.unlock();
        }
    }
}
