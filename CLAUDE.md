# CLAUDE.md — api-java

Backend **único** do SafraCerta (decisão da equipe: só Java nesta rodada). Todo o domínio + Motor de Risco + integração IA. Contexto de produto e modelo de dados completos no `CLAUDE.md` da raiz do workspace.

> **Antes de mexer em qualquer entidade:** ler `.claude/docs/specs/01-modelo-dados-java.md` (modelo vigente) **e** `.claude/docs/decisions/01-heranca-registro-climatico.md` (ADR da herança). O modelo é a fonte de verdade.

## Stack

- Java 21 (LTS) · Spring Boot **3.4.5** · Maven (`mvn` global — **sem** wrapper `./mvnw` no repo)
- Spring Web, Spring Data JPA, HATEOAS, Validation, Lombok, Oracle JDBC, Springdoc OpenAPI
- **Spring AI 1.1.6** (Gemini via starter `spring-ai-starter-model-openai`, endpoint OpenAI-compatible)
- Datasource **Oracle** (`application.properties`); `ddl-auto: create` em dev (recria o schema a cada boot — facilita testes)

## Comandos

```bash
mvn spring-boot:run                 # dev local (precisa de Oracle rodando)
mvn clean compile                   # valida compilação (não conecta no banco)
mvn test                            # todos os testes
mvn clean package -DskipTests       # gerar JAR
```

> 💡 Não há `./mvnw` — considerar `mvn wrapper:wrapper` pra fixar a versão do Maven no repo.

> ✅ **Spring AI habilitado:** Boot 3.4.5 + Spring AI 1.1.6 + `spring-ai-starter-model-openai`. Gemini consumido pelo endpoint OpenAI-compatible (`spring.ai.openai.*` no `application.properties`); `GEMINI_API_KEY` é secret de ambiente. `DiagnosticoService` (em `service/`) usa o `ChatClient` do Spring AI direto, injetado via `ObjectProvider` no motor — se a chave/serviço faltar, a análise é salva **sem** texto (fallback).

## Arquitetura — por CAMADAS (não por módulo)

Estrutura de pacotes (pasta = `package` declarado — reconciliados; sem divergência):

```
com/safracerta/api/
├── entity/           10 entidades + RegistroClimatico (abstrata)
│   ├── embeddable/   Coordenada, Medicao, Previsao
│   └── enums/        NivelRisco, StatusSafra
├── repository/       um JpaRepository por raiz de agregado
├── service/          CRUD + MotorDeRiscoService (motor determinístico) + DiagnosticoService (texto via Gemini)
├── controller/       um por recurso
├── assembler/        um RepresentationModelAssembler (HATEOAS) por recurso
├── dto/              um subpacote por domínio, flat dentro (sem request/response aninhados)
│   ├── cooperativa/  CooperativaRequest, CooperativaResponse
│   ├── produtor/ · cultura/ · safra/ · dispositivo/   (Request + Response cada)
│   ├── talhao/       TalhaoRequest, TalhaoResponse, CoordenadaDto, TalhaoPontoDto
│   ├── leitura/      LeituraRequest, LeituraResponse
│   ├── previsao/     PrevisaoResponse, PrevisaoDto
│   └── analise/      AnaliseResponse, MedicaoDto, RotuloRisco, ContextoAnalise (input IA), DiagnosticoIa (output IA)
├── validation/       @OrdensDistintas + OrdensDistintasValidator (constraint Bean Validation)
├── client/           integrações HTTP externas, um subpacote por provider
│   └── openmeteo/    OpenMeteoClient (só I/O) + OpenMeteoResponse (DTO cru) + OpenMeteoMapper (→ PrevisaoClimatica) + OpenMeteoProperties
├── exception/        NotFoundException, ConflictException
├── handler/          GlobalExceptionHandler, ErrorResponse
└── config/           ChatClientConfig, CorsConfig, OpenApiConfig, CulturaSeeder
```

**Convenções da estrutura:**
- **Sem wrapper `model/`** — `entity/` é raiz; assemblers em `assembler/` (não em `model/` — são camada de apresentação).
- **Embeddables** em `entity/embeddable/` (subpacote próprio, fora do meio das entidades).
- **DTOs por domínio** (`dto/<dominio>/`, flat) — Request + Response do mesmo recurso co-locados. Sem `request/`/`response/` aninhados (alinha com a convenção global de DTOs Java).
- **`@OrdensDistintas`** vive em `validation/` (é constraint, não DTO).
- **Gemini não tem `client/`** — o `ChatClient` do Spring AI (bean em `config/ChatClientConfig`) já é a camada de client. A lógica de prompt/fallback é regra de negócio → `DiagnosticoService` em `service/`; os records de input/output ficam em `dto/analise/`. OpenMeteo tem `client/openmeteo/` só porque envolve `RestClient` cru (sem abstração de lib).

