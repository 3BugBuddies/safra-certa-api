# Brief: Coerência API × consumo do front (preparação PRD 04)

**Status:** Pronto pro PRD 04
**Data:** 2026-06-06
**Modo:** entender-sistema / prep-feature (alimenta o PRD 04)
**Fonte do esboço:** Miro board "SafraCerta" (`uXjVHLn0kVM=`), painéis 🟢 PRODUTOR e 🟠 COOPERATIVA + cluster LOGIN.

## Ideia em uma frase
Antes de abrir o PRD 04 (Motor de Risco + IA), confrontar o esboço de consumo do front com a API real (PRD 02/03) para decidir: o que sai do esboço, o que falta, e onde mexer em **payload de leitura / relacionamento de entidade / validação** para o front consumir bem.

## Eixos escolhidos para esta discovery
1. Mapa de telas → rotas (achar o que sobra e o que falta).
2. Payload fino vs join (cortar round-trips no mobile).
3. Fechar as models: relacionamentos faltantes, validations e campos finais.

---

## O que já existe (citado em código)

**Rotas vivas** (`controller/*.java`):
- `/cooperativa`, `/produtor`, `/cultura`, `/talhao`, `/safra`, `/dispositivo` → CRUD completo.
- `/leitura` (POST + GET `?talhaoId`), `/previsao` (GET `?talhaoId`).
- Filtros por query param: `GET /produtor?cooperativaId=`, `GET /talhao?produtorId=`, `GET /safra?talhaoId=`.

**Entidades** (`entity/*.java`): 11 + `RegistroClimatico` abstrata + embeddables. `AnaliseTalhao` e `Alerta` **já modeladas**, mas sem service/controller (vêm no PRD 04).

**Padrão de payload hoje — raso/flat:**
- `TalhaoResponse` (`TalhaoResponse.java:7`): `id, produtorId, nome, areaHa, centro, pontos[]`. Polígono **já vem** em `pontos`.
- `SafraTalhaoResponse` (`SafraTalhaoResponse.java:8`): só `culturaId` (sem nome da cultura).
- `ProdutorResponse`: flat, só `cooperativaId`.
- Todos os Response devolvem **FK id**, nunca objeto aninhado.

**Padrão de relacionamento — único `@OneToMany` é `Talhao.pontos`** (`Talhao.java:34`). Todo o resto é `@ManyToOne` filho→pai. Não há `Cooperativa.produtores`, `Produtor.talhoes`, `Talhao.safras`, `SafraTalhao.analises`. Navegação top-down é feita por **query param no controller**, não por coleção na entidade.

---

## Eixo 1 — Mapa de telas → rotas (reconstruído dos clusters de anotação)

### 🔐 LOGIN (cluster à esquerda)
- "LOGIN — sem API — auth": **fora do escopo backend** (sem auth nesta rodada). ✅
- Clica → `GET /produtor/{id}` ou `GET /cooperativa/{id}` com **ID hardcoded** = simula sessão.

### 🟢 PRODUTOR (app mobile, 5 telas)
| Tela (aprox.) | Rotas anotadas | Estado |
|---|---|---|
| Home/talhão | `GET /produtor/{id}`, `GET /talhao/{id}` | ✅ existe (payload raso) |
| Previsão | `GET /PREVISAO` | ✅ é `/previsao?talhaoId=` |
| Lista de safras | `GET(Lista) /safra` | ✅ `/safra?talhaoId=` |
| Análise/risco | `GET /ANALISETALHAO` | ❌ **PRD 04** |
| Detalhe talhão | `GET /talhao/{id}` "Ajustar payload"; `GET /talhao/{id}/MAPA` "Ajustar payload"; "Fazer só 1 talhão por vez" | ⚠️ ver Eixo 2 |
| (marcação) | "Mesma tela da Cooperativa" | reuso |

