package com.safracerta.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS básico para o deploy provisório — libera o front a consumir a API.
 * Origens configuráveis via {@code safracerta.cors.allowed-origins} (default {@code *}).
 * Sem {@code allowCredentials} no provisório, para poder usar origem curinga.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${safracerta.cors.allowed-origins:*}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
