# Spec 04-5: Endurecimento de Validações

**Status:** Aprovado (2026-06-07) · **Frente:** 5 (independente) · **Depende de:** — · **Base:** análise (gap de validações esparsas).

## Objetivo
Endurecer as validações Bean Validation nos Request DTOs de cadastro, rejeitando dados inválidos com 400 e mensagem clara. **Sem mudança de comportamento de runtime além da rejeição** — o `GlobalExceptionHandler` já traduz `MethodArgumentNotValidException` para 400 com `fieldErrors` (`handler/GlobalExceptionHandler.java:19`), então **nenhum handler novo é necessário**.

## Escopo
Adicionar anotações aos Request DTOs existentes. Tipos numéricos já são wrappers (`Integer/Double/Long`) — `@NotNull` funciona.

**Fora de escopo:** novas regras de negócio; validações cross-field complexas (além das já existentes como `@OrdensDistintas`).

## Arquivos (alterar Request DTOs)

### `dto/CulturaRequest.java`
- `nome`: `@NotBlank` (já tem).
- `diasAteColheita`: `@Positive` (validado se presente).
- `umidadeSoloCritica`: `@PositiveOrZero` + `@DecimalMax("100.0")` (umidade em %).
- `temperaturaMinCritica` / `temperaturaMaxCritica`: **sem `@PositiveOrZero`** (geada → valores negativos válidos). Opcional: `@DecimalMin("-50.0")`/`@DecimalMax("60.0")` como sanidade.
- `chuvaMinima`: `@PositiveOrZero`.
- Manter nullable (motor tolera ausência) — validar **se presente**.

### `dto/ProdutorRequest.java`
- `cooperativaId`: `@NotNull` (já tem). `nome`: `@NotBlank` (já tem). `cpf`: `@NotBlank @Size(min=11,max=14)` (já tem) → reforçar com `@Pattern(regexp="\\d{11}")` (só dígitos) **ou** aceitar máscara — definir 1 formato.
- `telefone`: `@Pattern` numérico/máscara (se presente).
- `uf`: `@Pattern(regexp="[A-Z]{2}")` (se presente).
- `cep`: `@Pattern(regexp="\\d{8}")` (se presente).

### `dto/CooperativaRequest.java`
- `nome`: `@NotBlank`. `cnpj`: `@NotBlank @Pattern(regexp="\\d{14}")` (1 formato).
- `email`: `@Email` (se presente).
- `uf`: `@Pattern("[A-Z]{2}")`; `cep`: `@Pattern("\\d{8}")` (se presentes).

### `dto/TalhaoRequest.java`
- `produtorId`: `@NotNull`. `nome`: `@NotBlank`.
- `areaHa`: `@PositiveOrZero` (se presente).
- Coordenada `centro` e pontos: latitude `@DecimalMin("-90") @DecimalMax("90")`, longitude `@DecimalMin("-180") @DecimalMax("180")` (no DTO de coordenada, com `@Valid` na composição). Manter `@OrdensDistintas` do polígono.

### `dto/SafraTalhaoRequest.java`
- `talhaoId`, `culturaId`: `@NotNull`. `dataPlantio`: `@NotNull`.
- `dataPrevistaColheita`: opcional; se houver regra (≥ dataPlantio), fica como nota — **não** implementar cross-field agora.

### Já validados (não mexer salvo conferência)
- `dto/DispositivoRequest.java`, `dto/LeituraRequest.java` (faixas físicas `@DecimalMin/@DecimalMax` do PRD 03).

## Critérios de aceitação
- [ ] Enviar `Cultura` com `umidadeSoloCritica` negativa ou > 100 → 400 com `fieldError`.
- [ ] Enviar `Produtor`/`Cooperativa` com UF/CEP/CNPJ/CPF em formato inválido → 400.
- [ ] Enviar `Talhao` com coordenada fora de faixa → 400.
- [ ] Campos opcionais ausentes continuam aceitos (validação só quando presente).
- [ ] `mvn clean compile` BUILD SUCCESS; cadastros válidos seguem funcionando.

## Verificação
`mvn clean compile`. Smoke: payloads inválidos retornam 400 com lista de campos; payloads válidos seguem 201/200.
