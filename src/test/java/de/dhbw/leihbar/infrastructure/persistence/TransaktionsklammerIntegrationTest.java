package de.dhbw.leihbar.infrastructure.persistence;

import de.dhbw.leihbar.application.services.TransactionRunner;
import de.dhbw.leihbar.domain.aggregates.Gegenstand;
import de.dhbw.leihbar.domain.repositories.GegenstandRepository;
import de.dhbw.leihbar.domain.valueobjects.InventarNummer;
import de.dhbw.leihbar.domain.valueobjects.Kategorie;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integrationstest für die Transaktionsklammer ({@link JpaTransactionRunner}).
 *
 * Belegt, dass mehrere schreibende Operationen, die innerhalb eines
 * {@code execute(...)}-Blocks laufen, atomar sind: Schlägt eine Operation fehl,
 * wird die gesamte Transaktion zurückgerollt und es bleibt nichts persistiert.
 */
@DisplayName("Transaktionsklammer Integrationstest (Atomarität)")
class TransaktionsklammerIntegrationTest {

    private static final Map<String, String> IN_MEMORY_OVERRIDES = Map.of(
        "jakarta.persistence.jdbc.url", "jdbc:h2:mem:leihbar-tx;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "hibernate.hbm2ddl.auto", "create-drop",
        "hibernate.show_sql", "false"
    );

    private EntityManagerFactory emf;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("leihbar-pu", IN_MEMORY_OVERRIDES);
    }

    @AfterEach
    void tearDown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    @DisplayName("Sollte alle Schreibvorgänge zurückrollen, wenn einer fehlschlägt")
    void sollteBeiFehlerAllesZurueckrollen() {
        EntityManager em = emf.createEntityManager();
        TransactionRunner runner = new JpaTransactionRunner(em);
        GegenstandRepository repository = new JpaGegenstandRepository(em);

        Gegenstand gegenstand = Gegenstand.neu(
            InventarNummer.of(99), "Test", "", Kategorie.of("Werkzeug", 30));

        // Innerhalb der Transaktionsklammer speichern und danach scheitern
        assertThatThrownBy(() -> runner.execute(() -> {
            repository.speichern(gegenstand);  // nimmt an der offenen Transaktion teil
            throw new IllegalStateException("Simulierter Fehler nach dem Speichern");
        })).isInstanceOf(IllegalStateException.class);

        em.close();

        // Frischer EntityManager: Es darf nichts committet worden sein.
        EntityManager pruefEm = emf.createEntityManager();
        GegenstandRepository pruefRepository = new JpaGegenstandRepository(pruefEm);
        assertThat(pruefRepository.findeNachId(gegenstand.getId())).isEmpty();
        assertThat(pruefRepository.zaehleAlle()).isZero();
        pruefEm.close();
    }
}
