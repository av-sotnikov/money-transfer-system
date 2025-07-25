package com.custom.payment.service;

import com.custom.payment.audit.AuditEvent;
import com.custom.payment.audit.Auditable;
import com.custom.payment.db.model.EmailData;
import com.custom.payment.db.model.User;
import com.custom.payment.db.repository.EmailRepository;
import com.custom.payment.db.repository.UserRepository;
import com.custom.payment.dto.EmailDto;
import com.custom.payment.mapper.EmailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailRepository emailRepository;
    private final UserRepository userRepository;
    private final EmailMapper emailMapper;

    @Transactional
    @Auditable(AuditEvent.USER_EMAIL_CREATED)
    public EmailDto addEmail(Long userId, String email) {
        if (emailRepository.existsByEmailAndIsActiveTrue(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        Optional<EmailData> inactive = emailRepository.findByUserIdAndEmailAndIsActiveFalse(userId, email);
        if (inactive.isPresent()) {
            EmailData restored = inactive.get();
            restored.setIsActive(true);
            return emailMapper.toDto(emailRepository.save(restored));
        }

        // User уже аутентифицирован, и его userId гарантированно существует (из JWT),
        // поэтому не загружаем сущность из базы.
        var user = new User();
        user.setId(userId);
        EmailData emailData = new EmailData();
        emailData.setEmail(email);
        emailData.setUser(user);
        return emailMapper.toDto(emailRepository.save(emailData));
    }


    public List<EmailDto> getEmails(Long userId) {
        List<EmailData> emails = emailRepository.findActiveByUserId(userId);
        return emailMapper.toDtoList(emails);
    }

    @Transactional
    @Auditable(AuditEvent.USER_EMAIL_CHANGED)
    public EmailDto changeEmail(Long userId, Long emailId, String newEmail) throws AccessDeniedException {
        EmailData currentEmail = emailRepository.findByIdAndIsActiveTrue(emailId)
                .orElseThrow(() -> new EntityNotFoundException("Active email not found"));

        if (!currentEmail.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Email does not belong to user");
        }

        if (currentEmail.getEmail().equalsIgnoreCase(newEmail)) {
            throw new IllegalArgumentException("New email is same as current");
        }

        currentEmail.setIsActive(false);
        emailRepository.save(currentEmail);

        Optional<EmailData> oldInactive = emailRepository.findByUserIdAndEmailAndIsActiveFalse(userId, newEmail);
        if (oldInactive.isPresent()) {
            EmailData toRestore = oldInactive.get();
            toRestore.setIsActive(true);
            return emailMapper.toDto(emailRepository.save(toRestore));

        }

        if (emailRepository.existsByEmailAndIsActiveTrue(newEmail)) {
            throw new IllegalArgumentException("Email already taken");
        }

        EmailData newEmailData = emailMapper.copyWithNewEmail(currentEmail, newEmail);
        return emailMapper.toDto(emailRepository.save(newEmailData));
    }


    @Transactional
    @Auditable(AuditEvent.USER_EMAIL_DEACTIVATED)
    public EmailDto deleteEmail(Long userId, Long emailId) throws AccessDeniedException {
        int activeEmailCount = emailRepository.countActiveByUserId(userId);
        if (activeEmailCount <= 1) {
            throw new IllegalStateException("User must have at least one active email.");
        }

        EmailData email = emailRepository.findByIdAndIsActiveTrue(emailId)
                .orElseThrow(() -> new EntityNotFoundException("Active email not found"));

        if (!email.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("This email does not belong to the current user");
        }

        email.setIsActive(false);
        return emailMapper.toDto(emailRepository.save(email));
    }
}
