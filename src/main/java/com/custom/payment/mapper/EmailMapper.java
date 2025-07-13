package com.custom.payment.mapper;

import com.custom.payment.db.model.EmailData;
import com.custom.payment.dto.EmailDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmailMapper {

    EmailMapper INSTANCE = Mappers.getMapper(EmailMapper.class);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "isActive", target = "isActive")
    EmailDto toDto(EmailData emailData);

    List<EmailDto> toDtoList(List<EmailData> emailDataList);

    @Mapping(target = "id", ignore = true) // чтобы не копировать ID
    @Mapping(target = "email", source = "newEmail")
    @Mapping(target = "isActive", constant = "true")
    EmailData copyWithNewEmail(EmailData source, String newEmail);
}
