# Spec 04-4: Telas da Cooperativa — Visões Agregadas + Payload Rico (app mobile)

**Status:** Implementado (compila — `mvn clean compile` BUILD SUCCESS; smoke runtime pendente de Oracle) · **Frente:** 4 · **Depende de:** Frente 1 (dados) + Frente 3 (última análise por talhão) · **Base:** decisões 2, 5, 7, 8.

## Realinhamento de produto
**Não existe "Dashboard Web".** É **um único app React Native** consumindo a API Java, com duas áreas: **PRODUTOR** (vê os próprios dados) e **cooperativa** (CRUD + visões agregadas). Mockups no board do Miro (`uXjVHLn0kVM`, telas mobile à esquerda dos ERDs). Esta frente entrega os **endpoints agregados** das telas da cooperativa — não um cliente separado.

## Decisões aplicadas
1. **Vocabulário de risco domínio-puro (mudança no PRD 04):** o enum `NivelRisco` virou `{SAUDAVEL, ATENCAO, ALERTA, CRITICO}` (4 níveis nomeados), devolvido **cru**. O DE-PARA 4→3 (`RotuloRisco`) foi **removido** — o front renderiza rótulo/cor. Revisa a Decisão 4. Motor mapeia por contagem: `0=SAUDAVEL, 1=ATENCAO, 2=ALERTA, 3+=CRITICO`.
2. **"Em risco" = nível ∈ {ALERTA, CRITICO}.** No painel a API devolve a **distribuição crua por nível**; o front soma "quantos em risco". (No card de produtor, `qtdTalhoesEmRisco` é conveniência de negócio.)
3. **Endpoints nos controllers existentes** (`CooperativaController`, `ProdutorController`, `TalhaoController`) — sem `PainelController`.
4. **DTOs por domínio** (`dto/cooperativa/`, `dto/produtor/`, `dto/talhao/`).
5. **Assemblers `RepresentationModelAssembler` simples** (consistente com os 7 atuais). Revisa a Decisão 7.
6. **Sem login/auth** (descopado).
7. **Fatores de risco não persistidos** — o motor calcula e descarta; o front aproxima pelo nível + texto da IA. *(Gabriel pode expandir depois: persistir `fatores` exigiria formatá-los pro front.)*

## Escopo
- `GET /cooperativa/{id}/painel` — contadores + distribuição por nível (sem mapa de calor).
- `GET /cooperativa/{id}/produtores` — cards de produtor com agregados.
- `GET /produtor/{id}/talhoes` — situação de cada talhão (mapa de talhões).
- `GET /talhao/{id}/situacao` — situação de um talhão.
- Relacionamentos bidirecionais.

**Fora de escopo:** mapa de calor; login/auth; persistir fatores; CRUD existente intacto.

## Arquivos (implementados)

**Entidades — `@OneToMany` inverso (LAZY, sem cascade):**
- `Cooperativa.produtores`, `Produtor.talhoes`, `Talhao.safras`, `SafraTalhao.analises`. Serializa só DTO (Decisão 2).

**DTOs read-model:**
- `dto/cooperativa/PainelCooperativaResponse.java` (+ nested `DistribuicaoRisco`).
- `dto/produtor/ProdutorCardResponse.java`.
- `dto/talhao/TalhaoSituacaoResponse.java`.
- Reuso: `CoordenadaDto`/`TalhaoPontoDto` (`dto/talhao`), enum `NivelRisco`.

**Serviço / assemblers:**
- Leituras **nos services dos recursos** (sem service de visão à parte): `CooperativaService.painel`/`produtoresCards`, `ProdutorService.talhoesSituacao`/`cardDe`, `TalhaoService.situacao`/`montarSituacao`/`nivelAtual`. `@Transactional(readOnly=true)`, navegam os bidirecionais; última análise via `AnaliseTalhaoRepository.findFirstBySafraTalhao_Talhao_IdOrderByDataHoraAnaliseDesc`. Lógica compartilhada (montar situação + nível por talhão) ancorada no `TalhaoService`, reusada por Produtor/Cooperativa.
- `assembler/{PainelCooperativaAssembler, ProdutorCardAssembler, TalhaoSituacaoAssembler}.java`.

**Controllers — `@GetMapping` adicionados:**
- `CooperativaController` → `/{id}/painel`, `/{id}/produtores`.
- `ProdutorController` → `/{id}/talhoes`.
- `TalhaoController` → `/{id}/situacao`.

## DTOs (como ficaram)

```java
public record PainelCooperativaResponse(
    Long cooperativaId, String nome,
    long qtdProdutores, double totalHectares,
    DistribuicaoRisco distribuicao            // contagem de TALHÕES por nível (última análise)
) {
    public record DistribuicaoRisco(long saudavel, long atencao, long alerta, long critico) {}
}

public record ProdutorCardResponse(
    Long id, String nome, String cidade, String uf, String telefone,
    double areaTotalHa, long qtdTalhoes, long qtdTalhoesEmRisco,   // {ALERTA, CRITICO}
    NivelRisco nivelAgregado                  // pior nível (Decisão 5); null se sem análise
) {}

public record TalhaoSituacaoResponse(
    Long id, String nome, Double areaHa, CoordenadaDto centro, List<TalhaoPontoDto> pontos,
    String culturaNome,                       // da safra ATIVA
    Double umidadeSolo, Double temperatura,   // da última medição (AnaliseTalhao.medicaoAtual)
    NivelRisco nivelRisco                     // null = "sem dado"
) {}
```

## Agregação
- Nível por talhão = **última `AnaliseTalhao`** (`findFirstBySafraTalhao_Talhao_IdOrderByDataHoraAnaliseDesc`).
- `nivelAgregado` = pior nível entre os talhões do produtor (max por `ordinal()`; o enum ordena SAUDAVEL<ATENCAO<ALERTA<CRITICO).
- Talhão **sem safra ATIVA ou sem análise** → "sem dado" (não conta na distribuição/em risco; campos nulos).
- **N+1 (Decisão 2):** navegação demo-pragmática (uma query de última análise por talhão). Refinamento futuro: JPQL agregada se houver lentidão.

## HATEOAS
- `painel` → self + `produtores`.
- `produtoresCards` → cada card linka self (`/produtor/{id}`) + `talhoes` (`/produtor/{id}/talhoes`).
- `talhoesSituacao` / `situacao` → self + `talhao` (`/talhao/{id}`) + `analises` (`/analise?talhaoId=`).

## Critérios de aceitação
- [x] `GET /cooperativa/{id}/painel` retorna nº produtores, total hectares e distribuição por nível.
- [x] `GET /cooperativa/{id}/produtores` retorna área total, nº talhões, nº em risco e nível agregado (pior).
- [x] `GET /produtor/{id}/talhoes` retorna nome, área, cultura, umidade/temp da última medição, nível, polígono.
- [x] Nenhuma resposta serializa entidade direto (só DTO).
- [x] `_links` navegam cooperativa → produtores → talhões → análises.
- [x] Talhão sem análise = "sem dado" sem quebrar a agregação.
- [x] `mvn clean compile` BUILD SUCCESS.
- [ ] Smoke runtime (exige Oracle + dados): conferir os 4 endpoints contra os mockups.

## Verificação
`mvn clean compile` ✅. Smoke pendente: popular cooperativa/produtor/talhão/safra + leituras e conferir os 4 endpoints.
