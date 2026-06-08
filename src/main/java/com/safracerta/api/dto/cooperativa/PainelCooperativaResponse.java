package com.safracerta.api.dto.cooperativa;

/**
 * Painel agregado da cooperativa (tela da cooperativa no app).
 * Devolve métricas de domínio cruas; o front rotula/agrupa (ex.: "em risco" = alerta + critico).
 */
public record PainelCooperativaResponse(
        Long cooperativaId,
        String nome,
        long qtdProdutores,
        double totalHectares,
        DistribuicaoRisco distribuicao   // contagem de TALHÕES por nível (última análise de cada)
) {
    /** Talhões sem análise não entram em nenhum balde (estado "sem dado"). */
    public record DistribuicaoRisco(long saudavel, long atencao, long alerta, long critico) {}
}
