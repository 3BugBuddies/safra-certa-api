package com.safracerta.api.dto.cooperativa;

/** Métricas cruas da cooperativa; "em risco" (alerta + critico) é calculado no front. */
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
