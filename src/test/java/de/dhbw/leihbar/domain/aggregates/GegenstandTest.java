package de.dhbw.leihbar.domain.aggregates;

import de.dhbw.leihbar.domain.valueobjects.Kategorie;
import de.dhbw.leihbar.domain.valueobjects.VerfuegbarkeitsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Erste Tests fuer Gegenstand.
 * PRE-REFACTORING: Verwendet getBezeichnung(), inWartung(), Kategorie.WERKZEUG.
 * Minimale Version — wird spaeter erweitert.
 */
@DisplayName("Gegenstand Aggregate Tests")
class GegenstandTest {

    private Gegenstand gegenstand;

    @BeforeEach
    void setUp() {
        gegenstand = Gegenstand.neu(
            "Bohrmaschine",
            "Bosch Professional GSB 18V",
            Kategorie.WERKZEUG
        );
    }

    @Test
    @DisplayName("Neuer Gegenstand hat korrekte Standardwerte")
    void sollteNeuenGegenstandMitStandardwertenErstellen() {
        assertNotNull(gegenstand.getId());
        assertEquals("Bohrmaschine", gegenstand.getBezeichnung());
        assertEquals(VerfuegbarkeitsStatus.VERFUEGBAR, gegenstand.getStatus());
        assertTrue(gegenstand.istVerfuegbar());
    }

    @Test
    @DisplayName("Ausleihe aendert Status auf AUSGELIEHEN")
    void sollteStatusAufAusgesliehenAendern() {
        gegenstand.ausleihen();

        assertEquals(VerfuegbarkeitsStatus.AUSGELIEHEN, gegenstand.getStatus());
        assertFalse(gegenstand.istVerfuegbar());
    }

    @Test
    @DisplayName("Rueckgabe aendert Status auf VERFUEGBAR")
    void sollteStatusAufVerfuegbarAendern() {
        gegenstand.ausleihen();
        gegenstand.zurueckgeben();

        assertEquals(VerfuegbarkeitsStatus.VERFUEGBAR, gegenstand.getStatus());
    }

    @Test
    @DisplayName("Nicht verfuegbaren Gegenstand ausleihen wirft Exception")
    void sollteFehlerWerfenBeiAusleiheNichtVerfuegbar() {
        gegenstand.ausleihen();

        assertThrows(IllegalStateException.class, () -> gegenstand.ausleihen());
    }
}
