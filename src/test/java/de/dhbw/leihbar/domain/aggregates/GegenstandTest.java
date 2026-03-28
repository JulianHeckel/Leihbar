package de.dhbw.leihbar.domain.aggregates;

import de.dhbw.leihbar.domain.valueobjects.InventarNummer;
import de.dhbw.leihbar.domain.valueobjects.Kategorie;
import de.dhbw.leihbar.domain.valueobjects.VerfuegbarkeitsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für das Aggregate Gegenstand.
 * Testet Statusübergänge und Invarianten.
 */
@DisplayName("Gegenstand Aggregate Tests")
class GegenstandTest {

    private Gegenstand gegenstand;
    private InventarNummer inventarNummer;
    private Kategorie werkzeug;

    @BeforeEach
    void setUp() {
        inventarNummer = InventarNummer.of(1);
        werkzeug = Kategorie.of("Werkzeug", 30);
        gegenstand = Gegenstand.neu(
            inventarNummer,
            "Bohrmaschine",
            "Bosch Professional GSB 18V",
            werkzeug
        );
    }

    @Test
    @DisplayName("Sollte neuen Gegenstand mit korrekten Standardwerten erstellen")
    void sollteNeuenGegenstandMitStandardwertenErstellen() {
        // Assert
        assertNotNull(gegenstand.getId());
        assertNotNull(gegenstand.getInventarNummer());
        assertEquals("Bohrmaschine", gegenstand.getName());
        assertEquals("Bosch Professional GSB 18V", gegenstand.getBeschreibung());
        assertEquals(werkzeug, gegenstand.getKategorie());
        assertEquals(VerfuegbarkeitsStatus.VERFUEGBAR, gegenstand.getStatus());
        assertTrue(gegenstand.istVerfuegbar());
    }

    @Test
    @DisplayName("Sollte Gegenstand mit eigener Inventarnummer erstellen")
    void sollteMitEigenerInventarnummerErstellen() {
        // Arrange
        InventarNummer eigenNummer = new InventarNummer("INV-9999");
        Kategorie elektronik = Kategorie.of("Elektronik", 14);

        // Act
        Gegenstand g = Gegenstand.neu(
            eigenNummer,
            "Laptop",
            "Dell XPS 15",
            elektronik
        );

        // Assert
        assertEquals(eigenNummer, g.getInventarNummer());
        assertEquals("INV-9999", g.getInventarNummer().getValue());
    }

    @Test
    @DisplayName("Sollte Status auf AUSGELIEHEN ändern bei Ausleihe")
    void sollteStatusAufAusgesliehenAendernBeiAusleihe() {
        // Arrange
        assertTrue(gegenstand.istVerfuegbar());

        // Act
        gegenstand.ausleihen();

        // Assert
        assertEquals(VerfuegbarkeitsStatus.AUSGELIEHEN, gegenstand.getStatus());
        assertFalse(gegenstand.istVerfuegbar());
    }

    @Test
    @DisplayName("Sollte Status auf VERFUEGBAR ändern bei Rückgabe")
    void sollteStatusAufVerfuegbarAendernBeiRueckgabe() {
        // Arrange
        gegenstand.ausleihen();
        assertFalse(gegenstand.istVerfuegbar());

        // Act
        gegenstand.zurueckgeben();

        // Assert
        assertEquals(VerfuegbarkeitsStatus.VERFUEGBAR, gegenstand.getStatus());
        assertTrue(gegenstand.istVerfuegbar());
    }

    @Test
    @DisplayName("Sollte Fehler werfen bei Ausleihe eines nicht verfügbaren Gegenstands")
    void sollteFehlerWerfenBeiAusleiheNichtVerfuegbar() {
        // Arrange
        gegenstand.ausleihen();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> gegenstand.ausleihen());
    }

    @Test
    @DisplayName("Sollte Fehler werfen bei Rückgabe eines nicht ausgeliehenen Gegenstands")
    void sollteFehlerWerfenBeiRueckgabeNichtAusgeliehen() {
        // Arrange - Gegenstand ist verfügbar

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> gegenstand.zurueckgeben());
    }

    @Test
    @DisplayName("Sollte in Wartung setzen")
    void sollteInWartungSetzen() {
        // Act
        gegenstand.inWartungSetzen();

        // Assert
        assertEquals(VerfuegbarkeitsStatus.IN_WARTUNG, gegenstand.getStatus());
        assertFalse(gegenstand.istVerfuegbar());
    }

    @Test
    @DisplayName("Sollte aus Wartung zurückholen")
    void sollteAusWartungZurueckholen() {
        // Arrange
        gegenstand.inWartungSetzen();

        // Act
        gegenstand.wartungBeenden();

        // Assert
        assertEquals(VerfuegbarkeitsStatus.VERFUEGBAR, gegenstand.getStatus());
    }

    @Test
    @DisplayName("Sollte ausmustern")
    void sollteAusmustern() {
        // Act
        gegenstand.ausmustern();

        // Assert
        assertEquals(VerfuegbarkeitsStatus.AUSGEMUSTERT, gegenstand.getStatus());
        assertFalse(gegenstand.istVerfuegbar());
    }

    @Test
    @DisplayName("Sollte Name und Beschreibung ändern können")
    void sollteNameUndBeschreibungAendernKoennen() {
        // Act
        gegenstand.setName("Akkuschrauber");
        gegenstand.setBeschreibung("Makita DDF484");

        // Assert
        assertEquals("Akkuschrauber", gegenstand.getName());
        assertEquals("Makita DDF484", gegenstand.getBeschreibung());
    }
}
