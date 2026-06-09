package com.safracerta.api.service;

import com.safracerta.api.dto.leitura.LeituraRequest;
import com.safracerta.api.entity.Dispositivo;
import com.safracerta.api.entity.LeituraSensor;
import com.safracerta.api.exception.NotFoundException;
import com.safracerta.api.repository.DispositivoRepository;
import com.safracerta.api.repository.LeituraSensorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LeituraSensorService {

    private final LeituraSensorRepository repository;
    private final DispositivoRepository dispositivoRepository;
    private final PrevisaoClimaticaService previsaoService;
    private final MotorDeRiscoService motorDeRiscoService;

    public LeituraSensorService(LeituraSensorRepository repository,
                                DispositivoRepository dispositivoRepository,
                                PrevisaoClimaticaService previsaoService,
                                MotorDeRiscoService motorDeRiscoService) {
        this.repository = repository;
        this.dispositivoRepository = dispositivoRepository;
        this.previsaoService = previsaoService;
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

        // Dispositivo nasce inativo; a primeira leitura real o ativa
        // (entidade gerida na transação → flush no commit, sem save explícito).
        if (!Boolean.TRUE.equals(dispositivo.getAtivo())) {
            dispositivo.setAtivo(Boolean.TRUE);
        }

        LeituraSensor leitura = repository.save(req.toEntity(dispositivo));
        previsaoService.atualizarComThrottle(dispositivo.getTalhao());
        motorDeRiscoService.avaliar(leitura);
        return leitura;
    }
}
