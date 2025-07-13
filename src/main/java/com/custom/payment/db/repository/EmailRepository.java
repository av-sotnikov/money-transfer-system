package com.custom.payment.db.repository;

import com.custom.payment.db.model.EmailData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<EmailData, Long> {

    boolean existsByEmailAndIsActiveTrue(String email);

    @Query("SELECT e FROM EmailData e WHERE e.user.id = :userId AND e.isActive = true")
    List<EmailData> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(e) FROM EmailData e WHERE e.user.id = :userId AND e.isActive = true")
    int countActiveByUserId(@Param("userId") Long userId);

    Optional<EmailData> findByIdAndIsActiveTrue(Long id);

    @Query("SELECT e FROM EmailData e WHERE e.user.id = :userId AND e.email = :email AND e.isActive = false")
    Optional<EmailData> findByUserIdAndEmailAndIsActiveFalse(@Param("userId") Long userId, @Param("email") String email);
}
