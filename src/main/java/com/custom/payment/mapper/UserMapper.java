package com.custom.payment.mapper;

import com.custom.payment.db.model.User;
import com.custom.payment.dto.UserDetailsDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    default UserDetailsDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDetailsDto.builder()
                .id(user.getId())
                .login(user.getLogin())
                .balance(user.getAccount() != null ? user.getAccount().getBalance() : null)
                .emails(user.getEmails().stream()
                        .map(e -> e.getEmail())
                        .collect(java.util.stream.Collectors.toList()))
                .phones(user.getPhones().stream()
                        .map(e -> e.getPhone())
                        .collect(java.util.stream.Collectors.toList()))
                .build();
    }
}
