package com.custom.payment.db.model;

import com.custom.payment.db.enums.TransactionStatus;
import com.custom.payment.db.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fromUserId;
    private Long toUserId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isActive = true;

    public static Transaction ofTransfer(Long from, Long to, BigDecimal amount, TransactionStatus status) {
        return new Transaction(null, from, to, amount, TransactionType.TRANSFER, status, LocalDateTime.now(), true);
    }

    public static Transaction ofAccrual(Long to, BigDecimal amount, TransactionStatus status) {
        return new Transaction(null, 9999L, to, amount, TransactionType.ACCRUAL, status, LocalDateTime.now(), true);
    }
}

