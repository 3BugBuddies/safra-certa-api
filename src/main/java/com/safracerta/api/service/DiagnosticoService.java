package com.safracerta.api.service;
import com.safracerta.api.dto.analise.ContextoAnalise;
import com.safracerta.api.dto.analise.DiagnosticoIa;

import com.safracerta.api.entity.embeddable.Medicao;
import com.safracerta.api.entity.embeddable.Previsao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

/** Tolerante a falhas: qualquer erro retorna {@code Optional.empty()} e a análise segue sem texto. */
@Service
public class DiagnosticoService {

    private static final Logger log = LoggerFactory.getLogger(DiagnosticoService.class);

    private static final String SYSTEM = """
            Você é um agrônomo que interpreta o risco climático de um talhão agrícola.
            Recebe a cultura, o nível de risco já calculado por um motor determinístico,
            a lista de fatores que dispararam, a medição do sensor e a previsão.

            Regras:
            - NÃO recalcule nem altere o nível de risco — ele já vem decidido.
            - NÃO invente números além dos fornecidos.
            - Escreva em português brasileiro, objetivo e técnico, sem saudações.
            - diagnostico: 1 a 2 frases explicando a situação do talhão a partir dos fatores.
            - recomendacao: 1 a 2 frases com orientação prática e qualitativa ao produtor.
            - Se não houver fatores de risco, descreva a condição como adequada.
            """;

    private final ChatClient chatClient;

    public DiagnosticoService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public Optional<DiagnosticoIa> diagnosticar(ContextoAnalise ctx) {
        if (ctx == null) {
            return Optional.empty();
        }
        try {
            DiagnosticoIa resultado = chatClient.prompt()
                    .system(SYSTEM)
                    .user(montarPrompt(ctx))
                    .call()
                    .entity(DiagnosticoIa.class);
            if (resultado == null
                    || (isBlank(resultado.diagnostico()) && isBlank(resultado.recomendacao()))) {
                return Optional.empty();
            }
            return Optional.of(resultado);
        } catch (Exception e) {
            log.warn("[IA] fallback (análise segue sem texto): {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String montarPrompt(ContextoAnalise ctx) {
        String fatores = (ctx.fatores() == null || ctx.fatores().isEmpty())
                ? "nenhum"
                : String.join("; ", ctx.fatores());
        return """
                CULTURA: %s
                NIVEL_DE_RISCO: %s
                FATORES_DISPARADOS: %s
                MEDICAO: %s
                PREVISAO: %s
                """.formatted(
                ctx.culturaNome(), ctx.nivel(), fatores,
                descreverMedicao(ctx.medicao()), descreverPrevisao(ctx.previsao()));
    }

    private static String descreverMedicao(Medicao m) {
        if (m == null) {
            return "sem medição";
        }
        return "temp=%s C, umidadeAr=%s %%, umidadeSolo=%s %%, radiacao=%s"
                .formatted(m.getTemperatura(), m.getUmidadeAr(), m.getUmidadeSolo(), m.getRadiacaoSolar());
    }

    private static String descreverPrevisao(Previsao p) {
        if (p == null) {
            return "sem previsão";
        }
        return "chuva=%s, tempMin=%s C, tempMax=%s C, umidadeSolo=%s %%"
                .formatted(p.getChuva(), p.getTemperaturaMin(), p.getTemperaturaMax(), p.getUmidadeSolo());
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
