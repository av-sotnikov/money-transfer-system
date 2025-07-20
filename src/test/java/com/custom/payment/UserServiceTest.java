package com.custom.payment;

import com.custom.payment.db.model.User;
import com.custom.payment.db.projection.CommonUserProjection;
import com.custom.payment.db.repository.UserRepository;
import com.custom.payment.dto.UserDetailsDto;
import com.custom.payment.dto.UserSummaryDto;
import com.custom.payment.mapper.UserMapper;
import com.custom.payment.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserSummary_shouldReturnSummaryDto_whenUserExists() {
        Long userId = 1L;
        UserSummaryDto expectedDto = new UserSummaryDto();
        expectedDto.setName("John Doe");


        Mockito.when(userRepository.findFullSummary(userId))
                .thenReturn(Optional.of(expectedDto));

        UserSummaryDto result = userService.getUserSummary(userId);

        assertEquals(expectedDto, result);
    }

    @Test
    void getUserSummary_shouldThrow404_whenUserNotFound() {
        Long userId = 1L;

        Mockito.when(userRepository.findFullSummary(userId))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                userService.getUserSummary(userId));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void getUserDetails_shouldReturnDto_whenUserExists() {
        Long userId = 1L;
        User user = new User();
        UserDetailsDto dto = new UserDetailsDto();

        Mockito.when(userRepository.findWithAllRelationsById(userId))
                .thenReturn(Optional.of(user));
        Mockito.when(userMapper.toDto(user)).thenReturn(dto);

        UserDetailsDto result = userService.getUserDetails(userId);

        assertEquals(dto, result);
    }

    @Test
    void getUserDetails_shouldThrowException_whenUserNotFound() {
        Long userId = 1L;

        Mockito.when(userRepository.findWithAllRelationsById(userId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                userService.getUserDetails(userId));
    }

    @Test
    void getUsersFiltered_shouldReturnFilteredList() {
        LocalDate date = LocalDate.of(2000, 1, 1);
        List<CommonUserProjection> list = List.of(
                new CommonUserProjection() {
                    @Override
                    public Long getId() {
                        return 1L;
                    }

                    @Override
                    public String getName() {
                        return "Alice";
                    }

                    @Override
                    public LocalDate getDateOfBirth() {
                        return null;
                    }
                }
        );

        Mockito.when(userRepository.findByDateOfBirthAfter(date))
                .thenReturn(list);

        List<CommonUserProjection> result = userService.getUsersFiltered(date);

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getName());
    }
}