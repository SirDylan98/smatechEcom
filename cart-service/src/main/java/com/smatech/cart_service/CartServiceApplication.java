package com.smatech.cart_service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@OpenAPIDefinition(
		info = @Info(
				title = "CART SERVICE",
				description = "REST APIs FOR CART ACTIONS",
				version = "v1",
				contact = @Contact(
						name = "Dylan Dzvene",
						email = "dylandzvenetashinga@gmail.com"
				),
				license = @License(
						name = "License Information",
						url = "https://www.dylandzvene.com"
				)
		)
)
@SpringBootApplication

@EnableFeignClients
public class CartServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CartServiceApplication.class, args);
	}

}
