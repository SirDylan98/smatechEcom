package com.smatechEcom.api_gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer
{
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/**").allowedMethods(new String[] { "*" }).allowedOrigins(new String[] { "*" }).allowedHeaders(new String[] { "*" });
    }

    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler(new String[] { "/uploads/**" }).addResourceLocations(new String[] { "file:uploads/" });
    }
}