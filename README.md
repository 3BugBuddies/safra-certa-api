# safracerta-api — Global Solution 2026/1 | Java Advanced

API REST de monitoramento inteligente de talhões agrícolas, desenvolvida com Spring Boot e Spring AI como parte da Global Solution da disciplina de **Java Advanced (2TDS)** — FIAP 2026/1.

O serviço recebe leituras de sensores ESP32 e dados climáticos do Open-Meteo, executa um motor de risco determinístico a cada nova entrada e aciona o Gemini para gerar diagnóstico e recomendação em linguagem natural. Quando o risco ultrapassa o limiar da cultura plantada, um alerta é registrado e fica disponível para o produtor no app.

---

## Integrantes do Grupo

| Nome | RM |
|------|----|
| Felipe Yuiti Ishii | 565339 |
| Gabriel Nogueira Peixoto | 563925 |
| Giovanna Neri dos Santos | 566154 |
| Mariana Inoue | 565834 |

---

## Configuração — Spring Initializr

### Dependências

| Dependência | Categoria | Descrição |
|-------------|-----------|-----------|
| Spring Web | WEB | API REST + endpoints de ingestão |
| Spring Data JPA | SQL | 10 entidades JPA (domínio agrícola) |
| Oracle Driver | SQL | Oracle XE 21 via Docker |
| Spring HATEOAS | WEB | Hypermedia nos responses |
| Spring AI Google GenAI | AI | Gemini 2.0 Flash — diagnóstico/recomendação · **desabilitado até o PRD 04** (upgrade de stack) |
| Bean Validation | I/O | Validação de DTOs de entrada |
| Spring Boot DevTools | Dev | Reload automático em desenvolvimento |
| Springdoc OpenAPI | Dev | Swagger UI com todos os endpoints |
| Lombok | Dev | Redução de boilerplate nas entidades e DTOs |

---

## Setup rápido para avaliação

```bash
# 1. Copiar e preencher variáveis de ambiente
cp .env.example .env

# 2. Subir Oracle XE + API Java
docker compose up -d

# Acompanhar o Oracle subindo (primeira vez ~5 min)
docker compose logs -f oracle-xe

# 3. Após ambos os containers estarem running
# Swagger: http://localhost:8080/swagger-ui.html
```

---

## Endpoints da API (estado atual)

> **Implementado:** CRUD de cooperativa, produtor, cultura, talhão (+polígono), safra e dispositivo · ingestão de leitura do ESP32 · coleta de previsão via Open-Meteo (no fluxo do POST de leitura, com throttle).
> **Próxima fase (PRD 04):** Motor de Risco + IA (Gemini) e os recursos `AnaliseTalhao` / `Alerta` — ainda **não** expostos.
> **Rotas no singular.** Base local: `http://localhost:8080` · Swagger: `/swagger-ui.html` (lista todos os endpoints e schemas).

### Recursos e rotas

| Recurso | Métodos | Rota base | Observações |
|---|---|---|---|
| Cooperativa | `GET` `POST` `PUT` `DELETE` | `/cooperativa` `/cooperativa/{id}` | HATEOAS; `cnpj` único; delete bloqueado com produtores |
| Produtor | `GET` `POST` `PUT` `DELETE` | `/produtor` `/produtor/{id}` | filtro `?cooperativaId=`; `cpf` único |
| Cultura | `GET` `POST` `PUT` `DELETE` | `/cultura` `/cultura/{id}` | catálogo + limiares; `nome` único |
| Talhão | `GET` `POST` `PUT` `DELETE` | `/talhao` `/talhao/{id}` | filtro `?produtorId=`; `centro` + `pontos` (polígono) |
| Safra | `GET` `POST` `PUT` `DELETE` | `/safra` `/safra/{id}` | filtro `?talhaoId=`; `statusSafra` |
| Dispositivo | `GET` `POST` `PUT` `DELETE` | `/dispositivo` `/dispositivo/{id}` | 1 por talhão; delete bloqueado com safra ATIVA |
| Leitura | `GET` `POST` | `/leitura?talhaoId={id}` `/leitura` | ingestão ESP32 (DTO simples) |
| Previsão | `GET` | `/previsao?talhaoId={id}` | histórico coletado da Open-Meteo |

### Exemplos de payload (POST)

**`POST /cooperativa`** (endereço em campos planos)
```json
{ "nome": "Cooperativa Sorriso", "cnpj": "12345678000199", "telefone": "6699990000",
  "email": "contato@coop.com", "logradouro": "Av. Brasil", "numero": "1000",
  "bairro": "Centro", "cidade": "Sorriso", "cep": "78890000", "uf": "MT" }
```

**`POST /produtor`** (`cooperativaId` obrigatório; mesmos campos de endereço)
```json
{ "cooperativaId": 1, "nome": "João da Silva", "telefone": "6698887777", "cpf": "12345678901",
  "nomePropriedade": "Fazenda Boa Vista", "caf": "CAF-001", "logradouro": "Rod. MT-242, km 10",
  "numero": "S/N", "bairro": "Zona Rural", "cidade": "Sorriso", "cep": "78890000", "uf": "MT" }
```
> `numero` é **String** (aceita `"S/N"`, `"123A"`).

