package com.custom.payment;

import com.custom.payment.db.model.EmailData;
import com.custom.payment.db.repository.EmailRepository;
import com.custom.payment.db.repository.UserRepository;
import com.custom.payment.dto.EmailDto;
import com.custom.payment.mapper.EmailMapper;
import com.custom.payment.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailMapper emailMapper;

    @Test
    void shouldAddNewEmailIfNotExists() {
        Long userId = 1L;
        String email = "test@example.com";
        EmailData savedData = new EmailData();
        EmailDto expectedDto = new EmailDto();

        when(emailRepository.existsByEmailAndIsActiveTrue(email)).thenReturn(false);
        when(emailRepository.findByUserIdAndEmailAndIsActiveFalse(userId, email)).thenReturn(Optional.empty());
        when(emailRepository.save(any(EmailData.class))).thenReturn(savedData);
        when(emailMapper.toDto(savedData)).thenReturn(expectedDto);

        EmailDto result = emailService.addEmail(userId, email);

        assertEquals(expectedDto, result);
        verify(emailRepository).save(any(EmailData.class));
    }

    @Test
    void shouldThrowIfEmailAlreadyExists() {
        Long userId = 1L;
        String email = "test@example.com";

        when(emailRepository.existsByEmailAndIsActiveTrue(email)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> emailService.addEmail(userId, email));
    }

    @Test
    void shouldReturnListOfActiveEmails() {
        Long userId = 1L;
        List<EmailData> dataList = List.of(new EmailData());
        List<EmailDto> dtoList = List.of(new EmailDto());

        when(emailRepository.findActiveByUserId(userId)).thenReturn(dataList);
        when(emailMapper.toDtoList(dataList)).thenReturn(dtoList);

        List<EmailDto> result = emailService.getEmails(userId);

        assertEquals(dtoList, result);
    }
}
