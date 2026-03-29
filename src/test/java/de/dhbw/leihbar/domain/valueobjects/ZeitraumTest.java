package de.dhbw.leihbar.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für das Value Object Zeitraum.
 * Testet Validierung, Überfälligkeitsprüfung und Überlappungen.
 */
@DisplayName("Zeitraum Value Object Tests")
class ZeitraumTest {

    @Test
    @DisplayName("Sollte gültigen Zeitraum erstellen")
    void sollteGueltigenZeitraumErstellen() {
        // Arrange
        LocalDate start = LocalDate.now();
        LocalDate ende = LocalDate.now().plusDays(7);

        // Act
        Zeitraum zeitraum = new Zeitraum(start, ende);

        // Assert
        assertEquals(start, zeitraum.getVon());
        assertEquals(ende, zeitraum.getBis());
    }

    @Test
    @DisplayName("Sollte Zeitraum mit gleichem Start- und Enddatum erlauben")
    void sollteGleichesStartUndEnddatumErlauben() {
        // Arrange
        LocalDate datum = LocalDate.now();

        // Act
        Zeitraum zeitraum = new Zeitraum(datum, datum);

        // Assert
        assertEquals(datum, zeitraum.getVon());
        assertEquals(datum, zeitraum.getBis());
        assertEquals(0, zeitraum.getDauerInTagen());
    }

    @Test
    @DisplayName("Sollte Enddatum vor Startdatum ablehnen")
    void sollteEnddatumVorStartdatumAblehnen() {
        // Arrange
        LocalDate start = LocalDate.now();
        LocalDate ende = LocalDate.now().minusDays(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new Zeitraum(start, ende));
    }

    @Test
    @DisplayName("Sollte null-Werte ablehnen")
    void sollteNullWerteAblehnen() {
        // Arrange
        LocalDate datum = LocalDate.now();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> new Zeitraum(null, datum));
        assertThrows(NullPointerException.class, () -> new Zeitraum(datum, null));
    }

    @Test
    @DisplayName("Sollte Überfälligkeit korrekt erkennen")
    void sollteUeberfaelligkeitKorrektErkennen() {
        // Arrange
        Zeitraum vergangenerZeitraum = new Zeitraum(
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(1)
        );
        Zeitraum aktuellerZeitraum = new Zeitraum(
            LocalDate.now(),
            LocalDate.now().plusDays(7)
        );

        // Act & Assert
        assertTrue(vergangenerZeitraum.istUeberfaellig());
        assertFalse(aktuellerZeitraum.istUeberfaellig());
    }

    @Test
    @DisplayName("Sollte Dauer in Tagen korrekt berechnen")
    void sollteDauerInTagenKorrektBerechnen() {
        // Arrange
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate ende = LocalDate.of(2024, 1, 8);
        Zeitraum zeitraum = new Zeitraum(start, ende);

        // Act
        long dauer = zeitraum.getDauerInTagen();

        // Assert
        assertEquals(7, dauer);
    }

    @Test
    @DisplayName("Sollte Überlappung korrekt erkennen")
    void sollteUeberlappungKorrektErkennen() {
        // Arrange
        Zeitraum zeitraum1 = new Zeitraum(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 10)
        );
        Zeitraum zeitraum2 = new Zeitraum(
            LocalDate.of(2024, 1, 5),
            LocalDate.of(2024, 1, 15)
        );
        Zeitraum zeitraum3 = new Zeitraum(
            LocalDate.of(2024, 1, 15),
            LocalDate.of(2024, 1, 20)
        );

        // Act & Assert
        assertTrue(zeitraum1.ueberschneidetSichMit(zeitraum2));
        assertFalse(zeitraum1.ueberschneidetSichMit(zeitraum3));
    }

    @Test
    @DisplayName("Sollte Gleichheit korrekt implementieren")
    void sollteGleichheitKorrektImplementieren() {
        // Arrange
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate ende = LocalDate.of(2024, 1, 10);
        Zeitraum zeitraum1 = new Zeitraum(start, ende);
        Zeitraum zeitraum2 = new Zeitraum(start, ende);
        Zeitraum zeitraum3 = new Zeitraum(start, ende.plusDays(1));

        // Assert
        assertEquals(zeitraum1, zeitraum2);
        assertNotEquals(zeitraum1, zeitraum3);
        assertEquals(zeitraum1.hashCode(), zeitraum2.hashCode());
    }
}
