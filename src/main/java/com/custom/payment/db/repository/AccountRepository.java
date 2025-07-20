package com.custom.payment.db.repository;

import com.custom.payment.db.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    @Query("SELECT a.initialDeposit FROM Account a WHERE a.user.id = :userId")
    BigDecimal getInitialDeposit(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Account a SET a.balance = :balance, a.lastAccrualTime = :time WHERE a.user.id = :userId")
    void updateBalance(@Param("userId") Long userId, @Param("balance") BigDecimal balance, @Param("time") LocalDateTime time);

    @Query("SELECT a FROM Account a WHERE a.user.id = :userId")
    Optional<Account> findByUserId(@Param("userId") Long userId);

}