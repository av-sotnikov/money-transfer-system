package com.custom.payment;

import com.custom.payment.audit.PaymentAuditService;
import com.custom.payment.db.model.Transaction;
import com.custom.payment.db.repository.AccountRepository;
import com.custom.payment.db.repository.TransactionRepository;
import com.custom.payment.redis.service.AccountRedisService;
import com.custom.payment.service.BalanceService;
import com.custom.payment.service.TransferService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @InjectMocks
    private TransferService transferService;

    @Mock
    private BalanceService balanceService;
    @Mock
    private AccountRedisService redisService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private PaymentAuditService paymentAuditService;

    @Mock
    private RLock lock1;
    @Mock
    private RLock lock2;

    @Test
    void testSuccessfulTransfer() throws InterruptedException {
        Long fromUserId = 1L;
        Long toUserId = 2L;
        BigDecimal amount = BigDecimal.valueOf(100);
        BigDecimal fromBalance = BigDecimal.valueOf(200);
        BigDecimal toBalance = BigDecimal.valueOf(50);

        RLock lock1 = mock(RLock.class);
        RLock lock2 = mock(RLock.class);
        RedissonClient redissonClient = mock(RedissonClient.class);
        when(redissonClient.getLock("lock:user:1")).thenReturn(lock1);
        when(redissonClient.getLock("lock:user:2")).thenReturn(lock2);

        RedissonMultiLock multiLock = mock(RedissonMultiLock.class);
        when(multiLock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);
        when(lock1.isHeldByCurrentThread()).thenReturn(true);
        when(lock2.isHeldByCurrentThread()).thenReturn(true);

        BalanceService balanceService = mock(BalanceService.class);
        AccountRedisService redisService = mock(AccountRedisService.class);
        AccountRepository accountRepository = mock(AccountRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        PaymentAuditService paymentAuditService = mock(PaymentAuditService.class);

        TransferService transferService = Mockito.spy(new TransferService(
                balanceService, redisService, accountRepository,
                redissonClient, transactionRepository, paymentAuditService
        ));

        doReturn(multiLock).when(transferService).createMultiLock(lock1, lock2);

        when(balanceService.getActualBalance(fromUserId, false)).thenReturn(fromBalance);
        when(balanceService.getActualBalance(toUserId, false)).thenReturn(toBalance);

        transferService.transfer(fromUserId, toUserId, amount);

        BigDecimal expectedFrom = fromBalance.subtract(amount);
        BigDecimal expectedTo = toBalance.add(amount);

        verify(redisService).setBalance(fromUserId, expectedFrom);
        verify(redisService).setBalance(toUserId, expectedTo);
        verify(accountRepository).updateBalance(eq(fromUserId), eq(expectedFrom), any());
        verify(accountRepository).updateBalance(eq(toUserId), eq(expectedTo), any());
        verify(transactionRepository).save(any(Transaction.class));
        verify(paymentAuditService).logTransfer(eq(fromUserId), eq(toUserId), eq(amount), any());
        verify(lock1).unlock();
        verify(lock2).unlock();
    }
    
    private TransferService spyTransferServiceWithMultiLock(RedissonMultiLock multiLock) {
        TransferService spy = Mockito.spy(new TransferService(
                balanceService, redisService, accountRepository,
                redissonClient, transactionRepository, paymentAuditService
        ));
        doReturn(multiLock).when(spy).createMultiLock(any(), any());
        return spy;
    }
}

