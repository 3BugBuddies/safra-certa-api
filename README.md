# safracerta-api — Global Solution 2026/1 | Java Advanced

API REST de monitoramento inteligente de talhões agrícolas, em Spring Boot + Spring AI — Global Solution de **Java Advanced (2TDS)**, FIAP 2026/1.

O serviço recebe leituras de sensores ESP32 e previsões do Open-Meteo, executa um **motor de risco determinístico** a cada nova leitura e aciona o **Gemini** para gerar diagnóstico e recomendação em linguagem natural. O nível de risco fica registrado na análise do talhão e é consumido pelo app (áreas de **produtor** e **cooperativa**).

---

## Integrantes do Grupo

| Nome | RM |
|------|----|
| Felipe Yuiti Ishii | 565339 |
| Gabriel Nogueira Peixoto | 563925 |
| Giovanna Neri dos Santos | 566154 |
| Mariana Inoue | 565834 |

> Representante (RM no nome do container DevOps): **565339**.

---

## Stack

- **Java 21** · **Spring Boot 3.4.5**
- **Spring AI 1.1.6** — Gemini 2.5 Flash via starter `spring-ai-starter-model-openai` (endpoint OpenAI-compatible)
- **Spring Data JPA + Hibernate** — 10 tabelas + superclasse abstrata (herança `TABLE_PER_CLASS`)
- **Spring HATEOAS** — hypermedia nos responses
- **Oracle** (JDBC `ojdbc11`) · **Bean Validation** · **Lombok** · **DevTools**
- **Springdoc OpenAPI 2.8.8** — Swagger UI

> Datasource e IA em `src/main/resources/application.properties` (`ORACLE_*`, `GEMINI_API_KEY`). `ddl-auto: update` em dev. Sem autenticação nesta rodada.

---

## Cobertura da API (estado atual)

- **CRUD** (REST + HATEOAS): Cooperativa, Produtor, Cultura, Talhão (+polígono), Safra, Dispositivo.
- **Ingestão:** `POST /leitura` (ESP32) + coleta de previsão (Open-Meteo) no fluxo, com throttle.
- **Motor de Risco + IA:** a cada leitura, classifica o `NivelRisco` (determinístico) e persiste `AnaliseTalhao`; o Gemini preenche `diagnostico`/`recomendacao` (com fallback se faltar chave/serviço).
- **Consulta de análise** e **visões agregadas da cooperativa** (painel, cards de produtor, situação de talhão).
- **Seed automático** de 14 culturas embasadas no boot.

> **Rotas no singular.** Base local: `http://localhost:8080` · Swagger: `/swagger-ui.html`.
> **Documentação de API (Postman):** [`postman/SafraCerta.postman_collection.json`](docs/postman/SafraCerta.postman_collection.json) — todos os endpoints, params e exemplos.

### Recursos e rotas

| Recurso | Métodos | Rotas | Observações |
|---|---|---|---|
| Cooperativa | `GET POST PUT DELETE` | `/cooperativa` · `/cooperativa/{id}` | `cnpj` único; **delete em cascata** (produtores e tudo abaixo) |
| → Painel | `GET` | `/cooperativa/{id}/painel` | contadores + distribuição por nível (telas da cooperativa) |
| → Produtores (cards) | `GET` | `/cooperativa/{id}/produtores` | área total, nº talhões, nº em risco, nível agregado |
| Produtor | `GET POST PUT DELETE` | `/produtor` · `/produtor/{id}` | filtro `?cooperativaId=`; `cpf` único |
| → Talhões (situação) | `GET` | `/produtor/{id}/talhoes` | mapa de talhões: cultura, medição, nível, polígono |
| Cultura | `GET POST PUT DELETE` | `/cultura` · `/cultura/{id}` | catálogo + limiares; `nome` único |
| Talhão | `GET POST PUT DELETE` | `/talhao` · `/talhao/{id}` | filtro `?produtorId=`; `centro` + `pontos` (polígono) |
| → Situação | `GET` | `/talhao/{id}/situacao` | situação atual de um talhão |
| Safra | `GET POST PUT DELETE` | `/safra` · `/safra/{id}` | filtro `?talhaoId=`; `statusSafra` |
| Dispositivo | `GET POST PUT DELETE` | `/dispositivo` · `/dispositivo/{id}` | 1 por talhão; **delete em cascata** das leituras |
| Leitura | `GET POST` | `/leitura?talhaoId={id}` · `/leitura` | ingestão ESP32 (dispara motor + IA) |
| Previsão | `GET` | `/previsao?talhaoId={id}` | histórico coletado da Open-Meteo |
| Análise | `GET` | `/analise?talhaoId={id}` · `/analise/ultima?talhaoId={id}` · `/analise/{id}` | histórico, última e detalhe |

