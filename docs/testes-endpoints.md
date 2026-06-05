# Roteiro de testes de endpoints — SafraCerta API

Bateria de testes **manuais** contra a API rodando, exercitando persistência e regras de negócio (PRD 02 + PRD 03). Como `ddl-auto: create` recria o schema a cada boot, o banco começa **vazio** e pode ser sujo à vontade — basta reiniciar para zerar.

## Pré-requisitos

1. Oracle acessível (config em `application.yml`).
2. Subir a API: `mvn spring-boot:run` → `http://localhost:8080`.
3. Swagger UI: `http://localhost:8080/swagger-ui.html` (caminho mais simples para testar — cole os JSONs abaixo).

> **Windows/PowerShell:** o `curl` nativo é alias de `Invoke-WebRequest`. Use **`curl.exe`** nos comandos abaixo, ou teste pelo **Swagger**. Equivalente PowerShell nativo:
> ```powershell
> Invoke-RestMethod -Uri http://localhost:8080/cooperativa -Method Post `
>   -ContentType 'application/json' -Body '{ "nome": "...", "cnpj": "..." }'
> ```

**Convenção:** cada POST devolve o `id` no corpo (e no header `Location`). Anote os ids — as fases seguintes dependem deles. Abaixo uso placeholders `{idCooperativa}`, `{idTalhao}`, etc.

Base URL assumida: `http://localhost:8080`.

---

## Fase 0 — App no ar

| # | Requisição | Esperado |
|---|---|---|
| 0.1 | `GET /swagger-ui.html` | 200, UI carrega |
| 0.2 | `GET /cooperativa` | 200, coleção **vazia** (`_embedded` ausente) |

```bash
curl.exe http://localhost:8080/cooperativa
```

---

## Fase 1 — Dados base (PRD 02)

Ordem obrigatória por causa das FKs: Cooperativa → Produtor → Cultura → Talhão → Safra.

**1.1 Criar Cooperativa** → 201
```bash
curl.exe -X POST http://localhost:8080/cooperativa -H "Content-Type: application/json" -d "{ \"nome\": \"Cooperativa Sorriso\", \"cnpj\": \"12345678000199\", \"telefone\": \"6699990000\", \"email\": \"contato@coop.com\", \"logradouro\": \"Av. Brasil\", \"numero\": \"1000\", \"bairro\": \"Centro\", \"cidade\": \"Sorriso\", \"cep\": \"78890000\", \"uf\": \"MT\" }"
```

**1.2 Criar Produtor** (usa `{idCooperativa}`) → 201
```bash
curl.exe -X POST http://localhost:8080/produtor -H "Content-Type: application/json" -d "{ \"cooperativaId\": {idCooperativa}, \"nome\": \"João da Silva\", \"telefone\": \"6698887777\", \"cpf\": \"12345678901\", \"nomePropriedade\": \"Fazenda Boa Vista\", \"caf\": \"CAF-001\", \"logradouro\": \"Rod. MT-242, km 10\", \"numero\": \"S/N\", \"bairro\": \"Zona Rural\", \"cidade\": \"Sorriso\", \"cep\": \"78890000\", \"uf\": \"MT\" }"
```

**1.3 Criar Cultura** (Soja) → 201
```bash
curl.exe -X POST http://localhost:8080/cultura -H "Content-Type: application/json" -d "{ \"nome\": \"Soja\", \"diasAteColheita\": 120, \"umidadeSoloCritica\": 20.0, \"temperaturaMinCritica\": 10.0, \"temperaturaMaxCritica\": 35.0, \"chuvaMinima\": 5.0 }"
```

