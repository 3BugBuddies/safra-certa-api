# Spec 04-1: Motor de Risco + Análise do Talhão

**Status:** Aprovado (2026-06-07) · **Frente:** 1 (espinha) · **Depende de:** — · **Base:** análise `04-motor-ia-visoes.md`, decisões 1, 3, 10.

## Objetivo
A cada `POST /leitura` de um talhão com safra **ATIVA**, calcular o `nivelRisco` (contagem de fatores) e persistir uma `AnaliseTalhao` como snapshot. Garantir 1 safra ATIVA por talhão. Remover a entidade `Alerta`.

## Escopo
- Cálculo determinístico do nível de risco no fluxo de ingestão.
- Persistência da `AnaliseTalhao` (sem diagnóstico/recomendação — campos ficam `null`; a IA preenche na Frente 2).
- Regra de unicidade de safra ATIVA (409).
- Remoção do `Alerta`.

**Fora de escopo:** IA (Frente 2), endpoints de consulta (Frente 3), gatilho de UI.

## Arquivos

**Criar:**
- `entity/enums/` — (reusar `NivelRisco`, já existe).
- `repository/AnaliseTalhaoRepository.java`
- `service/MotorDeRiscoService.java`

**Alterar:**
- `service/LeituraSensorService.java` — chamar o motor após salvar a leitura.
- `repository/SafraTalhaoRepository.java` — query da safra ativa.
- `service/SafraTalhaoService.java` — rejeitar 2ª safra ATIVA.

**Remover:**
- `entity/Alerta.java` (e qualquer referência — não há repository/service/controller de Alerta hoje).

## Detalhe técnico

### `repository/AnaliseTalhaoRepository.java`
```java
public interface AnaliseTalhaoRepository extends JpaRepository<AnaliseTalhao, Long> {
    // usado nas Frentes 3 e 4; já criado aqui pois o motor salva
}
```

### `repository/SafraTalhaoRepository.java` (alterar)
Adicionar:
```java
Optional<SafraTalhao> findFirstByTalhaoIdAndStatusSafra(Long talhaoId, StatusSafra statusSafra);
```
(reaproveita `existsByTalhaoIdAndStatusSafra`, que já existe.)

### `service/MotorDeRiscoService.java` (criar)
- Método `@Transactional void avaliar(LeituraSensor leitura)` — roda dentro da transação de `ingerir()`.
- Passos:
  1. `talhao = leitura.getTalhao()`.
  2. `safra = safraTalhaoRepository.findFirstByTalhaoIdAndStatusSafra(talhao.getId(), ATIVA)`; se vazio → **retorna** (sem análise).
  3. `cultura = safra.getCultura()`.
  4. **Medição (snapshot)**: `new Medicao(leitura.getTemperatura(), leitura.getUmidadeAr(), leitura.getRadiacaoSolar(), leitura.getUmidadeSolo())`.
  5. **Previsão (snapshot)**: buscar última via `previsaoRepository.findFirstByTalhaoIdOrderByDataHoraDesc(talhaoId)`; se presente, montar `Previsao(chuva, umidadeAr, temperatura, temperaturaMin, temperaturaMax, radiacaoSolar, umidadeSolo)`; senão `null`.
  6. **Contagem de fatores** (cada um só conta se o limite da cultura **e** o dado existem):
     - Solo seco: `umidadeSoloCritica != null && leitura.umidadeSolo != null && leitura.umidadeSolo < umidadeSoloCritica`.
     - Geada: `temperaturaMinCritica != null && previsao?.temperaturaMin != null && previsao.temperaturaMin < temperaturaMinCritica`.
     - Calor: `temperaturaMaxCritica != null && previsao?.temperaturaMax != null && previsao.temperaturaMax > temperaturaMaxCritica`.
     - Déficit hídrico: `chuvaMinima != null && previsao?.chuva != null && previsao.chuva < chuvaMinima`.
  7. **Nível**: `switch(contagem) { 0→NORMAL; 1→BAIXO; 2→MEDIO; default→ALTO }`.
  8. Criar `AnaliseTalhao(safra, now(), medicao, previsao, nivel, diagnostico=null, recomendacao=null)` e salvar.
  9. **Seam da IA (Frente 2):** após montar a análise e antes/depois do save, deixar ponto de extensão para `IaService` preencher `diagnostico`/`recomendacao`. Na Frente 1, não injeta IA.

### `service/LeituraSensorService.java` (alterar)
- Injetar `MotorDeRiscoService`.
- Em `ingerir()`, após `LeituraSensor leitura = repository.save(...)` e `atualizarPrevisaoComThrottle(...)`, chamar `motorDeRiscoService.avaliar(leitura)`.
- **Ordem:** atualizar previsão **antes** de avaliar (motor lê a previsão mais fresca). Falha do motor **não** deve quebrar a ingestão? → Decisão de borda: o motor roda na mesma transação; se ele lançar, a leitura faz rollback. Como o motor é tolerante a dados parciais (não lança por dado faltante), o risco é baixo. Manter na mesma transação (consistência leitura↔análise).

### `service/SafraTalhaoService.java` (alterar) — regra de safra ATIVA
- Em `criar()`: se `req` resultará em `statusSafra == ATIVA` (default é ATIVA) e `repository.existsByTalhaoIdAndStatusSafra(req.talhaoId(), ATIVA)` → `throw new ConflictException("Talhão já possui safra ativa: " + req.talhaoId())`.
- Em `atualizar()`: se o alvo for `ATIVA` e existir outra safra ATIVA no mesmo talhão com id diferente → 409. (checar via lista `findByTalhaoId` filtrando `ATIVA` e id != atual, ou método dedicado.)

## Regras de negócio
- Talhão sem safra ATIVA → nenhuma análise criada.
- Limite da cultura nulo ou dado faltante → fator ignorado.
- Sem previsão → só o fator de solo é avaliável.
- 1 safra ATIVA por talhão (409 na violação).

## Critérios de aceitação
- [ ] `POST /leitura` em talhão com safra ATIVA cria uma `AnaliseTalhao` com `nivelRisco` coerente com a contagem.
- [ ] `POST /leitura` em talhão **sem** safra ATIVA **não** cria análise e retorna 201 normal.
- [ ] A análise preserva os valores de medição/previsão usados (snapshot).
- [ ] Cadastrar 2ª safra ATIVA no mesmo talhão retorna 409.
- [ ] Não há mais entidade `Alerta` nem referências a ela; `mvn clean compile` BUILD SUCCESS.

## Verificação
`mvn clean compile`. Smoke (com Oracle): postar leitura e conferir a análise criada com o nível esperado para limites de cultura conhecidos.