### Exemplos de payload (POST)

**`POST /cooperativa`**
```json
{ "nome": "Cooperativa Boa Esperança", "cnpj": "12345678000190", "telefone": "3433334444",
  "email": "contato@coop.com.br", "logradouro": "Rod. BR-050", "numero": "km 12",
  "bairro": "Zona Rural", "cidade": "Uberaba", "cep": "38000000", "uf": "MG" }
```

**`POST /produtor`** (`cooperativaId` obrigatório; `numero` é String — aceita `"S/N"`)
```json
{ "cooperativaId": 1, "nome": "João Silva", "telefone": "34999991111", "cpf": "12345678900",
  "dataNascimento": "1980-05-12", "nomePropriedade": "Fazenda Boa Esperança", "caf": "CAF-001",
  "logradouro": "Rod. BR-050", "numero": "km 12", "bairro": "Zona Rural", "cidade": "Uberaba",
  "cep": "38000000", "uf": "MG" }
```

**`POST /cultura`**
```json
{ "nome": "Soja", "diasAteColheita": 160, "umidadeSoloCritica": 30.0,
  "temperaturaMinCritica": 10.0, "temperaturaMaxCritica": 40.0, "chuvaMinima": 5.0 }
```

**`POST /talhao`** (`centro` alimenta a previsão; `pontos` = polígono, `ordem` única)
```json
{ "produtorId": 1, "nome": "Talhão Norte", "areaHa": 45.0,
  "centro": { "latitude": -19.75, "longitude": -47.93 },
  "pontos": [ { "ordem": 0, "latitude": -19.749, "longitude": -47.931 },
              { "ordem": 1, "latitude": -19.751, "longitude": -47.929 } ] }
```

**`POST /safra`**
```json
{ "talhaoId": 1, "culturaId": 1, "dataPlantio": "2026-01-15",
  "dataPrevistaColheita": "2026-06-24", "statusSafra": "ATIVA" }
```

**`POST /dispositivo`**
```json
{ "codigoDispositivo": "ESP32-001", "talhaoId": 1, "ativo": true }
```

**`POST /leitura`** (ESP32 — faixas: temp −50..60, umidades 0..100, radiação 0..1500)
```json
{ "codigoDispositivo": "ESP32-001", "dataHora": "2026-06-08T09:41:00",
  "temperatura": 26.0, "umidadeAr": 72.0, "umidadeSolo": 31.0, "radiacaoSolar": 456.0 }
```

### Respostas de erro (`ErrorResponse` padronizado)

| Situação | Exemplo | Status |
|---|---|---|
| Campo obrigatório ausente / fora de faixa | `POST /leitura` com `temperatura: 99` | `400` (com `fieldErrors`) |
| Recurso inexistente | `GET /produtor/999999` | `404` |
| `cnpj`/`cpf`/código duplicado | `POST /cooperativa` com CNPJ repetido | `409` |
| Dispositivo inativo na leitura | `POST /leitura` de dispositivo `ativo:false` | `409` |

---

## Modelo de Dados

### Entidades — 10 tabelas + 1 superclasse abstrata

| Entidade | Tabela | Relacionamentos |
|---|---|---|
| `Cooperativa` | `COOPERATIVA` | 1:N → Produtor; endereço plano |
| `Produtor` | `PRODUTOR` | N:1 → Cooperativa; 1:N → Talhao |
| `Cultura` | `CULTURA` | catálogo; 1:N → SafraTalhao; limiares de risco |
| `Dispositivo` | `DISPOSITIVO` | N:1 → Talhao; `codigoDispositivo` unique; `ativo` |
| `Talhao` | `TALHAO` | N:1 → Produtor; `[Coordenada centro]`; 1:N → TalhaoPonto/Dispositivo/SafraTalhao |
| `TalhaoPonto` | `TALHAO_PONTO` | N:1 → Talhao; `ordem`; unique `(talhao_id, ordem)`; `[Coordenada]` |
| `SafraTalhao` | `SAFRA_TALHAO` | N:1 → Talhao/Cultura; 1:N → AnaliseTalhao; `statusSafra` |
| `RegistroClimatico` | — (abstrata) | base `TABLE_PER_CLASS`; N:1 → Talhao; `dataHora` + grandezas |
| `LeituraSensor` | `LEITURA_SENSOR` | herda `RegistroClimatico`; N:1 → Dispositivo |
| `PrevisaoClimatica` | `PREVISAO_CLIMATICA` | herda `RegistroClimatico`; + `chuva`, `temperaturaMin/Max` (D+1) |
| `AnaliseTalhao` | `ANALISE_TALHAO` | N:1 → SafraTalhao; snapshot `[Medicao]` + `[Previsao]` + `nivelRisco` + diagnóstico/recomendação |

