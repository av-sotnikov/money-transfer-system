package com.custom.payment;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@OpenAPIDefinition(
        info = @Info(
                title = "Система платежных переводов",
                version = "1",
                description = "Bank Payment System"
        )
)
@SpringBootApplication
@EnableAspectJAutoProxy
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }

}