**1.4 Criar Talhão** (usa `{idProdutor}`; **centro com coordenadas reais** — necessário para a previsão Open-Meteo) → 201
```bash
curl.exe -X POST http://localhost:8080/talhao -H "Content-Type: application/json" -d "{ \"produtorId\": {idProdutor}, \"nome\": \"Talhão Norte\", \"areaHa\": 50.0, \"centro\": { \"latitude\": -12.5455, \"longitude\": -55.7215 }, \"pontos\": [ { \"ordem\": 1, \"latitude\": -12.544, \"longitude\": -55.722 }, { \"ordem\": 2, \"latitude\": -12.546, \"longitude\": -55.722 }, { \"ordem\": 3, \"latitude\": -12.546, \"longitude\": -55.720 } ] }"
```
> Edge a testar depois: `pontos` com `ordem` repetida → **400** (`@OrdensDistintas`).

**1.5 Criar Safra** (talhão + cultura, status ATIVA) → 201
```bash
curl.exe -X POST http://localhost:8080/safra -H "Content-Type: application/json" -d "{ \"talhaoId\": {idTalhao}, \"culturaId\": {idCultura}, \"dataPlantio\": \"2026-03-01\", \"dataPrevistaColheita\": \"2026-07-01\", \"statusSafra\": \"ATIVA\" }"
```

✅ **Persistência:** `GET /talhao/{idTalhao}` deve retornar o talhão com `centro` e os 3 `pontos`.

---

## Fase 2 — Dispositivo (PRD 03)

**2.1 Criar dispositivo** → 201
```bash
curl.exe -X POST http://localhost:8080/dispositivo -H "Content-Type: application/json" -d "{ \"codigoDispositivo\": \"ESP-001\", \"talhaoId\": {idTalhao}, \"ativo\": true }"
```

| # | Requisição | Esperado |
|---|---|---|
| 2.2 | `GET /dispositivo` | 200, lista com ESP-001 (links HATEOAS) |
| 2.3 | `GET /dispositivo/{idDispositivo}` | 200 |
| 2.4 | POST outro dispositivo no **mesmo** `{idTalhao}` | **409** (1 por talhão) |
| 2.5 | POST com `codigoDispositivo` `"ESP-001"` repetido (outro talhão) | **409** (código único) |
| 2.6 | POST com `talhaoId` inexistente (ex. 99999) | **404** |
| 2.7 | `GET /dispositivo/99999` | **404** |

**2.4 exemplo:**
```bash
curl.exe -X POST http://localhost:8080/dispositivo -H "Content-Type: application/json" -d "{ \"codigoDispositivo\": \"ESP-002\", \"talhaoId\": {idTalhao}, \"ativo\": true }"
```

---

## Fase 3 — Ingestão de leitura (PRD 03)

**3.1 POST leitura válida** → 201 (e dispara a coleta de previsão)
```bash
curl.exe -X POST http://localhost:8080/leitura -H "Content-Type: application/json" -d "{ \"codigoDispositivo\": \"ESP-001\", \"dataHora\": \"2026-06-05T14:30:00\", \"temperatura\": 28.5, \"umidadeAr\": 65.0, \"umidadeSolo\": 30.0, \"radiacaoSolar\": 800.0 }"
```
✅ Resposta traz `id`, `dispositivoId`, `talhaoId`.

| # | Requisição | Esperado |
|---|---|---|
| 3.2 | `GET /leitura?talhaoId={idTalhao}` | 200, lista com a leitura |
| 3.3 | POST leitura com `temperatura: 99.0` (fora de −50..60) | **400** |
| 3.4 | POST leitura com `umidadeAr: 150.0` (fora de 0..100) | **400** |
| 3.5 | POST leitura com `radiacaoSolar: 5000.0` (fora de 0..1500) | **400** |
| 3.6 | POST leitura sem `codigoDispositivo` ou sem `dataHora` | **400** |
| 3.7 | POST leitura com `codigoDispositivo: "ESP-XXX"` (inexistente) | **404** |

**Dispositivo inativo (3.8):**
1. `PUT /dispositivo/{idDispositivo}` com `"ativo": false` → 200
2. POST leitura com `ESP-001` → **409** (inativo)
3. Reativar: `PUT /dispositivo/{idDispositivo}` com `"ativo": true` → 200 (para seguir as próximas fases)