> **Não há entidade `Alerta`** — o nível de risco vive em `AnaliseTalhao`; "alerta/crítico" é rótulo de UI derivado do nível.

### Embeddables — 3 `@Embeddable`

| Embeddable | Campos | Usado em |
|---|---|---|
| `Coordenada` | `latitude`, `longitude` | `Talhao`, `TalhaoPonto` |
| `Medicao` | `temperatura`, `umidadeAr`, `radiacaoSolar`, `umidadeSolo` | `AnaliseTalhao` (snapshot) |
| `Previsao` | `chuva`, `umidadeAr`, `temperatura`, `temperaturaMin/Max`, `radiacaoSolar`, `umidadeSolo` | `AnaliseTalhao` (snapshot, `@AttributeOverrides`) |

> Herança `RegistroClimatico` → `LeituraSensor`/`PrevisaoClimatica` em `TABLE_PER_CLASS` (cada subclasse com tabela completa, sem tabela base). Ver **ADR 01**.

### Enums

| Enum | Valores |
|---|---|
| `NivelRisco` | `SAUDAVEL`, `ATENCAO`, `ALERTA`, `CRITICO` |
| `StatusSafra` | `ATIVA`, `COLHIDA`, `PERDIDA` |

> A API devolve o `nivelRisco` **cru** (4 níveis); o front renderiza rótulo/cor. "Em risco" = nível ∈ {ALERTA, CRITICO}.

---

## Motor de Risco + IA

```
POST /leitura  (ESP32)
      │
      ▼
LeituraSensorService ── salva LeituraSensor + coleta Previsão (throttle)
      │
      ▼
MotorDeRiscoService  (só age se o talhão tem safra ATIVA)
      ├── conta 4 fatores vs. limiares da Cultura:
      │     solo seco · geada (tempMin) · calor (tempMax) · déficit hídrico (chuva)
      ▼
NivelRisco  ←  contagem: 0=SAUDAVEL · 1=ATENCAO · 2=ALERTA · 3+=CRITICO
      │
      ▼
DiagnosticoService ── prompt ── Gemini 2.5 Flash   (opcional, com fallback)
      │                              │
      │            diagnostico + recomendacao (texto)
      ▼
AnaliseTalhao  (snapshot: Medicao + Previsao + nivelRisco + diagnóstico/recomendação)
```

**Separação de responsabilidades:** o cálculo determinístico decide o nível (auditável, sem IA). O Gemini **só descreve** — não inventa números nem decide o nível. Se a IA falhar/sem chave, a análise é salva sem texto (fallback).

**`AnaliseTalhao` é snapshot, não FK para leituras:** copia os valores no momento da análise; o histórico é autossuficiente e não depende de leituras antigas continuarem existindo.

---

## Como Executar

### Pré-requisitos
- Docker + Docker Compose
- Chave Gemini ([Google AI Studio](https://aistudio.google.com)) — opcional: sem ela a IA degrada para `null` e o motor segue.

### Configuração
```bash
cp .env.example .env
```
```env
ORACLE_URL=jdbc:oracle:thin:@oracle-xe:1521/XEPDB1
ORACLE_USER=rm563925
ORACLE_PASSWORD=sua_senha
GEMINI_API_KEY=sua_chave_aqui
```

### Docker
```bash
docker compose up -d
docker compose logs -f                # acompanhar Oracle + API
```

### Local (sem Docker — requer Oracle de pé)
```bash
mvn spring-boot:run
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Links

| | |
|---|---|
| Deploy (nuvem) | `em breve` |
| Swagger (produção) | `em breve` |
| Vídeo de apresentação | `em breve` |
