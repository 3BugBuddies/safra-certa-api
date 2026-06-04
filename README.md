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
| Spring AI Google GenAI | AI | Gemini 2.0 Flash — diagnóstico e recomendação |
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

## Avaliação Java — roteiro de endpoints

> Estes endpoints são **autocontidos**: não dependem de ESP32 físico nem de chave Gemini ativa. O avaliador precisa apenas dos containers rodando.

### Contexto dos recursos testados

O núcleo do sistema é o **Motor de Risco**: a cada leitura de sensor recebida, o motor busca a safra ativa do talhão, compara os valores medidos com os limiares da cultura plantada e calcula o `NivelRisco` (`NORMAL`, `BAIXO`, `MEDIO` ou `ALTO`). Em seguida, chama o Gemini para gerar `diagnostico` e `recomendacao` em texto livre. O resultado é gravado em `AnaliseTalhao`. Se o nível for diferente de `NORMAL`, um `Alerta` é criado como marcador.

Para que o motor funcione, é preciso antes montar a hierarquia de dados: cooperativa → produtor → talhão → cultura → safra. Depois, enviar uma leitura do sensor já dispara todo o fluxo.

### Fluxo sugerido — caminho feliz completo

| Ordem | Descrição | Método | Rota | Retorno esperado |
|---|---|---|---|---|
| 1 | Cadastra a cooperativa | `POST` | `/cooperativas` | `201` com a cooperativa criada |
| 2 | Lista cooperativas cadastradas | `GET` | `/cooperativas` | `200` com lista |
| 3 | Cadastra um produtor vinculado | `POST` | `/produtores` | `201` com o produtor |
| 4 | Cadastra uma cultura no catálogo | `POST` | `/culturas` | `201` com limiares definidos |
| 5 | Lista culturas disponíveis | `GET` | `/culturas` | `200` com catálogo |
| 6 | Cadastra um talhão do produtor | `POST` | `/talhoes` | `201` com o talhão |
| 7 | Abre uma safra (talhão + cultura) | `POST` | `/safras` | `201` com a safra ativa |
| 8 | Consulta a safra criada | `GET` | `/safras/{id}` | `200` com status `ATIVA` |
| 9 | Envia leitura do sensor ESP32 | `POST` | `/leituras` | `201` → dispara Motor de Risco |
| 10 | Consulta a saúde gerada | `GET` | `/saude-talhao` | `200` com `nivelRisco`, `diagnostico`, `recomendacao` |
| 11 | Verifica se alerta foi criado | `GET` | `/alertas` | `200` — presente se nível ≠ `NORMAL` |
| 12 | Envia previsão climática | `POST` | `/previsoes` | `201` com a previsão registrada |
| 13 | Atualiza dados da cooperativa | `PUT` | `/cooperativas/{id}` | `200` com dados atualizados |
| 14 | Atualiza dados do produtor | `PUT` | `/produtores/{id}` | `200` com dados atualizados |
| 15 | Encerra a safra | `PUT` | `/safras/{id}` com `statusSafra: COLHIDA` | `200` com status atualizado |
| 16 | Remove talhão de teste | `DELETE` | `/talhoes/{id}` | `204` sem corpo |

### Validações e respostas de erro

| Situação | Método | Rota | Retorno esperado |
|---|---|---|---|
| Cria cooperativa sem CNPJ | `POST` | `/cooperativas` com `{}` | `400` com `ErrorResponse` |
| Busca produtor inexistente | `GET` | `/produtores/999999` | `404` com `ErrorResponse` |
| Envia leitura sem `talhaoId` | `POST` | `/leituras` com `{}` | `400` com `ErrorResponse` |
| Cria safra com cultura inexistente | `POST` | `/safras` com `culturaId` inválido | `404` com `ErrorResponse` |
| CNPJ duplicado na cooperativa | `POST` | `/cooperativas` com CNPJ já cadastrado | `409` com `ErrorResponse` |

---

## Estrutura do Projeto

```
api-java/
├── src/main/java/com/safracerta/api/
│   ├── config/        # OpenApiConfig, CorsConfig, SchedulingConfig
│   ├── controller/    # Um controller por recurso
│   ├── dto/           # Records XxxRequest / XxxResponse
│   ├── entity/        # 11 entidades + RegistroClimatico (abstrata) + 3 embeddables
│   │   └── enums/     # NivelRisco, StatusSafra
│   ├── repository/    # Um JpaRepository por raiz de agregado
│   └── service/       # CRUD services + MotorDeRiscoService + IaService
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
| `Cooperativa` | `COOPERATIVA` | 1:N → Produtor; `endereco` |
| `Produtor` | `PRODUTOR` | N:1 → Cooperativa; 1:N → Talhao |
| `Cultura` | `CULTURA` | catálogo; 1:N → SafraTalhao |
| `Dispositivo` | `DISPOSITIVO` | N:1 → Talhao; `codigoDispositivo` unique; `ativo` |
| `Talhao` | `TALHAO` | N:1 → Produtor; `[Coordenada centro]`; 1:N → TalhaoPonto; 1:N → Dispositivo |
| `TalhaoPonto` | `TALHAO_PONTO` | N:1 → Talhao; `ordem`; unique `(talhao_id, ordem)`; `[Coordenada]` |
| `SafraTalhao` | `SAFRA_TALHAO` | N:1 → Talhao; N:1 → Cultura; 1:N → AnaliseTalhao |
| `RegistroClimatico` | — (abstrata) | base `TABLE_PER_CLASS` (sem tabela); N:1 → Talhao; `dataHora` + grandezas |
| `LeituraSensor` | `LEITURA_SENSOR` | herda `RegistroClimatico`; N:1 → Dispositivo |
| `PrevisaoClimatica` | `PREVISAO_CLIMATICA` | herda `RegistroClimatico`; + `dataHoraPrevista`, `chuva` |
| `AnaliseTalhao` | `ANALISE_TALHAO` | N:1 → SafraTalhao; snapshot `[Medicao]` + `[Previsao]` |
| `Alerta` | `ALERTA` | 1:1 → AnaliseTalhao (unique); criado só quando `NivelRisco ≠ NORMAL` |

### Embeddables — 3 classes `@Embeddable` (servem só `AnaliseTalhao`)

| Embeddable | Campos | Usado em |
|---|---|---|
| `Coordenada` | `latitude`, `longitude` | `Talhao`, `TalhaoPonto` |
| `Medicao` | `temperatura`, `umidadeAr`, `radiacaoSolar`, `umidadeSolo?` | `AnaliseTalhao` (snapshot) |
| `Previsao` | `chuva`, `umidadeAr`, `temperatura`, `radiacaoSolar`, `umidadeSolo` | `AnaliseTalhao` (snapshot, `@AttributeOverrides`) |

> A herança climática (`RegistroClimatico` → `LeituraSensor`/`PrevisaoClimatica`) usa `TABLE_PER_CLASS`: cada subclasse tem sua tabela completa, sem tabela base. Detalhe na **ADR 01**.

### Enums

| Enum | Valores |
|---|---|
| `NivelRisco` | `NORMAL`, `BAIXO`, `MEDIO`, `ALTO` |
| `StatusSafra` | `ATIVA`, `COLHIDA`, `PERDIDA` |

---

## Arquitetura do Motor de Risco

```
POST /leituras  (ESP32)
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
- **Spring AI 1.0.0** — Google GenAI (Gemini 2.0 Flash via AI Studio)
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
