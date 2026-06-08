# Spec 04-6: CORS + Seed Automático de Cultura

**Status:** Aprovado (2026-06-07) · **Frente:** 6 (independente — sai primeiro) · **Depende de:** — · **Base:** decisão 9.

## Objetivo
Destravar o consumo pelo front no deploy provisório: liberar CORS e semear automaticamente o catálogo de `Cultura` no boot (já que `ddl-auto: create` zera o banco a cada subida).

## Escopo
- `CorsConfig` básico (configurável por property).
- Seed automático **apenas de `Cultura`** no boot, idempotente.

**Fora de escopo:** seed de produtor/talhão/safra/leitura/análise; troca de `ddl-auto`.

## Arquivos

**Criar:**
- `config/CorsConfig.java`
- `config/CulturaSeeder.java`

**Alterar:**
- `application.yml` — property de origens permitidas.

## Detalhe técnico

### `config/CorsConfig.java`
- `@Configuration` implementando `WebMvcConfigurer`, sobrescrevendo `addCorsMappings`:
  - path `/**`.
  - `allowedOrigins` lidos de `safracerta.cors.allowed-origins` (lista). Provisório: default `*`.
  - métodos `GET, POST, PUT, DELETE, OPTIONS`; headers `*`.
  - ⚠️ Se usar `allowCredentials(true)`, **não** pode usar origem `*` — então no provisório manter `allowCredentials` desligado (default) com origem `*`, ou usar `allowedOriginPatterns("*")`.

### `application.yml`
```yaml
safracerta:
  cors:
    allowed-origins: "*"   # provisório; restringir ao domínio do front no deploy final
```

### `config/CulturaSeeder.java`
- `@Component` implementando `ApplicationRunner` (ou `CommandLineRunner`).
- No boot: se `culturaRepository.count() == 0`, inserir um catálogo base. Idempotente (só semeia se vazio) — convive com `ddl-auto: create`.
- Catálogo sugerido (nome + limites; valores de referência, ajustáveis):
  - **Soja** — diasAteColheita ~120, umidadeSoloCritica ~30 (%), temperaturaMinCritica ~10, temperaturaMaxCritica ~35, chuvaMinima ~5.
  - **Milho** — ~140, ~35, ~8, ~38, ~6.
  - **Café** — ~240, ~40, ~4, ~34, ~4.
  - (acrescentar 2–3 culturas p/ um catálogo apresentável.)
- Usar as unidades do sensor/`Cultura` (umidade em %, conforme conversão do `OpenMeteoMapper`).

## Critérios de aceitação
- [ ] Requisições do front (origem diferente) não são bloqueadas por CORS.
- [ ] Ao subir a aplicação com banco vazio, o catálogo de `Cultura` é semeado automaticamente.
- [ ] Subir novamente não duplica culturas (idempotente).
- [ ] `GET /cultura` retorna o catálogo logo após o boot.
- [ ] `mvn clean compile` BUILD SUCCESS.

## Verificação
`mvn clean compile`. Smoke: subir a app, `GET /cultura` retorna o catálogo; chamada cross-origin do front não toma erro de CORS.

## Nota de deploy (fora desta frente, registrar)
Para o deploy provisório ser útil de forma estável, considerar depois: trocar `ddl-auto` para `update`/`validate` + seed mais amplo, `GEMINI_API_KEY` como secret, e nome do container com o RM (zerador DevOps). Não faz parte do PRD 04.
