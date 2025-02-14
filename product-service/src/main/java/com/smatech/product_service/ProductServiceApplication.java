package com.smatech.product_service;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@OpenAPIDefinition(
		info = @Info(
				title = "PRODUCT SERVICE",
				description = "REST APIs FOR PRODUCT ACTIONS",
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
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

}
