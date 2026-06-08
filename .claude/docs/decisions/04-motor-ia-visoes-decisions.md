# Decisões — PRD 04 (Motor de Risco, IA, Visões de Front, CORS)

**Data:** 2026-06-06 · **Origem:** `/analyze 04` (sobre PRD `.claude/docs/prds/04.md` + brief `.claude/docs/discovery/prd04-consumo-front-coerencia.md`)

---

## Decisão 1: Remover a entidade `Alerta`

**Contexto:** `Alerta` era marcador puro (`id + analiseTalhao` 1:1), redundante com `AnaliseTalhao.nivelRisco`.
**Decisão:** Remover a entidade. "Alerta" passa a ser conceito derivado — talhão cuja última análise tem `nivelRisco != NORMAL`. Contagens via query.
**Alternativas rejeitadas:** Dar lifecycle ao alerta (lido/notificado/resolvido) — descartado por não haver UI de lifecycle nos mockups e por aumentar escopo a 3 dias da entrega.
**Consequências:** Modelo 11→10 tabelas (PL/SQL mín. 6, folgado). Sem migration (`ddl-auto: create`). Perde o único `@OneToOne` (não é item de nota). Dívida: CLAUDE.md raiz + spec do modelo + ADR 01 ainda citam `Alerta` como fechado — sincronizar.
**Revisitar quando:** o produto exigir rastrear notificação/leitura do alerta pelo produtor.

## Decisão 2: Relacionamentos bidirecionais

**Contexto:** As telas precisam navegar da cooperativa → produtores → talhões → safras → análises; hoje só há `@ManyToOne` filho→pai (único `@OneToMany` é `Talhao.pontos`).
**Decisão:** Adicionar `@OneToMany` inverso onde as visões exigem (ex. `Cooperativa.produtores`, `Produtor.talhoes`, `Talhao.safras`, `SafraTalhao.analises`) e navegar no service.
**Alternativas rejeitadas:** Manter unidirecional + projeções/queries agregadas (read-models) — preferência do Gabriel pelo relacionamento explícito.
**Consequências:** Exige mitigação obrigatória: (a) serializar **só DTO**, nunca entidade (evita ciclo); (b) `@EntityGraph`/join fetch nas listagens (evita N+1); (c) montar DTO dentro da transação (lazy). Sem essas, há risco de loop de serialização e N+1.
**Revisitar quando:** N+1 ou payload pesado aparecer em profiling.

## Decisão 3: Faixa de risco por **contagem de fatores**

**Contexto:** A `Cultura` tem limites críticos únicos (umidadeSoloCritica, temperaturaMin/MaxCritica, chuvaMinima), não faixas. O motor precisa produzir 4 níveis.
**Decisão:** Contar quantos fatores cruzaram o limite → `0=NORMAL, 1=BAIXO, 2=MEDIO, 3+=ALTO`. Fatores: solo seco (`umidadeSolo` < crítica), geada (`previsao.temperaturaMin` < crítica), calor (`previsao.temperaturaMax` > crítica), déficit hídrico (`previsao.chuva` < mínima). Cada fator só conta se a cultura tiver o limite **e** o dado existir.
**Alternativas rejeitadas:** Severidade/pior fator com margem percentual (gradiente mais fiel) — descartada por simplicidade; sem mapa de calor, o gradiente fino perde urgência.
**Consequências:** Simples e explicável. Limitação aceita: um único fator extremo classifica só como BAIXO (intensidade não é considerada).
**Revisitar quando:** o time quiser refletir intensidade do desvio, não só quantidade.

## Decisão 4: DE-PARA dos 4 níveis nos 3 rótulos da UI

**Contexto:** Enum tem 4 níveis; mockups usam 3 rótulos (Saudável/Alerta/Crítico).
**Decisão:** `NORMAL`+`BAIXO` → **Saudável** · `MEDIO` → **Alerta** · `ALTO` → **Crítico**.
**Alternativas rejeitadas:** NORMAL→Saudável / BAIXO+MEDIO→Alerta / ALTO→Crítico (mais conservador); expor enum cru e mapear no front.
**Consequências:** API pode expor um campo de rótulo derivado junto ao nível. Ruído baixo (BAIXO) não alarma.
**Revisitar quando:** o produto quiser tratar BAIXO como alerta visível.

## Decisão 5: Nível agregado do produtor = **pior nível** entre seus talhões

**Contexto:** O card do produtor (Image 3) mostra um rótulo único; o produtor tem N talhões.
**Decisão:** Agregar pelo **pior** nível de risco entre os talhões do produtor (última análise de cada). Distribuição/"Status da Região" conta **talhões** por nível.
**Alternativas rejeitadas:** Média/maioria — menos adequado a um painel de alerta.
**Consequências:** Dashboard destaca o produtor pelo seu pior talhão (conservador, desejável).
**Revisitar quando:** falsos positivos por um único talhão crítico incomodarem.

