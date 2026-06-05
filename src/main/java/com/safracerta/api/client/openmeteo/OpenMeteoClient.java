package com.safracerta.api.client.openmeteo;

import com.safracerta.api.entity.Coordenada;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

/**
 * Cliente da API de previsão Open-Meteo (gratuita, sem chave). Responsabilidade
 * única: montar a requisição, chamar o HTTP e desserializar a resposta crua.
 * A agregação D+1 e o mapeamento para a entidade ficam no {@link OpenMeteoMapper}.
 */
@Component
public class OpenMeteoClient {

    private final RestClient restClient;

    public OpenMeteoClient(OpenMeteoProperties props) {
        this.restClient = RestClient.builder().baseUrl(props.getBaseUrl()).build();
    }

    /** Consulta o horizonte D+1 (forecast_days=2) para a coordenada. */
    public Optional<OpenMeteoResponse> consultar(Coordenada centro) {
        OpenMeteoResponse resp = restClient.get()
                .uri(uri -> uri
                        .queryParam("latitude", centro.getLatitude())
                        .queryParam("longitude", centro.getLongitude())
                        .queryParam("daily", "temperature_2m_mean,temperature_2m_min,temperature_2m_max,precipitation_sum")
                        .queryParam("hourly", "relative_humidity_2m,soil_moisture_3_to_9cm,shortwave_radiation")
                        .queryParam("forecast_days", 2)
                        .queryParam("timezone", "auto")
                        .build())
                .retrieve()
                .body(OpenMeteoResponse.class);
        return Optional.ofNullable(resp);
    }
}
