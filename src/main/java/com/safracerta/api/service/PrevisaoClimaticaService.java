package com.safracerta.api.service;

import com.safracerta.api.client.openmeteo.OpenMeteoClient;
import com.safracerta.api.client.openmeteo.OpenMeteoMapper;
import com.safracerta.api.client.openmeteo.OpenMeteoProperties;
import com.safracerta.api.entity.PrevisaoClimatica;
import com.safracerta.api.entity.Talhao;
import com.safracerta.api.repository.PrevisaoClimaticaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PrevisaoClimaticaService {

    private static final Logger log = LoggerFactory.getLogger(PrevisaoClimaticaService.class);

    private final PrevisaoClimaticaRepository repository;
    private final OpenMeteoClient openMeteoClient;
    private final OpenMeteoMapper openMeteoMapper;
    private final OpenMeteoProperties props;

    public PrevisaoClimaticaService(PrevisaoClimaticaRepository repository,
                                    OpenMeteoClient openMeteoClient,
                                    OpenMeteoMapper openMeteoMapper,
                                    OpenMeteoProperties props) {
        this.repository = repository;
        this.openMeteoClient = openMeteoClient;
        this.openMeteoMapper = openMeteoMapper;
        this.props = props;
    }

    public List<PrevisaoClimatica> listar(Long talhaoId) {
        return repository.findByTalhaoIdOrderByDataHoraDesc(talhaoId);
    }

    /** Não bloqueante: falha de rede ou centro nulo loga e degrada sem interromper a ingestão. */
    public void atualizarComThrottle(Talhao talhao) {
        try {
            if (talhao.getCentro() == null
                    || talhao.getCentro().getLatitude() == null
                    || talhao.getCentro().getLongitude() == null) {
                log.debug("Talhão {} sem centro definido; previsão ignorada", talhao.getId());
                return;
            }

            Optional<PrevisaoClimatica> ultima =
                    repository.findFirstByTalhaoIdOrderByDataHoraDesc(talhao.getId());
            LocalDateTime limite = LocalDateTime.now().minusHours(props.getThrottleHoras());
            if (ultima.isPresent() && ultima.get().getDataHora().isAfter(limite)) {
                return; // dentro da janela do throttle — reusa a última
            }

            openMeteoClient.consultar(talhao.getCentro())
                    .flatMap(resp -> openMeteoMapper.toEntity(resp, talhao))
                    .ifPresent(repository::save);
        } catch (Exception e) {
            log.warn("Falha ao atualizar previsão do talhão {}: {}", talhao.getId(), e.getMessage());
        }
    }
}
