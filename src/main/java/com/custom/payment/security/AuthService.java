package com.custom.payment.security;

import com.custom.payment.audit.AuditEvent;
import com.custom.payment.audit.Auditable;
import com.custom.payment.db.repository.AccountRepository;
import com.custom.payment.db.repository.UserRepository;
import com.custom.payment.redis.service.AccountRedisService;
import com.custom.payment.security.jwt.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AccountRepository accountRepository;
    private final AccountRedisService accountRedisService;

    @Auditable(AuditEvent.LOGIN_ACTION)
    public String login(String username, String password) {
        var user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }

        preloadBalanceToRedis(user.getId());

        return jwtTokenService.getGenerateAccessToken(user);
    }

    private void preloadBalanceToRedis(Long userId) {
        var acc = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        accountRedisService.setBalance(userId, acc.getBalance());
        accountRedisService.setLastAccrual(userId, acc.getLastAccrualTime());
        accountRedisService.setInitialDeposit(userId, acc.getInitialDeposit());
    }
}
