# Análise: PRD 04 — Motor de Risco, IA, Visões de Front e CORS

**Status:** Aprovado (decisões fechadas)
**Data:** 2026-06-06
**Base:** PRD `.claude/docs/prds/04.md` · Brief `.claude/docs/discovery/prd04-consumo-front-coerencia.md` · Decisões `.claude/docs/decisions/04-motor-ia-visoes-decisions.md`

## O que existe hoje

- **Ingestão:** `LeituraSensorService.ingerir()` (`service/LeituraSensorService.java:56`) — leitura chega por `codigoDispositivo` → dispositivo → talhão; salva e atualiza previsão com throttle não bloqueante. **É aqui que o motor pluga**, após o `save`. A leitura **não** conhece a safra ativa — o motor busca pelo talhão.
- **Entidades-alvo já modeladas:** `AnaliseTalhao` (snapshot `Medicao` + `Previsao @AttributeOverrides`, `nivelRisco`, `diagnostico`/`recomendacao` CLOB — `entity/AnaliseTalhao.java`); `Alerta` (marcador `id + analiseTalhao` 1:1 — a remover).
- **Limites da cultura** todos nullable (`entity/Cultura.java:22-26`); **previsão pode faltar** (centro nulo/falha — `LeituraSensorService.java:78`).
- **DTOs de leitura rasos:** devolvem FK id (`TalhaoResponse.java:7`, `SafraTalhaoResponse.java:8`); único `@OneToMany` é `Talhao.pontos` (`Talhao.java:34`).
- **HATEOAS:** assemblers manuais (`RepresentationModelAssembler`) por recurso em `controller/`, envelopando DTO.
- **IA:** Spring AI **desabilitado** (`application.yml:14`; pom).
- **CORS:** inexistente — só `config/OpenApiConfig.java`.
- **`ddl-auto: create`** (`application.yml:11`) — recria schema a cada boot.
- **Padrões a respeitar:** camadas (entity/repository/service/controller/dto/config/client), mapeamento Request→Entity no DTO, exceções via `GlobalExceptionHandler` (400/404/409), herança `RegistroClimatico` TABLE_PER_CLASS (ADR 01).
- **Referência HATEOAS (ToyStore):** `RepresentationModelAssemblerSupport` + entidade no `EntityModel` — bom no assembler tipado, ruim por expor entidade (ciclo com nossos `@OneToMany`).

## Gaps identificados

1. **Motor sem acesso direto à safra/cultura** a partir da leitura (PRD §1) — resolver buscando a safra ATIVA pelo talhão.
2. **Faixa de risco indefinida** (PRD "Perguntas em aberto") — `Cultura` tem limite único, não faixa → **Decisão 3 (contagem de fatores)**.
3. **DE-PARA 4→3 níveis** (PRD §5) — **Decisão 4**.
4. **Nível agregado do produtor** ambíguo (PRD §5) — **Decisão 5 (pior nível)**.
5. **Ciclo de serialização** com bidirecional (PRD §6 + Decisão 2) — mitigado por DTO + assembler (Decisão 7).
6. **IA incompatível com a stack** (PRD "Dependências") — **Decisão 6 (upgrade)** com risco + fallback.
7. **Deploy provisório sem dados** — `ddl-auto: create` zera tudo; análise só nasce de `POST /leitura` → **Decisão 9 (CORS + seed só de Cultura)**.
8. **Múltiplas safras ATIVA** não barradas no modelo — **Decisão 10 (rejeitar 2ª)**.
9. **Bordas do motor** (cultura/dado/previsão faltando) — **Decisão 10 (degrada por fator)**.
10. **Mapa de calor** sem fonte de dados clara — **Decisão 8 (removido)**.

## Decisões tomadas

Ver `.claude/docs/decisions/04-motor-ia-visoes-decisions.md` (10 decisões): remoção do `Alerta`; bidirecional; faixa por contagem; DE-PARA; pior nível agregado; IA via Spring AI + upgrade (com fallback RestClient); DTO rico + `RepresentationModelAssemblerSupport` + links; sem mapa de calor; CORS + seed de Cultura; regras/bordas do motor.

## Escopo confirmado

- Motor determinístico na ingestão; `AnaliseTalhao` como snapshot; nível por contagem de fatores.
- Diagnóstico/recomendação via Gemini (Spring AI) com fallback (análise persiste sem texto se IA falhar).
- Consulta de análise: produtor (última + histórico do seu talhão); operador no contexto da cooperativa.
- Visões agregadas: painel da cooperativa (contadores + distribuição + produtores em risco, **sem mapa de calor**); lista de produtores (área total, nº talhões, nº em risco, nível agregado = pior); mapa de talhões enriquecido (nome/área/cultura/umidade/temp da última medição/nível/polígono).
- Relacionamentos bidirecionais + DTOs ricos + HATEOAS navegável (sem expor entidade).
- Remoção do `Alerta`; alerta = derivado de `nivelRisco != NORMAL`.
- Endurecimento de validações nos cadastros.
- CORS básico + seed automático do catálogo de `Cultura`.

## O que NÃO vai ser feito agora

- Gatilho de análise (timer no ESP / botão no front).
- Mapa de calor / agregação geoespacial.
- Lifecycle de alerta (lido/notificado/resolvido) e notificação push.
- Seed de produtor/talhão/safra/leitura/análise (dashboard fica sem risco até postar leitura).
- Trocar `ddl-auto` de `create`.
- Autenticação / login real.

## Próximos passos — frentes (uma spec cada, p/ aprovação e implementação paralela)

| # | Frente | Conteúdo | Depende de |
|---|---|---|---|
| 1 | **Motor de Risco + Análise** | cálculo (contagem de fatores) no `POST /leitura`, `AnaliseTalhao` snapshot, regra 1-safra-ativa (409), bordas, **remoção do `Alerta`** | — (espinha) |
| 2 | **IA Gemini (Spring AI)** | upgrade de stack (versões fixas + smoke), `IaService` em `client/gemini/`, diagnóstico/recomendação, fallback, `GEMINI_API_KEY` secret | contrato da Frente 1 |
| 3 | **Consulta de análise** | GET última + histórico por talhão (produtor); contexto cooperativa (operador) | dados da Frente 1 |
| 4 | **Visões agregadas + payload rico** | bidirecionais; DTOs ricos; `RepresentationModelAssemblerSupport` + links; DE-PARA; painel coop, lista produtores (pior nível), mapa talhões | dados da Frente 1 |
| 5 | **Validations** | endurecer cadastros (limites da cultura ≥ 0/not null; formatos de documento/UF/CEP/contato) | — (independente) |
| 6 | **CORS + seed de Cultura** | `CorsConfig`; seed automático do catálogo de `Cultura` no boot | — (independente, sai primeiro) |

**Dívida de doc:** sincronizar CLAUDE.md raiz + spec do modelo + ADR 01, que listam `Alerta` como decisão fechada, após a remoção (Frente 1).
