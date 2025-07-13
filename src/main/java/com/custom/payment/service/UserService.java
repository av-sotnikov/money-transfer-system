package com.custom.payment.service;

import com.custom.payment.db.model.User;
import com.custom.payment.db.repository.UserRepository;
import com.custom.payment.dto.UserDetailsDto;
import com.custom.payment.dto.UserSummaryDto;
import com.custom.payment.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * Метод для отображения full_name по user_id для информации при создании платежа / банковской транзакции
     *
     * @param id -user_id
     * @return - UserSummaryDto
     */
    public UserSummaryDto getUserSummary(Long id) {
        UserSummaryDto userSummaryDto = userRepository.findFullSummary(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return userSummaryDto;
    }

    /**
     * Загружает персональные данные профиля клиента и др. при синхронизации / входе в мобильное приложение
     *
     * @param id - user_id
     * @return - UserDetailsDto
     */
    public UserDetailsDto getUserDetails(Long id) {
        User user = userRepository.findWithAllRelationsById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return userMapper.toDto(user);
    }
}
