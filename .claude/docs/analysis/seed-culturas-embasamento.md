# Embasamento do Seed de Culturas

**Onde é usado:** `config/CulturaSeeder.java` (seed automático no boot). **Consumidor:** Motor de Risco (`MotorDeRiscoService`).

## Semântica dos limites
- **`umidadeSoloCritica` (%):** umidade do solo (sensor) abaixo da qual há estresse hídrico → fator "solo seco".
- **`temperaturaMinCritica` (°C):** mínima prevista (D+1) abaixo dela → fator "geada/frio".
- **`temperaturaMaxCritica` (°C):** máxima prevista (D+1) acima dela → fator "calor".
- **`chuvaMinima` (mm):** chuva diária prevista abaixo dela → fator "déficit hídrico".
- **`diasAteColheita`:** ciclo médio plantio→colheita.

> Valores de **referência para a demo**, calibrados a partir de fontes agronômicas reconhecidas. Não substituem zoneamento/recomendação local. Onde a fonte traz faixa, adotou-se o limiar de **início de dano/estresse** (não o ótimo).

## Fontes principais
- **Embrapa — Agência de Informação Tecnológica** (cultivos soja, milho, batata): exigências climáticas e relações com o clima.
- **Embrapa Trigo, Embrapa Hortaliças, Embrapa Mandioca**: faixas térmicas, geada e ciclos.
- **FAO EcoCrop / FAO-56**: faixas cardinais (T mín/ótima/máx) onde a fonte Embrapa não dá o valor pontual.

## Tabela (valores semeados)

| Cultura | dias | umSolo % | tMin °C | tMax °C | chuva mm | Justificativa / fonte |
|---|---:|---:|---:|---:|---:|---|
| Soja | 160 | 30 | 10 | 40 | 5 | Ciclo ~160d; ótimo 20–30°C, inadequado ≤10°C, dano >40°C (Embrapa Soja). |
| Milho | 130 | 35 | 10 | 35 | 6 | Germinação 25–30°C, dano <10°C/>40°C; estresse de pendoamento ~35°C (Embrapa Milho). |
| Feijão | 90 | 35 | 5 | 30 | 5 | Ciclo ~90d, ideal 21–29°C, intolerante a geada; abortamento de vagens em calor >30°C (Embrapa). |
| Café | 240 | 35 | 2 | 34 | 4 | Perene; ótimo 18–23°C; dano foliar por geada ~2°C; aborto floral no calor >34°C (Embrapa Café). |
| Trigo | 120 | 25 | -1 | 32 | 3 | Dano severo de geada no florescimento < -1°C (Embrapa Trigo); estresse de enchimento >32°C. |
| Tomate | 120 | 40 | 2 | 34 | 5 | Sensível a geada (~0–2°C); queda de flor/fruto em calor >32–35°C (Embrapa Hortaliças). |
| Alface | 60 | 45 | 0 | 30 | 4 | Ciclo curto; raiz rasa (sensível à seca); pendoamento/amargor em calor >~30°C (Embrapa). |
| Cenoura | 100 | 35 | 0 | 30 | 4 | Germinação 20–30°C; raízes melhores em clima ameno; estresse >30°C (Embrapa). |
| Batata | 110 | 40 | 2 | 30 | 5 | Tuberiza melhor ~15°C (faixa 10–22°C), inibida no calor; ciclo 110–120d; geada danifica folhagem (Embrapa Batata). |
| Cebola | 120 | 35 | 0 | 32 | 3 | Germinação ~20°C; tolerante; bulbificação prejudicada no calor alto (Embrapa). |
| Repolho | 90 | 40 | -2 | 30 | 4 | Brássica rústica ao frio; germinação 20–30°C; "cabeça" ruim em calor >30°C (Embrapa). |
| Couve | 70 | 40 | -2 | 32 | 4 | Brássica tolerante ao frio; ciclo curto de colheita contínua (Embrapa). |
| Mandioca | 300 | 20 | 5 | 38 | 2 | Ciclo longo; ótimo 25–29°C; muito tolerante à seca; parte aérea sensível a geada (Embrapa Mandioca). |
| Abóbora | 120 | 35 | 5 | 35 | 4 | Favorável 14–35°C, ótimo 25–30°C, **não** resiste a geada (Embrapa Hortaliças). |

## Observações
- **`chuvaMinima`** é o limite **diário** mais frágil conceitualmente (um dia seco isolado não é déficit); mantido baixo (2–6 mm) e proporcional à demanda hídrica. Refinar para janela acumulada numa próxima rodada.
- Mandioca/Café têm ciclos atípicos (longo/perene); `diasAteColheita` aproxima o ciclo de produção para fins de fase fenológica derivada.
