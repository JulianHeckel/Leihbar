package de.dhbw.leihbar.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Erste Tests fuer Zeitraum.
 * PRE-REFACTORING: Verwendet getStartdatum()/getEnddatum(), ueberschneidetSich().
 * Minimale Version — wird spaeter erweitert.
 */
@DisplayName("Zeitraum Value Object Tests")
class ZeitraumTest {

    @Test
    @DisplayName("Sollte gueltigen Zeitraum erstellen")
    void sollteGueltigenZeitraumErstellen() {
        LocalDate start = LocalDate.now();
        LocalDate ende = LocalDate.now().plusDays(7);

        Zeitraum zeitraum = new Zeitraum(start, ende);

        assertEquals(start, zeitraum.getStartdatum());
        assertEquals(ende, zeitraum.getEnddatum());
    }

    @Test
    @DisplayName("Sollte Enddatum vor Startdatum ablehnen")
    void sollteEnddatumVorStartdatumAblehnen() {
        LocalDate start = LocalDate.now();
        LocalDate ende = LocalDate.now().minusDays(1);

        assertThrows(IllegalArgumentException.class, () -> new Zeitraum(start, ende));
    }

    @Test
    @DisplayName("Sollte Dauer in Tagen korrekt berechnen")
    void sollteDauerInTagenKorrektBerechnen() {
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate ende = LocalDate.of(2024, 1, 8);
        Zeitraum zeitraum = new Zeitraum(start, ende);

        assertEquals(7, zeitraum.getDauerInTagen());
    }
}