```bash
curl.exe -X PUT http://localhost:8080/dispositivo/{idDispositivo} -H "Content-Type: application/json" -d "{ \"codigoDispositivo\": \"ESP-001\", \"talhaoId\": {idTalhao}, \"ativo\": false }"
```

---

## Fase 4 — Previsão Open-Meteo (integração externa real)

Após a leitura 3.1, a previsão do talhão deve ter sido coletada no fluxo do POST.

**4.1 Conferir previsão** → 200, **1 item**
```bash
curl.exe "http://localhost:8080/previsao?talhaoId={idTalhao}"
```
✅ Valida a integração + a **Decisão 4** (modelo): o item deve trazer
- `temperatura` (média), `temperaturaMin`, `temperaturaMax` (todos preenchidos),
- `umidadeAr`, `umidadeSolo`, `radiacaoSolar` (médias horárias de D+1),
- `chuva` (soma do dia), `dataHoraPrevista` = amanhã 00:00.

**4.2 Throttle (default 1h):** POST **outra** leitura válida (3.1) logo em seguida → `GET /previsao?talhaoId={idTalhao}` deve continuar com **1 item** (não cria nova previsão dentro da janela).

> Se quiser forçar nova coleta no teste, reduza `safracerta.open-meteo.throttle-horas` no `application.yml` e reinicie.

**4.3 Centro nulo (degradação):** crie um talhão **sem** `centro`, vincule um dispositivo e poste leitura → leitura é **201** normalmente, mas `GET /previsao?talhaoId=` daquele talhão fica **vazio** (previsão pulada, sem erro).

---

## Fase 5 — Regras de remoção de dispositivo

**5.1 Tentar deletar com safra ATIVA** → **409**
```bash
curl.exe -X DELETE http://localhost:8080/dispositivo/{idDispositivo}
```

**5.2 Colher a safra:** `PUT /safra/{idSafra}` com `"statusSafra": "COLHIDA"` → 200
```bash
curl.exe -X PUT http://localhost:8080/safra/{idSafra} -H "Content-Type: application/json" -d "{ \"talhaoId\": {idTalhao}, \"culturaId\": {idCultura}, \"dataPlantio\": \"2026-03-01\", \"dataPrevistaColheita\": \"2026-07-01\", \"statusSafra\": \"COLHIDA\" }"
```

**5.3 Deletar o dispositivo** → **204** (cascata: apaga as leituras dele)
```bash
curl.exe -X DELETE http://localhost:8080/dispositivo/{idDispositivo}
```
✅ **Cascata:** `GET /leitura?talhaoId={idTalhao}` deve voltar **vazio**.

---

## Fase 6 — Integridade referencial (delete bloqueado)

| # | Requisição | Esperado |
|---|---|---|
| 6.1 | `DELETE /cooperativa/{idCooperativa}` (com produtor vinculado) | **409** |
| 6.2 | `DELETE /cultura/{idCultura}` (com safra vinculada) | **409** |
| 6.3 | `DELETE /talhao/{idTalhao}` (com safra/leitura) | **409** (ou 204 conforme dependentes restantes) |

---

## Checklist final

- [ ] Fase 0 — app responde, base vazia
- [ ] Fase 1 — 5 entidades base persistidas (com `centro` e `pontos`)
- [ ] Fase 2 — dispositivo CRUD + 409 (1-por-talhão, código único) + 404
- [ ] Fase 3 — leitura 201 + 400 (faixas) + 404 + 409 (inativo)
- [ ] Fase 4 — previsão coletada com min/max/médias + throttle + centro nulo degrada
- [ ] Fase 5 — delete 409 com safra ativa; 204 + cascata após colher
- [ ] Fase 6 — deletes bloqueados por dependentes (409)

## Dados usados (referência)
- Coordenadas: Sorriso-MT (`-12.5455, -55.7215`) — região real de soja, coberta pela Open-Meteo.
- Throttle de previsão: `safracerta.open-meteo.throttle-horas` (default 1h).
