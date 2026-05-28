package de.dhbw.leihbar.infrastructure.persistence;

import de.dhbw.leihbar.domain.aggregates.Gegenstand;
import de.dhbw.leihbar.domain.repositories.GegenstandRepository;
import de.dhbw.leihbar.domain.valueobjects.InventarNummer;
import de.dhbw.leihbar.domain.valueobjects.Kategorie;
import de.dhbw.leihbar.domain.valueobjects.VerfuegbarkeitsStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integrationstests für das JpaGegenstandRepository gegen eine echte
 * (In-Memory-)H2-Datenbank. Im Gegensatz zu den Service-Unit-Tests, die das
 * Repository mocken, prüfen diese Tests das tatsächliche JPA-Mapping, die
 * Queries und die Round-Trip-Konvertierung Domain <-> JpaEntity.
 *
 * Die Persistence Unit "leihbar-pu" wird wiederverwendet, die JDBC-URL aber per
 * Override auf eine flüchtige In-Memory-Datenbank mit Schema-Neuanlage
 * (create-drop) gesetzt, damit die Produktiv-Datenbank unberührt bleibt.
 */
@DisplayName("JpaGegenstandRepository Integrationstests (H2 In-Memory)")
class JpaGegenstandRepositoryIntegrationTest {

    private static final Map<String, String> IN_MEMORY_OVERRIDES = Map.of(
        "jakarta.persistence.jdbc.url", "jdbc:h2:mem:leihbar-it;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "hibernate.hbm2ddl.auto", "create-drop",
        "hibernate.show_sql", "false"
    );

    private EntityManagerFactory emf;
    private EntityManager entityManager;
    private GegenstandRepository repository;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("leihbar-pu", IN_MEMORY_OVERRIDES);
        entityManager = emf.createEntityManager();
        repository = new JpaGegenstandRepository(entityManager);
    }

    @AfterEach
    void tearDown() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    @DisplayName("Sollte gespeicherten Gegenstand wiederfinden")
    void sollteGespeichertenGegenstandWiederfinden() {
        Gegenstand gegenstand = Gegenstand.neu(
            InventarNummer.of(1), "Bohrmaschine", "Bosch", Kategorie.of("Werkzeug", 30));
        repository.speichern(gegenstand);

        assertThat(repository.findeNachId(gegenstand.getId()))
            .isPresent()
            .get()
            .satisfies(g -> {
                assertThat(g.getName()).isEqualTo("Bohrmaschine");
                assertThat(g.getInventarNummer()).isEqualTo(InventarNummer.of(1));
                assertThat(g.getStatus()).isEqualTo(VerfuegbarkeitsStatus.VERFUEGBAR);
            });
    }

    @Test
    @DisplayName("Sollte nach Inventarnummer suchen und verfügbare Gegenstände liefern")
    void sollteNachInventarnummerUndStatusFiltern() {
        repository.speichern(Gegenstand.neu(InventarNummer.of(1), "Hammer", "", Kategorie.of("Werkzeug", 30)));
        repository.speichern(Gegenstand.neu(InventarNummer.of(2), "Laptop", "", Kategorie.of("Elektronik", 14)));

        assertThat(repository.findeNachInventarNummer(InventarNummer.of(2)))
            .isPresent()
            .get()
            .extracting(Gegenstand::getName)
            .isEqualTo("Laptop");

        assertThat(repository.findeVerfuegbare())
            .hasSize(2)
            .extracting(Gegenstand::getName)
            .containsExactlyInAnyOrder("Hammer", "Laptop");
    }

    @Test
    @DisplayName("Sollte fortlaufende Inventarnummern numerisch vergeben")
    void sollteFortlaufendeInventarnummernVergeben() {
        assertThat(repository.naechsteFreieInventarNummer()).isEqualTo(InventarNummer.of(1));

        repository.speichern(Gegenstand.neu(InventarNummer.of(1), "A", "", Kategorie.of("Werkzeug", 5)));
        repository.speichern(Gegenstand.neu(InventarNummer.of(2), "B", "", Kategorie.of("Werkzeug", 5)));

        assertThat(repository.naechsteFreieInventarNummer()).isEqualTo(InventarNummer.of(3));
    }

    @Test
    @DisplayName("Sollte verwendete Kategorienamen ohne Duplikate liefern")
    void sollteKategorienamenLiefern() {
        repository.speichern(Gegenstand.neu(InventarNummer.of(1), "A", "", Kategorie.of("Werkzeug", 30)));
        repository.speichern(Gegenstand.neu(InventarNummer.of(2), "B", "", Kategorie.of("Werkzeug", 30)));
        repository.speichern(Gegenstand.neu(InventarNummer.of(3), "C", "", Kategorie.of("Elektronik", 14)));

        assertThat(repository.findeAlleKategorienamen())
            .containsExactlyInAnyOrder("Werkzeug", "Elektronik");
    }
}
