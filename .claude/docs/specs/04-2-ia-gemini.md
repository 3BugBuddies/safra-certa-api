# Spec 04-2: IA Gemini — Diagnóstico e Recomendação

**Status:** Aprovado (2026-06-07) · **Frente:** 2 · **Depende de:** contrato da Frente 1 · **Base:** decisão 6.

## Objetivo
Reativar a IA para preencher `diagnostico` e `recomendacao` da `AnaliseTalhao`, via Spring AI (Gemini através do endpoint **OpenAI-compatible**), com **fallback** (se a IA falhar, a análise persiste sem os textos).

> ✅ **Stack comprovada.** Copiada do projeto `petbuddies-ai` (mesma org/FIAP, mesma base Oracle), que roda essa combinação: **Boot 3.4.5 · Spring AI 1.1.6 · springdoc 2.8.8 · Gemini 2.5 Flash via OpenAI-compatible**. Isso reduz muito o risco do upgrade — não é território novo. Ainda assim: upgrade → `mvn clean compile` + smoke → só então prosseguir. Fallback de último caso: `RestClient` direto.

## Escopo
- Upgrade de stack para a combinação comprovada.
- `IaService` (stateless) que gera diagnóstico/recomendação a partir do contexto da análise.
- Integração no seam deixado pela Frente 1 (`MotorDeRiscoService`).
- Fallback silencioso (texto `null`).

**Fora de escopo:** tool use, memória de conversa (JDBC chat memory — **não** usar, nosso caso é stateless), streaming.

## Arquivos

**Alterar:**
- `pom.xml` — subir Boot/Spring AI/springdoc; trocar starter.
- `application.yml` — bloco Spring AI (OpenAI-compatible apontando p/ Gemini).
- `service/MotorDeRiscoService.java` — injetar `IaService` e preencher os textos.

**Criar:**
- `config/ChatClientConfig.java` (bean `ChatClient` simples, **sem** advisor de memória).
- `client/gemini/IaService.java` (ou `service/IaService.java`).

## Detalhe técnico — copiar do `petbuddies-ai`

### `pom.xml` (alterar)
- `spring-boot-starter-parent`: **3.3.5 → 3.4.5**.
- propriedade `spring-ai.version`: **1.0.0 → 1.1.6** (BOM já importado).
- **Remover** o comentário do `google-genai` e **adicionar** o starter OpenAI:
  ```xml
  <dependency>
      <groupId>org.springframework.ai</groupId>
      <artifactId>spring-ai-starter-model-openai</artifactId>
  </dependency>
  ```
  (Não usar `spring-ai-starter-model-chat-memory-repository-jdbc` — sem memória.)
- `springdoc-openapi-starter-webmvc-ui`: **2.6.0 → 2.8.8** (compatível com Boot 3.4.x).

### `application.yml` (alterar) — equivalente YAML do que o petbuddies usa em `.properties`
```yaml
spring:
  ai:
    openai:
      base-url: https://generativelanguage.googleapis.com/v1beta/openai
      api-key: ${GEMINI_API_KEY}
      chat:
        options:
          model: gemini-2.5-flash
          temperature: 0.2
    retry:
      max-attempts: 3
      backoff:
        initial-interval: 2000
        multiplier: 2
        max-interval: 10000
      on-client-errors: false
```
`GEMINI_API_KEY` é **secret de ambiente** — nunca commitar. (Manter o resto do `application.yml` — datasource, `ddl-auto: create`, open-meteo — intacto.)

### `config/ChatClientConfig.java` (criar) — versão simplificada do petbuddies
```java
@Configuration
public class ChatClientConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();   // sem MessageChatMemoryAdvisor — stateless
    }
}
```

### `client/gemini/IaService.java` (criar) — padrão do `RedatorService`
- Injeta `ChatClient` (ou `ChatClient.Builder` e `.build()` no construtor).
- Método `Optional<DiagnosticoIa> diagnosticar(ContextoAnalise ctx)`:
  - Monta `system` (persona de agrônomo: descreve o risco e recomenda de forma qualitativa; **não** inventa números, **não** decide o nível) e `user` (dados autorizados: cultura + limites, medição, previsão, fatores disparados, nível já calculado).
  - Chama `chatClient.prompt().system(SYSTEM).user(msg).call().content()`.
  - **Fallback (igual ao RedatorService):** `try/catch` em volta da chamada; qualquer falha ou resposta vazia → `Optional.empty()` + log `warn`. Nunca propaga exceção.
  - `DiagnosticoIa(diagnostico, recomendacao)` — se quiser os dois campos separados, pedir saída estruturada (ex. JSON simples) e desserializar, **ou** fazer 2 chamadas curtas. Decidir na implementação; o mais simples é 1 prompt que devolve as duas partes e separar.

### `service/MotorDeRiscoService.java` (alterar)
- Injetar `IaService` via `ObjectProvider<IaService>` (mantém a Frente 1 compilável sem IA).
- Após calcular o nível e montar a análise: `iaService.diagnosticar(ctx)`; se presente, setar `diagnostico`/`recomendacao` antes do save.
- IA **nunca** altera `nivelRisco`.

## Regras de negócio
- Diagnóstico/recomendação são **descritivos**; o nível é do motor.
- IA indisponível/erro → análise salva sem os textos, ingestão segue.
- `temperature: 0.2`, modelo `gemini-2.5-flash`.

## Critérios de aceitação
- [ ] `mvn clean compile` BUILD SUCCESS após o upgrade (Boot 3.4.5 / Spring AI 1.1.6 / springdoc 2.8.8); app sobe.
- [ ] Com `GEMINI_API_KEY` válida, a análise de `POST /leitura` tem `diagnostico` e `recomendacao` preenchidos.
- [ ] Sem chave / com erro, a análise é criada sem os textos e o `POST /leitura` retorna 201.
- [ ] `nivelRisco` é idêntico com ou sem IA.

## Verificação
`mvn clean compile` logo após o upgrade (gate). Smoke com e sem `GEMINI_API_KEY`. Referência viva: `clyvo/petbuddies-ai` (`pom.xml`, `application.properties`, `config/ChatClientConfig.java`, `service/bot/RedatorService.java`).