### 🟠 COOPERATIVA (dashboard web, operador)
| Bloco | Rotas anotadas | Estado |
|---|---|---|
| Cooperativa | `GET /cooperativa/{id}`; `GET /COOPERATIVA (A DEFINIR)`; "API ?" | ⚠️ intenção difusa |
| Produtores | `GET ALL /produtor`, `GET /produtor/{id}`, `POST /produtor`, `PUT /produtor/{id}` | ✅ |
| Talhões | `/talhao`, `POST /talhao`, `GET /talhao/{id}`, "Add opção criar mais talhão" | ✅ |
| Safras | `POST /safra`, "add campo data de plantio" | ✅ (`dataPlantio` já existe) |
| Culturas | `GET ALL /cultura`, `GET /cultura/{id}`, "Dias?" | ✅ |
| Análise | `GET /ANALISETALHAO` (×3), "Gerado por IA" (diagnóstico/recomendação), "Nível de Risco — Fazer DE PARA" | ❌ **PRD 04** |
| Ação | "Adicionar botão que pede medição esp32 ou timer (a definir)" | ⚠️ gatilho do motor |

---

## Eixo 2 — Payload fino vs join

**Problema:** tela de talhão (mobile e dashboard) precisa de dados de 3-4 agregados, mas cada Response é raso. Hoje o front faria:
`GET /talhao/{id}` → `GET /safra?talhaoId=` → `GET /cultura/{id}` → `GET /analise?...` = 4 chamadas encadeadas só pra uma tela. É o que o esboço chama de **"Ajustar payload"**.

**Opções:**
- **(a) Enriquecer DTO de leitura** — `TalhaoResponse` ganha `produtorNome`, `safraAtiva { culturaNome, statusSafra }`, `ultimaAnalise { nivelRisco, dataHora }`. 1 chamada resolve a tela.
- **(b) Endpoint agregado dedicado** — ex. `GET /talhao/{id}/dashboard` com o pacote pronto, mantendo `TalhaoResponse` cru intacto pro CRUD.
- **(c) Manter raso** — front orquestra N chamadas. Mais simples no back, pior no mobile.

⚠️ **Regra do CLAUDE.md global:** não explodir em `Resumo`+`Detalhe`+`Result`. Preferir **um `XxxResponse` com campos nullable** onde o endpoint não popula.

**`/talhao/{id}/MAPA` provavelmente é redundante** — o polígono já vem em `TalhaoResponse.pontos` (`TalhaoResponse.java:13`). Confirmar com quem desenhou.

---

## Eixo 3 — Fechar as models

**Relacionamentos (o ponto do "não tem lista de produtores em cooperativa"):**
- A navegação top-down hoje **só existe por query param**, não por coleção. `GET /produtor?cooperativaId=` já entrega a lista de produtores da cooperativa — sem precisar de `@OneToMany` na entidade.
- **Decisão de design:** manter unidirecional + query param (evita lazy-loading/serialização recursiva) **ou** adicionar coleções/`count` no Response (ex. `CooperativaResponse` com `qtdProdutores`). Recomendação inicial: **não** botar `@OneToMany` só pra leitura — resolver com campo derivado no Response ou endpoint de listagem.

**Validations — hoje esparsas, gap real:**
- `CulturaRequest` (`CulturaRequest.java`): thresholds (`umidadeSoloCritica`, `temperaturaMin/MaxCritica`, `chuvaMinima`) **sem nenhuma validação** — aceita negativo/nulo. Candidatos: `@NotNull`, `@PositiveOrZero`, faixas.
- `ProdutorRequest`: só `cpf` tem `@Size`; `telefone`, `cep`, `uf`, `email`/endereço sem formato. Candidatos: `@Pattern` (UF 2 letras, CEP, CPF dígitos), `@Email`.
- `Cooperativa`/`Produtor` têm `cnpj`/`cpf` unique no banco, mas sem validação de formato no request.

**Campos finais a confirmar:**
- `SafraTalhaoResponse` expõe `culturaId` mas não `culturaNome` → telas de safra mostram id cru. (liga ao Eixo 2.)
- "DE-PARA do Nível de Risco" (enum → label/cor): decidir se é responsabilidade do front ou se a `AnaliseResponse` expõe um `label`.

---

## Perguntas ainda abertas (para o PRD 04 / próxima conversa)
1. **Contrato do `AnaliseTalhao`** (núcleo PRD 04): filtra por `safraTalhaoId` ou `talhaoId`? Última análise vs histórico? Campos do payload (nível, label, diagnóstico, recomendação, snapshot medição/previsão)?
2. **Payload de leitura**: vamos pela opção (a), (b) ou (c) do Eixo 2? Quais telas justificam enriquecimento?
3. **Gatilho do motor**: "botão que pede medição" → `POST /leitura` dispara o motor de forma síncrona, ou job/timer separado?
4. **Validations**: incluir endurecimento de validação no escopo do PRD 04 ou tratar como PRD próprio menor?
5. **`GET /COOPERATIVA (A DEFINIR)` e "API ?"**: o que o esboço quer aqui? (seletor de cooperativa? já coberto por `GET /cooperativa`.)