**`POST /cultura`**
```json
{ "nome": "Soja", "diasAteColheita": 120, "umidadeSoloCritica": 20.0,
  "temperaturaMinCritica": 10.0, "temperaturaMaxCritica": 35.0, "chuvaMinima": 5.0 }
```

**`POST /talhao`** (`centro` necessário para a previsão; `pontos` = polígono, `ordem` única)
```json
{ "produtorId": 1, "nome": "Talhão Norte", "areaHa": 50.0,
  "centro": { "latitude": -12.5455, "longitude": -55.7215 },
  "pontos": [ { "ordem": 1, "latitude": -12.544, "longitude": -55.722 },
              { "ordem": 2, "latitude": -12.546, "longitude": -55.722 } ] }
```

**`POST /safra`**
```json
{ "talhaoId": 1, "culturaId": 1, "dataPlantio": "2026-03-01",
  "dataPrevistaColheita": "2026-07-01", "statusSafra": "ATIVA" }
```

**`POST /dispositivo`**
```json
{ "codigoDispositivo": "ESP-001", "talhaoId": 1, "ativo": true }
```

**`POST /leitura`** (ESP32 — faixas físicas validadas: temp −50..60, umidades 0..100, radiação 0..1500)
```json
{ "codigoDispositivo": "ESP-001", "dataHora": "2026-06-05T14:30:00",
  "temperatura": 28.5, "umidadeAr": 65.0, "umidadeSolo": 30.0, "radiacaoSolar": 800.0 }
```

### Respostas de erro (`ErrorResponse` padronizado)

| Situação | Exemplo | Status |
|---|---|---|
| Campo obrigatório ausente / fora de faixa | `POST /leitura` com `temperatura: 99` | `400` (com `fieldErrors`) |
| Recurso inexistente | `GET /produtor/999999` | `404` |
| `cnpj`/`cpf`/código duplicado | `POST /cooperativa` com CNPJ repetido | `409` |
| Dispositivo inativo na leitura | `POST /leitura` de dispositivo `ativo:false` | `409` |
| Delete com dependentes / safra ATIVA | `DELETE /dispositivo/{id}` com safra ativa | `409` |

> Roteiro de teste manual passo a passo: [`docs/testes-endpoints.md`](docs/testes-endpoints.md). Collection Postman: [`docs/postman/`](docs/postman).

---

## Estrutura do Projeto

```
api-java/
├── src/main/java/com/safracerta/api/
│   ├── client/        # Integrações externas (client/openmeteo/) — IA (Gemini) entra no PRD 04
│   ├── config/        # OpenApiConfig (Swagger)
│   ├── controller/    # Um controller por recurso (+ assemblers HATEOAS)
│   ├── dto/           # Records XxxRequest / XxxResponse
│   ├── entity/        # 11 entidades + RegistroClimatico (abstrata) + embeddables
│   │   └── enums/     # NivelRisco, StatusSafra
│   ├── exception/     # NotFoundException, ConflictException
│   ├── handler/       # GlobalExceptionHandler (400/404/409), ErrorResponse
│   ├── repository/    # Um JpaRepository por raiz de agregado
│   └── service/       # CRUD services (Motor de Risco + IA no PRD 04)
├── src/main/resources/
│   └── application.yml
├── .env.example
├── Dockerfile
└── README.md
```

---

## Modelo de Dados

### Entidades — 11 tabelas + 1 superclasse abstrata

| Entidade | Tabela | Relacionamentos |
|---|---|---|
| `Cooperativa` | `COOPERATIVA` | 1:N → Produtor; endereço (logradouro, numero, bairro, cidade, cep, uf) |
| `Produtor` | `PRODUTOR` | N:1 → Cooperativa; 1:N → Talhao; mesmos campos de endereço |
| `Cultura` | `CULTURA` | catálogo; 1:N → SafraTalhao |
| `Dispositivo` | `DISPOSITIVO` | N:1 → Talhao; `codigoDispositivo` unique; `ativo` |
| `Talhao` | `TALHAO` | N:1 → Produtor; `[Coordenada centro]`; 1:N → TalhaoPonto; 1:N → Dispositivo |
| `TalhaoPonto` | `TALHAO_PONTO` | N:1 → Talhao; `ordem`; unique `(talhao_id, ordem)`; `[Coordenada]` |
| `SafraTalhao` | `SAFRA_TALHAO` | N:1 → Talhao; N:1 → Cultura; 1:N → AnaliseTalhao |
| `RegistroClimatico` | — (abstrata) | base `TABLE_PER_CLASS` (sem tabela); N:1 → Talhao; `dataHora` + grandezas |
| `LeituraSensor` | `LEITURA_SENSOR` | herda `RegistroClimatico`; N:1 → Dispositivo |
| `PrevisaoClimatica` | `PREVISAO_CLIMATICA` | herda `RegistroClimatico`; + `dataHoraPrevista`, `chuva`, `temperaturaMin`, `temperaturaMax` |
| `AnaliseTalhao` | `ANALISE_TALHAO` | N:1 → SafraTalhao; snapshot `[Medicao]` + `[Previsao]` |
| `Alerta` | `ALERTA` | 1:1 → AnaliseTalhao (unique); criado só quando `NivelRisco ≠ NORMAL` |

