package de.dhbw.leihbar.domain.aggregates;

import de.dhbw.leihbar.domain.valueobjects.AusleiheStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Erste Tests fuer Ausleihe.
 * PRE-REFACTORING: Verwendet Konstruktor statt Builder, getZeitraum() statt getGeplanterZeitraum(),
 * getZustandsbericht().isPresent()/get().
 * Minimale Version — wird spaeter erweitert.
 */
@DisplayName("Ausleihe Aggregate Tests")
class AusleiheTest {

    private final UUID gegenstandId = UUID.randomUUID();
    private final UUID ausleiherId = UUID.randomUUID();

    @Test
    @DisplayName("Sollte neue Ausleihe erstellen")
    void sollteNeueAusleiheErstellen() {
        Ausleihe ausleihe = new Ausleihe(
            gegenstandId, ausleiherId,
            LocalDate.now(), LocalDate.now().plusDays(7)
        );

        assertNotNull(ausleihe.getId());
        assertEquals(gegenstandId, ausleihe.getGegenstandId());
        assertEquals(AusleiheStatus.AKTIV, ausleihe.getStatus());
        assertTrue(ausleihe.istAktiv());
    }

    @Test
    @DisplayName("Sollte Ausleihe zurueckgeben koennen")
    void sollteAusleiheZurueckgebenKoennen() {
        Ausleihe ausleihe = new Ausleihe(
            gegenstandId, ausleiherId,
            LocalDate.now(), LocalDate.now().plusDays(7)
        );

        ausleihe.zurueckgeben("Gut erhalten");

        assertEquals(AusleiheStatus.ZURUECKGEGEBEN, ausleihe.getStatus());
        assertFalse(ausleihe.istAktiv());
        assertTrue(ausleihe.getZustandsbericht().isPresent());
        assertEquals("Gut erhalten", ausleihe.getZustandsbericht().get());
    }

    @Test
    @DisplayName("Sollte Ausleihe stornieren koennen")
    void sollteAusleiheStornierenKoennen() {
        Ausleihe ausleihe = new Ausleihe(
            gegenstandId, ausleiherId,
            LocalDate.now(), LocalDate.now().plusDays(7)
        );

        ausleihe.stornieren();

        assertEquals(AusleiheStatus.STORNIERT, ausleihe.getStatus());
    }

    @Test
    @DisplayName("Sollte bereits zurueckgegebene nicht erneut zurueckgeben")
    void sollteBereitsZurueckgegebeneNichtEreutZurueckgeben() {
        Ausleihe ausleihe = new Ausleihe(
            gegenstandId, ausleiherId,
            LocalDate.now(), LocalDate.now().plusDays(7)
        );
        ausleihe.zurueckgeben(null);

        assertThrows(IllegalStateException.class, () -> ausleihe.zurueckgeben(null));
    }
}
