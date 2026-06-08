# Spec 04-3: Consulta de Análise

**Status:** Aprovado (2026-06-07) · **Frente:** 3 · **Depende de:** dados da Frente 1 · **Base:** decisões 4, 5.

## Objetivo
Expor a `AnaliseTalhao` para consulta: última análise e histórico por talhão (produtor), com o rótulo de risco (DE-PARA) já no payload.

## Escopo
- `GET /analise?talhaoId=` — histórico (desc por data).
- `GET /analise/{id}` — uma análise.
- `GET /analise/ultima?talhaoId=` — última análise do talhão.

**Fora de escopo:** agregações da cooperativa (Frente 4).

## Arquivos

**Criar:**
- `dto/AnaliseResponse.java`, `dto/MedicaoDto.java`, `dto/PrevisaoDto.java`
- `dto/RotuloRisco.java` (enum/util do DE-PARA) — ou método estático em `AnaliseResponse`.
- `service/AnaliseTalhaoService.java`
- `controller/AnaliseTalhaoController.java`, `controller/AnaliseModelAssembler.java`

**Alterar:**
- `repository/AnaliseTalhaoRepository.java` (criado na Frente 1) — queries de consulta.

## Detalhe técnico

### `repository/AnaliseTalhaoRepository.java`
```java
List<AnaliseTalhao> findBySafraTalhao_Talhao_IdOrderByDataHoraAnaliseDesc(Long talhaoId);
Optional<AnaliseTalhao> findFirstBySafraTalhao_Talhao_IdOrderByDataHoraAnaliseDesc(Long talhaoId);
```
(O caminho `safraTalhao.talhao.id` é válido em derived query; o talhão é derivável da safra.)

### DE-PARA (decisão 4)
`NORMAL`,`BAIXO` → **"Saudável"** · `MEDIO` → **"Alerta"** · `ALTO` → **"Crítico"**.
Implementar como método utilitário (ex. `RotuloRisco.de(NivelRisco)` retornando `String`), reutilizado pela Frente 4.

### `dto/AnaliseResponse.java`
```java
public record AnaliseResponse(
    Long id, Long safraTalhaoId, Long talhaoId,
    LocalDateTime dataHoraAnalise,
    MedicaoDto medicaoAtual, PrevisaoDto previsaoPrevista,
    NivelRisco nivelRisco, String rotuloRisco,
    String diagnostico, String recomendacao
) {
    public static AnaliseResponse from(AnaliseTalhao a) { ... }
}
```
- `talhaoId` = `a.getSafraTalhao().getTalhao().getId()`.
- `rotuloRisco` = DE-PARA do `nivelRisco`.
- `MedicaoDto`/`PrevisaoDto`: espelham os embeddables `Medicao`/`Previsao` com `from(...)`.

### `controller/AnaliseTalhaoController.java` (`/analise`)
- Segue o padrão existente (`TalhaoController.java`): `CollectionModel<EntityModel<AnaliseResponse>>` na lista, `EntityModel` no item.
- `GET /analise?talhaoId=` (obrigatório), `GET /analise/{id}`, `GET /analise/ultima?talhaoId=`.

### `controller/AnaliseModelAssembler.java`
- `RepresentationModelAssembler<AnaliseTalhao, EntityModel<AnaliseResponse>>` (mesmo padrão dos demais).
- Links: self (`/analise/{id}`), collection (`/analise?talhaoId=`), e relacionado `talhao` (`/talhao/{id}`).

## Critérios de aceitação
- [ ] `GET /analise?talhaoId=` retorna o histórico do talhão em ordem decrescente de data.
- [ ] `GET /analise/ultima?talhaoId=` retorna a análise mais recente (404 se não houver).
- [ ] O payload traz `nivelRisco` **e** `rotuloRisco` (Saudável/Alerta/Crítico).
- [ ] `diagnostico`/`recomendacao` aparecem quando a IA (Frente 2) os preencheu; `null` caso contrário.
- [ ] `mvn clean compile` BUILD SUCCESS.

## Verificação
`mvn clean compile`. Smoke: após postar leituras, consultar histórico e última.
