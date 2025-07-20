package com.custom.payment.integreation;


import com.custom.payment.PaymentApplication;
import com.custom.payment.db.model.Account;
import com.custom.payment.db.model.User;
import com.custom.payment.db.repository.AccountRepository;
import com.custom.payment.db.repository.UserRepository;
import com.custom.payment.dto.TransferRequest;
import com.custom.payment.redis.service.AccountRedisService;
import com.custom.payment.security.jwt.JwtTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(
        classes = PaymentApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TransferIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.0.0")
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort());

    static {
        postgres.start();
        redis.start();
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountRedisService redisService;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenService jwtTokenService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = userRepository.findUserByLogin("RRoss")
                .orElseThrow(() -> new IllegalStateException("user1 not found"));
        user2 = userRepository.findUserByLogin("HJackson")
                .orElseThrow(() -> new IllegalStateException("user2 not found"));

        // Установим стартовые балансы в Redis (БД уже заполнена CSV)
        redisService.setBalance(user1.getId(), BigDecimal.valueOf(8684));
        redisService.setBalance(user2.getId(), BigDecimal.valueOf(5863));
    }

    @Test
    @Order(1)
    void shouldTransferSuccessfully() throws Exception {
        // Подготовка mock'ов JWT
        when(jwtTokenService.validateToken("mocked-token")).thenReturn(true);
        when(jwtTokenService.getUserIdFromToken("mocked-token")).thenReturn(user1.getId());

        TransferRequest req = new TransferRequest(user2.getId(), new BigDecimal("100"));

        mockMvc.perform(post("/api/v1/accounts/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .header("Authorization", "Bearer mocked-token"))
                .andExpect(status().isOk());

        assertThat(redisService.getBalance(user1.getId())).isEqualByComparingTo("8584.00");

        Account a1 = accountRepository.findByUserId(user1.getId()).orElseThrow();
        Account a2 = accountRepository.findByUserId(user2.getId()).orElseThrow();

        assertThat(a1.getBalance()).isEqualByComparingTo("8584.00");
        assertThat(a2.getBalance()).isEqualByComparingTo("5963.00");
    }
}