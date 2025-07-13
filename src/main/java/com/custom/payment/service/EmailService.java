package com.custom.payment.service;

import com.custom.payment.db.model.EmailData;
import com.custom.payment.db.model.User;
import com.custom.payment.db.repository.EmailRepository;
import com.custom.payment.db.repository.UserRepository;
import com.custom.payment.dto.EmailDto;
import com.custom.payment.mapper.EmailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public EmailDto addEmail(Long userId, String email) {
        // Проверка: активный такой email уже есть?
        if (emailRepository.existsByEmailAndIsActiveTrue(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Проверка: есть ли удалённый (is_active = false)?
        Optional<EmailData> inactive = emailRepository.findByUserIdAndEmailAndIsActiveFalse(userId, email);
        if (inactive.isPresent()) {
            EmailData restored = inactive.get();
            restored.setIsActive(true);
            return emailMapper.toDto(emailRepository.save(restored));
        }

        // Если вообще нет — создаём новую запись
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

    public EmailDto changeEmail(Long userId, Long emailId, String newEmail) throws AccessDeniedException {
        if (emailRepository.existsByEmailAndIsActiveTrue(newEmail)) {
            throw new IllegalArgumentException("Email is already in use");
        }

        EmailData currentEmail = emailRepository.findByIdAndIsActiveTrue(emailId)
                .orElseThrow(() -> new EntityNotFoundException("Active email not found"));

        if (!currentEmail.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied");
        }

        currentEmail.setIsActive(false);
        emailRepository.save(currentEmail);

        EmailData newEmailData = emailMapper.copyWithNewEmail(currentEmail, newEmail);
        return emailMapper.toDto(emailRepository.save(newEmailData));
    }


    public EmailDto changeEmailV2(Long userId, Long emailId, String newEmail) throws AccessDeniedException {
        EmailData currentEmail = emailRepository.findByIdAndIsActiveTrue(emailId)
                .orElseThrow(() -> new EntityNotFoundException("Active email not found"));

        if (!currentEmail.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Email does not belong to user");
        }

        if (currentEmail.getEmail().equalsIgnoreCase(newEmail)) {
            throw new IllegalArgumentException("New email is same as current");
        }

        // ✅ Деактивируем старую запись
        currentEmail.setIsActive(false);
        emailRepository.save(currentEmail);

        // ✅ Ищем старую запись с таким email
        Optional<EmailData> oldInactive = emailRepository.findByUserIdAndEmailAndIsActiveFalse(userId, newEmail);
        if (oldInactive.isPresent()) {
            EmailData toRestore = oldInactive.get();
            toRestore.setIsActive(true);
            return emailMapper.toDto(emailRepository.save(toRestore));

        }

        // ✅ Если такой не было — создаём новую
        if (emailRepository.existsByEmailAndIsActiveTrue(newEmail)) {
            throw new IllegalArgumentException("Email already taken");
        }

        EmailData newEmailData = emailMapper.copyWithNewEmail(currentEmail, newEmail);
        return emailMapper.toDto(emailRepository.save(newEmailData));
    }


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
