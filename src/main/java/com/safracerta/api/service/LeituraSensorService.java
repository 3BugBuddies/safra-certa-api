package com.safracerta.api.service;

import com.safracerta.api.client.openmeteo.OpenMeteoClient;
import com.safracerta.api.client.openmeteo.OpenMeteoMapper;
import com.safracerta.api.client.openmeteo.OpenMeteoProperties;
import com.safracerta.api.dto.leitura.LeituraRequest;
import com.safracerta.api.entity.Dispositivo;
import com.safracerta.api.entity.LeituraSensor;
import com.safracerta.api.entity.PrevisaoClimatica;
import com.safracerta.api.entity.Talhao;
import com.safracerta.api.exception.ConflictException;
import com.safracerta.api.exception.NotFoundException;
import com.safracerta.api.repository.DispositivoRepository;
import com.safracerta.api.repository.LeituraSensorRepository;
import com.safracerta.api.repository.PrevisaoClimaticaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LeituraSensorService {

    private static final Logger log = LoggerFactory.getLogger(LeituraSensorService.class);

    private final LeituraSensorRepository repository;
    private final DispositivoRepository dispositivoRepository;
    private final PrevisaoClimaticaRepository previsaoRepository;
    private final OpenMeteoClient openMeteoClient;
    private final OpenMeteoMapper openMeteoMapper;
    private final OpenMeteoProperties props;
    private final MotorDeRiscoService motorDeRiscoService;

    public LeituraSensorService(LeituraSensorRepository repository,
                                DispositivoRepository dispositivoRepository,
                                PrevisaoClimaticaRepository previsaoRepository,
                                OpenMeteoClient openMeteoClient,
                                OpenMeteoMapper openMeteoMapper,
                                OpenMeteoProperties props,
                                MotorDeRiscoService motorDeRiscoService) {
        this.repository = repository;
        this.dispositivoRepository = dispositivoRepository;
        this.previsaoRepository = previsaoRepository;
        this.openMeteoClient = openMeteoClient;
        this.openMeteoMapper = openMeteoMapper;
        this.props = props;
        this.motorDeRiscoService = motorDeRiscoService;
    }

    public List<LeituraSensor> listar(Long talhaoId) {
        return repository.findByTalhaoIdOrderByDataHoraDesc(talhaoId);
    }

    @Transactional
    public LeituraSensor ingerir(LeituraRequest req) {
        Dispositivo dispositivo = dispositivoRepository
                .findByCodigoDispositivo(req.codigoDispositivo())
                .orElseThrow(() -> new NotFoundException(
                        "Dispositivo não encontrado: código " + req.codigoDispositivo()));

        if (!Boolean.TRUE.equals(dispositivo.getAtivo())) {
            throw new ConflictException("Dispositivo inativo: " + req.codigoDispositivo());
        }

        LeituraSensor leitura = repository.save(req.toEntity(dispositivo));
        atualizarPrevisaoComThrottle(dispositivo.getTalhao());
        motorDeRiscoService.avaliar(leitura);
        return leitura;
    }

    /**
     * Coleta a previsão do talhão via Open-Meteo apenas se a última for mais
     * antiga que o throttle. Não bloqueante: qualquer falha (rede, centro nulo)
     * é logada e a ingestão segue (degrada silenciosamente).
     */
    private void atualizarPrevisaoComThrottle(Talhao talhao) {
        try {
            if (talhao.getCentro() == null
                    || talhao.getCentro().getLatitude() == null
                    || talhao.getCentro().getLongitude() == null) {
                log.debug("Talhão {} sem centro definido; previsão ignorada", talhao.getId());
                return;
            }

            Optional<PrevisaoClimatica> ultima =
                    previsaoRepository.findFirstByTalhaoIdOrderByDataHoraDesc(talhao.getId());
            LocalDateTime limite = LocalDateTime.now().minusHours(props.getThrottleHoras());
            if (ultima.isPresent() && ultima.get().getDataHora().isAfter(limite)) {
                return; // dentro da janela do throttle — reusa a última
            }

            openMeteoClient.consultar(talhao.getCentro())
                    .flatMap(resp -> openMeteoMapper.toEntity(resp, talhao))
                    .ifPresent(previsaoRepository::save);
        } catch (Exception e) {
            log.warn("Falha ao atualizar previsão do talhão {}: {}", talhao.getId(), e.getMessage());
        }
    }
}