Mapa de classes: `.claude/docs/analysis/03-mapa-de-classes.md`.

## Convenções de entidade

- FKs como `@ManyToOne(fetch = LAZY)`, sem cascade delete (exceto `Talhao.pontos`: cascade ALL + orphanRemoval).
- Ids via `@SequenceGenerator` (`SEQ_<TABELA>`); decimais climáticos como `Double`.
- Embeddables em `entity/embeddable/`; enums em `entity/enums/`.
- **Herança climática:** `RegistroClimatico` abstrata com `@Inheritance(TABLE_PER_CLASS)` → `LeituraSensor` + `PrevisaoClimatica`. Sequence **compartilhada** `SEQ_REGISTRO_CLIMATICO` (id único global). **Nunca** rebaixar para `@MappedSuperclass` — perde o item de Modelagem Avançada (ADR 01).
- Embeddables `Medicao`/`Previsao` servem **só** o snapshot da `AnaliseTalhao`; as leituras carregam os campos herdados.
- **Previsão (Decisão 4, PRD 03):** `PrevisaoClimatica` e o embeddable `Previsao` têm `temperaturaMin`/`temperaturaMax` (D+1); a `temperatura` herdada é a **média** do dia na previsão (valor medido na leitura). Open-Meteo: temperatura (média/min/máx) e `chuva` (soma) do `daily`; umidade ar/solo e radiação do `hourly` (média D+1), unidades do sensor. **`soil_moisture` vem em m³/m³ (0–1) → convertida ×100 para %** no `OpenMeteoMapper` (alinha com sensor e `Cultura.umidadeSoloCritica`).

## Estado atual

**Cobertura atual (o que a API entrega hoje):**
- **CRUD completo** (REST + HATEOAS): Cooperativa, Produtor, Cultura, Talhão (+polígono), Safra, Dispositivo.
- **Ingestão:** `POST /leitura` (ESP32) + coleta de Previsão (Open-Meteo) no fluxo, com throttle.
- **Motor de Risco** determinístico (4 fatores → `NivelRisco`) na ingestão, gerando `AnaliseTalhao` (snapshot); **IA Gemini** opcional preenche `diagnostico`/`recomendacao` (fallback).
- **Consulta de análise:** `GET /analise` (histórico), `/analise/ultima`, `/analise/{id}`.
- **Visões da cooperativa (Frente 4):** `/cooperativa/{id}/painel`, `/cooperativa/{id}/produtores`, `/produtor/{id}/talhoes`, `/talhao/{id}/situacao`.
- **Catálogo:** seed automático de 14 culturas embasadas no boot.
- **Infra:** Swagger (`/swagger-ui.html`), CORS aberto (provisório), `application.properties`, sem auth.
- Documentação de API: collection Postman em `postman/SafraCerta.postman_collection.json`.

Camada de entidade implementada (10 tabelas + herança + `Dispositivo`).

**PRD 02 (CRUD básico) implementado** — spec `.claude/docs/specs/02-crud-basico.md`. Camadas `repository/service/controller/dto/config` para 5 recursos: Cooperativa, Produtor, Cultura, Talhão (+pontos), SafraTalhao. Convenções aplicadas:
- Mapeamento Request→Entity **no próprio DTO** (`toEntity()`/`applyTo()`), espelhando `from()` do Response — sem helper privado no service.
- HATEOAS via `RepresentationModelAssembler` (um por recurso, em `assembler/`); controller retorna `EntityModel`/`CollectionModel`.
- Unicidade via **derived queries** (`existsByCnpj`, `existsByCpf`...) → `ConflictException` (409). **Delete em cascata** (via service, transacional): Cooperativa → Produtores → Talhões → (análises/leituras/dispositivos/safras). **Sem** bloqueio por dependentes — o front confirma a irreversibilidade. *(Reverteu a Decisão 1 do PRD 02 — barreira não se pagava.)*
- `ordem` única no polígono via constraint Bean Validation custom `@OrdensDistintas` (400).
- Tratamento de erro: `handler/` (`GlobalExceptionHandler` — 400/404/409 + `DataIntegrityViolationException` — e `ErrorResponse`) + `exception/` (`NotFoundException`, `ConflictException`); `OpenApiConfig` em `config/`.
- Verificado por `mvn clean compile` (BUILD SUCCESS). Smoke de runtime/Swagger exige Oracle de pé.

