package com.safracerta.api.client.openmeteo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "safracerta.open-meteo")
@Getter
@Setter
public class OpenMeteoProperties {
    private String baseUrl = "https://api.open-meteo.com/v1/forecast";
    private int throttleHoras = 1;
}
