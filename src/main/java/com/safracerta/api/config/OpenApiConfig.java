package com.safracerta.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI safraCertaOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("SafraCerta API")
                .description("Monitoramento de talhões: cadastro, leituras, análise de risco e alertas.")
                .version("v1"));
    }
}
