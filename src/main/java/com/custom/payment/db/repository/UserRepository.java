package com.custom.payment.db.repository;


import com.custom.payment.db.model.User;
import com.custom.payment.db.projection.UserAuthProjection;
import com.custom.payment.dto.UserSummaryDto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<UserAuthProjection> findByLogin(String login);

    @Query("SELECT new com.custom.payment.dto.UserSummaryDto(u.id, u.name) FROM User u WHERE u.id = :id")
    Optional<UserSummaryDto> findFullSummary(@Param("id") Long id);

    @EntityGraph(attributePaths = {"account", "emails", "phones"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findWithAllRelationsById(@Param("id") Long id);
}