### Embeddables — 3 classes `@Embeddable` (servem só `AnaliseTalhao`)

| Embeddable | Campos | Usado em |
|---|---|---|
| `Coordenada` | `latitude`, `longitude` | `Talhao`, `TalhaoPonto` |
| `Medicao` | `temperatura`, `umidadeAr`, `radiacaoSolar`, `umidadeSolo?` | `AnaliseTalhao` (snapshot) |
| `Previsao` | `chuva`, `umidadeAr`, `temperatura`, `temperaturaMin`, `temperaturaMax`, `radiacaoSolar`, `umidadeSolo` | `AnaliseTalhao` (snapshot, `@AttributeOverrides`) |

> A herança climática (`RegistroClimatico` → `LeituraSensor`/`PrevisaoClimatica`) usa `TABLE_PER_CLASS`: cada subclasse tem sua tabela completa, sem tabela base. Detalhe na **ADR 01**.

### Enums

| Enum | Valores |
|---|---|
| `NivelRisco` | `NORMAL`, `BAIXO`, `MEDIO`, `ALTO` |
| `StatusSafra` | `ATIVA`, `COLHIDA`, `PERDIDA` |

---

## Arquitetura do Motor de Risco

> ⚠️ **Planejado — PRD 04.** Ainda **não** implementado. Hoje o `POST /leitura` apenas persiste a leitura e coleta a previsão; o cálculo de risco e a IA entram na próxima fase. Diagrama abaixo é o desenho-alvo.

```
POST /leitura  (ESP32)
      │
      ▼
LeituraSensorService ─salva LeituraSensor────────────────────┐
      │                                                      │
      ▼                                                      │
MotorDeRiscoService                                    LEITURA_SENSOR
      │
      ├── busca SafraTalhao ativa do talhão
      ├── busca última LeituraSensor     ─── dados atuais
      ├── busca última PrevisaoClimatica ── dados previstos
      │
      ▼
RiscoCalculator (determinístico)
      │   compara Medicao + Previsao com limiares da Cultura
      ▼
NivelRisco (NORMAL | BAIXO | MEDIO | ALTO)
      │
      ▼
IaService ──── monta prompt ──── Gemini 2.0 Flash
      │                               │
      │              diagnostico + recomendacao (texto)
      │                               │
      └───────────────────────────────┘
                    │
                    ▼
             AnaliseTalhao  (snapshot: Medicao + Previsao + NivelRisco + IA)
                    │
          NivelRisco != NORMAL?
                    │
                    ▼
                 Alerta  (marcador 1:1 com AnaliseTalhao)
```

**Separação de responsabilidades:** o cálculo determinístico decide o nível (auditável, sem IA). O Gemini só descreve — não inventa números nem decide o alerta.

---

## Por que `AnaliseTalhao` é snapshot e não FK para leituras?

Guardar FKs para `LeituraSensor` e `PrevisaoClimatica` criaria acoplamento temporal: se uma leitura fosse deletada, o histórico de saúde ficaria incompleto. O snapshot copia os valores no momento da análise — o histórico de `AnaliseTalhao` é autossuficiente e não depende de leituras antigas continuarem existindo.

---

## Como Executar

### Pré-requisitos

- Docker e Docker Compose
- Chave Gemini API ([Google AI Studio](https://aistudio.google.com)) — opcional: sem ela a IA retorna `null`, o motor continua funcionando

### Configuração

```bash
cp .env.example .env
```

```env
ORACLE_ROOT_PASSWORD=SuaSenhaRoot
ORACLE_APP_USER=safra
ORACLE_APP_PASSWORD=SuaSenha
ORACLE_URL=jdbc:oracle:thin:@oracle-xe:1521/XEPDB1
GEMINI_API_KEY=sua_chave_aqui
```

### Subindo com Docker

```bash
docker compose up -d
docker compose logs -f oracle-xe   # aguardar healthy (~5 min)
```

### Rodando localmente (sem Docker)

```bash
# Requer Oracle rodando e variáveis no ambiente
mvn spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Tecnologias Utilizadas

- **Java 21** / Spring Boot 3.3.5
- **Spring AI** — Google GenAI (Gemini 2.0 Flash) — _desabilitado até o PRD 04_
- **Spring Data JPA + Hibernate** — 10 entidades Oracle
- **Spring HATEOAS** — hypermedia nos responses
- **Oracle XE 21** (Docker `gvenzl/oracle-xe:21-slim`)
- **Springdoc OpenAPI 2.6.0** (Swagger UI)
- **Bean Validation** (Jakarta)
- **Lombok**

---

## Links

| | |
|---|---|
| Deploy (nuvem) | `em breve` |
| Swagger (produção) | `em breve` |
| Vídeo de apresentação | `em breve` |
| Vídeo pitch | `em breve` |