## Sai do esboço (a corrigir no Miro)
- `GET /talhao/{id}/MAPA` → redundante (polígono já em `pontos`).
- `LOGIN com auth` → sem backend; vira GET com id hardcoded.
- "add campo data de plantio" → já existe em `SafraTalhao.dataPlantio`.

## Pronto pro PRD
- **Problema:** o front precisa de telas de risco/diagnóstico que dependem do Motor + IA (inexistentes) e de payloads de leitura mais ricos.
- **Personas:** produtor (mobile) e operador da cooperativa (dashboard).
- **Critérios candidatos:** `GET` de análise por talhão/safra; enriquecimento de Response de talhão; endurecimento de validações; mapa de nível de risco.

---

## Convergência — decisões (2026-06-06)

**Mockups confirmados** (3 telas, definem os contratos de leitura):

| Tela | Contrato candidato | Dados cruzados |
|---|---|---|
| **Painel da Cooperativa** (Image 1) — o "COOPERATIVA a definir" | agregado por cooperativa | `count(Produtor)`, `sum(Talhao.areaHa)`, nº alertas = `count` talhões com `nivelRisco != NORMAL` (última análise), distribuição por `nivelRisco`, mapa de calor (`Talhao.centro` + nível), top produtores em risco |
| **Mapa de Talhões** (Image 2) | `Talhao` enriquecido (substitui a ideia do `/talhao/{id}/mapa`) | nome+área (`Talhao`), cultura (`SafraTalhao`), umidade+temp (`Medicao` da última `AnaliseTalhao`), `nivelRisco` p/ legenda, polígono (`pontos`) |
| **Produtores Cooperados** (Image 3) | `Produtor` enriquecido / lista por cooperativa | nome/cidade/telefone (`Produtor`), `sum` área, `count` talhões, nº alertas = `count` talhões em risco, pior `nivelRisco` (nível agregado) |

**Decisões fechadas:**
1. **Relacionamento bidirecional** — adicionar `@OneToMany` (ex. `Cooperativa.produtores`, `Produtor.talhoes`, `Talhao.safras`, `SafraTalhao.analises`) e navegar no service. _Escolha do Gabriel sobre a alternativa "read-models via query"._
   - **Mitigações obrigatórias na spec:** (a) serializar **só DTO** via `from()` — nunca a entidade (evita ciclo); (b) `@EntityGraph`/join fetch nas listas (Images 1 e 3) p/ evitar N+1; (c) montar DTO dentro da transação do service (lazy).
2. **Enriquecer os DTOs de leitura** — sim, confirmado. `/talhao/{id}/mapa` vira `Talhao` enriquecido (já traz `pontos` + ganha `nivelRisco`/última medição).
3. **Tudo no PRD 04** — escopo único: Motor de Risco + IA (Gemini) + `AnaliseTalhao` (service/controller) + read-models/agregações das 3 telas + endurecimento de validations. _Atenção: escopo grande p/ 3 dias (entrega 09/06). Sequenciar tasks com o motor+IA (item de nota) primeiro._
4. **Remover a entidade `Alerta`** — era marcador puro (`id + analiseTalhao` 1:1), redundante com `AnaliseTalhao.nivelRisco`. "Alerta" passa a ser **conceito derivado**: talhão cuja última análise tem `nivelRisco != NORMAL`. Contagens via query. Modelo vai de 11 → 10 tabelas (PL/SQL mín. 6, folgado; `@OneToOne` não é item de nota). Sem migration (`ddl-auto: create`). _Atualizar depois: CLAUDE.md raiz + spec do modelo + ADR, que listavam `Alerta` como decisão fechada._

**Decisão pendente (não trava o PRD):**
- **Gatilho da análise:** timer no ESP32 vs botão no front que dispara `POST /leitura` ao acessar a tela. Especificar a análise sendo disparada no fluxo do `POST /leitura`; plugar o gatilho de UI depois.

**Sai do esboço:** `/talhao/{id}/mapa` como rota nova (vira `Talhao` enriquecido); `LOGIN` com auth (id hardcoded); "add campo data de plantio" (já existe).