## Decisão 6: IA via **Spring AI + upgrade de stack**

**Contexto:** Spring AI está desabilitado (`application.yml:14`) por incompatibilidade (projeto Boot 3.3.5; starter Gemini exige Boot 3.4+/3.5+).
**Decisão:** Reativar Spring AI subindo a stack para a **combinação comprovada do projeto `petbuddies-ai`** (mesma FIAP/Oracle): **Boot 3.4.5 · Spring AI 1.1.6 · springdoc 2.8.8**, usando o starter **`spring-ai-starter-model-openai`** apontado para o **endpoint OpenAI-compatible do Gemini** (`https://generativelanguage.googleapis.com/v1beta/openai`, modelo `gemini-2.5-flash`) — **não** o starter `google-genai`. _Escolha do Gabriel; versões confirmadas a partir do print + leitura do projeto real._
**Alternativas rejeitadas:** Chamada HTTP direta via `RestClient` (zero upgrade) — vira só fallback de último caso; starter `google-genai` — preterido pelo OpenAI-compatible, que é o que já roda no petbuddies.
**Consequências / risco:** Risco **reduzido** — não é território novo, é cópia de um combo funcionando. Sem memória JDBC (nosso caso é stateless). **Mitigação:** (a) `mvn clean compile` + smoke logo após o upgrade; (b) fallback `RestClient` se algo travar; (c) `GEMINI_API_KEY` como secret de ambiente.
**Revisitar quando:** o upgrade falhar ou consumir tempo crítico → fallback RestClient.

## Decisão 7: Payload rico via **DTO + `RepresentationModelAssemblerSupport`**, sem expor entidade

**Contexto:** Referência do ToyStore (`ToyStore-master`) devolve a **entidade** dentro do `EntityModel` — payload rico, mas a entidade lá é plana. As nossas, com os `@OneToMany` da Decisão 2, dariam ciclo de serialização.
**Decisão:** Manter **DTO** (não expor entidade), **enriquecer** os DTOs de leitura com dados aninhados, e adotar `RepresentationModelAssemblerSupport` + `_links` para recursos relacionados (self, collection, e relações: talhão→produtor/safras/análises).
**Alternativas rejeitadas:** Passar a entidade direto, estilo ToyStore — reintroduz o risco de ciclo que a Decisão 2 nos obriga a evitar.
**Consequências:** Payload rico que o Gabriel quer, sem acoplar contrato ao schema nem arriscar loop. Mais navegabilidade de HATEOAS.
**Revisitar quando:** —

## Decisão 8: Sem mapa de calor

**Contexto:** O Painel da Cooperativa (Image 1) tinha um widget de mapa de calor.
**Decisão:** Remover o mapa de calor do escopo. O painel mantém contadores (produtores, hectares, talhões em risco), distribuição por nível e lista de produtores em risco.
**Consequências:** Não há agregação espacial por município nem necessidade de plotar coordenadas no dashboard.
**Revisitar quando:** o time quiser visão geoespacial numa próxima rodada.

## Decisão 9: CORS + seed automático **apenas de `Cultura`**

**Contexto:** O front precisa de deploy provisório com algo a consumir. `ddl-auto: create` zera o banco a cada boot.
**Decisão:** Adicionar `CorsConfig` básico (libera origens p/ o front) e um seed automático **só do catálogo de `Cultura`** (re-semeado no boot, já que `ddl-auto` segue `create`). Demais dados (produtor/talhão/safra/leitura/análise) entram manualmente / via ESP.
**Alternativas rejeitadas:** Seed completo com análises (popular leituras p/ gerar risco no dashboard) — fora de escopo agora; trocar `ddl-auto` p/ `update` — não pedido.
**Consequências:** Front sobe com catálogo de culturas disponível; **dashboard fica sem risco/análise até alguém postar leituras**. Aceito conscientemente.
**Revisitar quando:** o demo precisar de dashboard populado → seed de leituras/análises.

## Decisão 10: Regras do motor (escopo e bordas)

**Contexto:** O motor roda na ingestão de leitura e depende de safra ativa + limites da cultura, ambos podendo faltar.
**Decisão:**
- Gatilho **fora de escopo** (timer/botão); o cálculo dispara no fluxo do `POST /leitura`.
- **1 safra ATIVA por talhão**: rejeitar (409) o cadastro de 2ª safra ATIVA no mesmo talhão; o motor sempre acha uma única.
- Leitura de talhão **sem safra ativa** → não gera análise (degrada silencioso).
- Limite da cultura nulo ou dado faltante → fator ignorado.
- **Sem previsão** → motor avalia só o fator de solo (geada/calor/chuva ficam de fora daquela análise).
**Consequências:** Motor robusto a dados parciais; análises só nascem quando há safra ativa.
**Revisitar quando:** regras de negócio do risco evoluírem.
