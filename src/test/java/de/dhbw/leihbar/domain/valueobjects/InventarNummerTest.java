package de.dhbw.leihbar.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für das Value Object InventarNummer.
 * Testet die Immutabilität, Validierung und Gleichheit.
 */
@DisplayName("InventarNummer Value Object Tests")
class InventarNummerTest {

    @Test
    @DisplayName("Sollte gültige Inventarnummer erstellen")
    void sollteGueltigeInventarnummerErstellen() {
        // Arrange & Act
        InventarNummer nummer = new InventarNummer("INV-1234");

        // Assert
        assertNotNull(nummer);
        assertEquals("INV-1234", nummer.getValue());
    }

    @Test
    @DisplayName("Sollte Inventarnummer aus Nummer erstellen")
    void sollteInventarnummerAusNummerErstellen() {
        // Arrange & Act
        InventarNummer nummer = InventarNummer.of(42);

        // Assert
        assertNotNull(nummer);
        assertEquals("INV-0042", nummer.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"INV-0001", "INV-9999", "INV-5555"})
    @DisplayName("Sollte verschiedene gültige Formate akzeptieren")
    void sollteVerschiedeneGueltigeFormateAkzeptieren(String wert) {
        // Arrange & Act
        InventarNummer nummer = new InventarNummer(wert);

        // Assert
        assertEquals(wert, nummer.getValue());
    }

    @Test
    @DisplayName("Sollte Kleinschreibung normalisieren")
    void sollteKleinschreibungNormalisieren() {
        // Arrange & Act - lowercase input should be normalized to uppercase
        InventarNummer nummer = new InventarNummer("inv-1234");

        // Assert
        assertEquals("INV-1234", nummer.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "INV", "INV-", "INV-123", "INV-12345", "ABC-1234"})
    @DisplayName("Sollte ungültige Formate ablehnen")
    void sollteUngueltigeFormateAblehnen(String wert) {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> new InventarNummer(wert));
    }

    @Test
    @DisplayName("Sollte null ablehnen")
    void sollteNullAblehnen() {
        // Arrange & Act & Assert
        assertThrows(NullPointerException.class, () -> new InventarNummer(null));
    }

    @Test
    @DisplayName("Sollte ungültige Nummern ablehnen")
    void sollteUngueltigeNummernAblehnen() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> InventarNummer.of(-1));
        assertThrows(IllegalArgumentException.class, () -> InventarNummer.of(10000));
    }

    @Test
    @DisplayName("Sollte Gleichheit korrekt implementieren")
    void sollteGleichheitKorrektImplementieren() {
        // Arrange
        InventarNummer nummer1 = new InventarNummer("INV-1234");
        InventarNummer nummer2 = new InventarNummer("INV-1234");
        InventarNummer nummer3 = new InventarNummer("INV-5678");

        // Assert
        assertEquals(nummer1, nummer2);
        assertNotEquals(nummer1, nummer3);
        assertEquals(nummer1.hashCode(), nummer2.hashCode());
    }

    @Test
    @DisplayName("Sollte numerischen Teil korrekt extrahieren")
    void sollteNumerischenTeilKorrektExtrahieren() {
        // Arrange
        InventarNummer nummer = new InventarNummer("INV-0042");

        // Act & Assert
        assertEquals(42, nummer.getNumericPart());
    }

    @Test
    @DisplayName("Sollte sinnvolle toString-Repräsentation liefern")
    void sollteSinnvolleToStringLiefern() {
        // Arrange
        InventarNummer nummer = new InventarNummer("INV-1234");

        // Act
        String result = nummer.toString();

        // Assert
        assertTrue(result.contains("INV-1234"));
    }
}
