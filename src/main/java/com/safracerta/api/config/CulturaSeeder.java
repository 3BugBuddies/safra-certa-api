package com.safracerta.api.config;

import com.safracerta.api.entity.Cultura;
import com.safracerta.api.repository.CulturaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Seed idempotente do catálogo de culturas (só roda se a tabela estiver vazia). */
@Component
public class CulturaSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CulturaSeeder.class);

    private final CulturaRepository culturaRepository;

    public CulturaSeeder(CulturaRepository culturaRepository) {
        this.culturaRepository = culturaRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (culturaRepository.count() > 0) {
            return;
        }
        List<Cultura> catalogo = List.of(
                //      nome,         dias, umSolo%, tMin,  tMax, chuvaMin
                // --- Grãos / commodities ---
                cultura("Soja",        160,   30.0,  10.0,  40.0,  5.0),
                cultura("Milho",       130,   35.0,  10.0,  35.0,  6.0),
                cultura("Feijão",       90,   35.0,   5.0,  30.0,  5.0),
                cultura("Café",        240,   35.0,   2.0,  34.0,  4.0),
                cultura("Trigo",       120,   25.0,  -1.0,  32.0,  3.0),
                // --- Hortaliças / PNAE ---
                cultura("Tomate",      120,   40.0,   2.0,  34.0,  5.0),
                cultura("Alface",       60,   45.0,   0.0,  30.0,  4.0),
                cultura("Cenoura",     100,   35.0,   0.0,  30.0,  4.0),
                cultura("Batata",      110,   40.0,   2.0,  30.0,  5.0),
                cultura("Cebola",      120,   35.0,   0.0,  32.0,  3.0),
                cultura("Repolho",      90,   40.0,  -2.0,  30.0,  4.0),
                cultura("Couve",        70,   40.0,  -2.0,  32.0,  4.0),
                cultura("Mandioca",    300,   20.0,   5.0,  38.0,  2.0),
                cultura("Abóbora",     120,   35.0,   5.0,  35.0,  4.0)
        );
        culturaRepository.saveAll(catalogo);
        log.info("Catálogo de culturas semeado: {} registros", catalogo.size());
    }

    private static Cultura cultura(String nome, Integer dias, Double umidadeSoloCritica,
                                   Double tempMin, Double tempMax, Double chuvaMinima) {
        Cultura c = new Cultura();
        c.setNome(nome);
        c.setDiasAteColheita(dias);
        c.setUmidadeSoloCritica(umidadeSoloCritica);
        c.setTemperaturaMinCritica(tempMin);
        c.setTemperaturaMaxCritica(tempMax);
        c.setChuvaMinima(chuvaMinima);
        return c;
    }
}
