package com.safracerta.api.client.openmeteo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(Daily daily, Hourly hourly) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Daily(
            List<String> time,
            @JsonProperty("temperature_2m_mean") List<Double> temperaturaMedia,
            @JsonProperty("temperature_2m_min") List<Double> temperaturaMin,
            @JsonProperty("temperature_2m_max") List<Double> temperaturaMax,
            @JsonProperty("precipitation_sum") List<Double> precipitacao) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hourly(
            List<String> time,
            @JsonProperty("relative_humidity_2m") List<Double> umidadeAr,
            @JsonProperty("soil_moisture_3_to_9cm") List<Double> umidadeSolo,
            @JsonProperty("shortwave_radiation") List<Double> radiacao) {}
}