**PRD 03 (ingestão de leituras + dispositivos) implementado** — spec `.claude/docs/specs/03-ingestao-leituras.md`, decisões `.claude/docs/decisions/03-ingestao-leituras-decisions.md`.
- `Dispositivo`: CRUD completo (HATEOAS), regra "1 por talhão" (409), unicidade de código (409), **delete em cascata** das leituras (sem barreira de safra ativa — reverteu a Decisão 2 do PRD 03).
- `POST /leitura` (DTO simples, payload ESP): valida dispositivo (404 não cadastrado / 409 inativo) + faixa física via `@DecimalMin/@DecimalMax` (400). `GET /leitura?talhaoId=`.
- Previsão **no fluxo do POST** com throttle (`safracerta.open-meteo.throttle-horas`, default 1h), não bloqueante (centro nulo/falha externa → degrada). `OpenMeteoClient` (`RestClient`) em `client/`. `GET /previsao?talhaoId=`.
- **Sem** gatilho do Motor de Risco (Decisão 1 — migrou inteiro pro PRD 04).
- **Mudança de modelo (Decisão 4):** `temperaturaMin/Max` na previsão (ver Convenções). Sem migration (`ddl-auto: create`).
- Verificado por `mvn clean compile` (BUILD SUCCESS).

**PRD 04 (Motor de Risco + IA + visões)** — quebrado em **6 frentes** (specs `04-1` a `04-6`). Stack migrada p/ Boot 3.4.5 + Spring AI 1.1.6 (Frente 2). Status por frente:

| # | Frente (spec) | Status spec | Implementação |
|---|---|---|---|
| 1 | Motor de Risco + Análise (`04-1`, espinha) | Aprovado | ✅ `MotorDeRiscoService` + persistência `AnaliseTalhao` |
| 2 | IA Gemini — diagnóstico/recomendação (`04-2`) | Aprovado | ✅ `DiagnosticoService` (`service/`) + `ChatClientConfig` |
| 3 | Consulta de Análise (`04-3`) | Aprovado | ✅ `AnaliseTalhaoController` (`/analise`, `/analise/ultima`, `/analise/{id}`) |
| 4 | Visões Agregadas (telas da cooperativa) (`04-4`) | Implementado | ✅ leituras **nos services dos recursos** (`CooperativaService.painel`/`produtoresCards`, `ProdutorService.talhoesSituacao`, `TalhaoService.situacao`); endpoints `/{id}/painel`, `/{id}/produtores`, `/{id}/talhoes`, `/{id}/situacao` |
| 5 | Endurecimento de Validações (`04-5`) | Aprovado | ⚠️ verificar |
| 6 | CORS + Seed automático de Cultura (`04-6`) | Aprovado | ✅ `CorsConfig` + `CulturaSeeder` (14 culturas embasadas — ver `analysis/seed-culturas-embasamento.md`) |

Detalhes do motor (Frente 1):
- `MotorDeRiscoService.avaliar(leitura)` roda **no fluxo do POST /leituras** (gatilho em `LeituraSensorService.ingerir`). Só age se o talhão tem safra **ATIVA**.
- Determinístico: conta 4 fatores (solo seco — sensor; geada/calor/déficit hídrico — previsão D+1) vs. limites da `Cultura`. Cada fator só conta se limite **e** dado existem. Nível por contagem: `0=SAUDAVEL, 1=ATENCAO, 2=ALERTA, 3+=CRITICO`.
- Persiste `AnaliseTalhao` (snapshot `Medicao` + última `Previsao`). IA **opcional** via `DiagnosticoService` (`ObjectProvider`): preenche `diagnostico`/`recomendacao`; falha/sem chave → análise salva sem texto (fallback).
- **Vocabulário de risco domínio-puro:** enum `NivelRisco {SAUDAVEL, ATENCAO, ALERTA, CRITICO}` devolvido **cru** na API; o front renderiza rótulo/cor. **Não há `RotuloRisco`/DE-PARA** (removido). "Em risco" = nível ∈ {ALERTA, CRITICO}, derivado no front.

Verificado por `mvn clean compile` (BUILD SUCCESS). **Runtime nunca exercitado** (exige Oracle de pé + `GEMINI_API_KEY`).

**Decisão de modelo — `Alerta` removido das entidades:** não há entidade/tabela `ALERTA`. O nível de risco vive em `AnaliseTalhao`; "alerta/crítico" é só nível/rótulo de UI. O marcador 1:1 da modelagem original foi descartado.

**Pendências:** verificar Frente 5 · RM do representante no `README.md` (zerador DevOps) · smoke de runtime + Swagger com Oracle de pé · doc-sync do enum em specs 04-1/04-3, README e PRD 04 (citam `NORMAL/BAIXO/MEDIO/ALTO`).
