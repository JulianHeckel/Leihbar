package de.dhbw.leihbar.domain.aggregates;

import de.dhbw.leihbar.domain.entities.Ausleiher;
import de.dhbw.leihbar.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für das Aggregate Ausleihe.
 * Testet den Builder, Statusübergänge und Geschäftsregeln.
 */
@DisplayName("Ausleihe Aggregate Tests")
class AusleiheTest {

    private Gegenstand gegenstand;
    private Ausleiher ausleiher;
    private Zeitraum zeitraum;
    private Kategorie werkzeug;

    @BeforeEach
    void setUp() {
        werkzeug = Kategorie.of("Werkzeug", 30);
        gegenstand = Gegenstand.neu(
            InventarNummer.of(1),
            "Bohrmaschine",
            "Bosch Professional",
            werkzeug
        );
        ausleiher = Ausleiher.neu(
            "Max",
            "Mustermann",
            Kontaktdaten.nurEmail("max@example.com")
        );
        zeitraum = new Zeitraum(
            LocalDate.now(),
            LocalDate.now().plusDays(7)
        );
    }

    @Test
    @DisplayName("Sollte Ausleihe mit Builder erstellen")
    void sollteAusleiheMitBuilderErstellen() {
        // Act
        Ausleihe ausleihe = new Ausleihe.Builder()
            .gegenstand(gegenstand)
            .ausleiher(ausleiher)
            .zeitraum(zeitraum)
            .build();

        // Assert
        assertNotNull(ausleihe.getId());
        assertEquals(gegenstand.getId(), ausleihe.getGegenstandId());
        assertEquals(ausleiher.getId(), ausleihe.getAusleiherId());
        assertEquals(zeitraum, ausleihe.getGeplanterZeitraum());
        assertEquals(AusleiheStatus.AKTIV, ausleihe.getStatus());
        assertTrue(ausleihe.istAktiv());
    }

    @Test
    @DisplayName("Builder sollte Fehler bei fehlendem Gegenstand werfen")
    void builderSollteFehlerBeiFehlendemGegenstandWerfen() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            new Ausleihe.Builder()
                .ausleiher(ausleiher)
                .zeitraum(zeitraum)
                .build()
        );
    }

    @Test
    @DisplayName("Builder sollte Fehler bei fehlendem Ausleiher werfen")
    void builderSollteFehlerBeiFehlendemAusleiherWerfen() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            new Ausleihe.Builder()
                .gegenstand(gegenstand)
                .zeitraum(zeitraum)
                .build()
        );
    }

    @Test
    @DisplayName("Builder sollte Fehler bei fehlendem Zeitraum werfen")
    void builderSollteFehlerBeiFehlendemZeitraumWerfen() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            new Ausleihe.Builder()
                .gegenstand(gegenstand)
                .ausleiher(ausleiher)
                .build()
        );
    }

    @Test
    @DisplayName("Sollte Ausleihe zurückgeben können")
    void sollteAusleiheZurueckgebenKoennen() {
        // Arrange
        Ausleihe ausleihe = new Ausleihe.Builder()
            .gegenstand(gegenstand)
            .ausleiher(ausleiher)
            .zeitraum(zeitraum)
            .build();

        // Act
        ausleihe.zurueckgeben("Gut erhalten");

        // Assert
        assertEquals(AusleiheStatus.ZURUECKGEGEBEN, ausleihe.getStatus());
        assertFalse(ausleihe.istAktiv());
        assertNotNull(ausleihe.getTatsaechlichesRueckgabedatum());
        assertEquals("Gut erhalten", ausleihe.getZustandsbericht());
    }

    @Test
    @DisplayName("Sollte Ausleihe ohne Zustandsbericht zurückgeben können")
    void sollteAusleiheOhneZustandsberichtZurueckgebenKoennen() {
        // Arrange
        Ausleihe ausleihe = new Ausleihe.Builder()
            .gegenstand(gegenstand)
            .ausleiher(ausleiher)
            .zeitraum(zeitraum)
            .build();

        // Act
        ausleihe.zurueckgeben(null);

        // Assert
        assertEquals(AusleiheStatus.ZURUECKGEGEBEN, ausleihe.getStatus());
        assertNull(ausleihe.getZustandsbericht());
    }

    @Test
    @DisplayName("Sollte Ausleihe stornieren können")
    void sollteAusleiheStornierenKoennen() {
        // Arrange
        Ausleihe ausleihe = new Ausleihe.Builder()
            .gegenstand(gegenstand)
            .ausleiher(ausleiher)
            .zeitraum(zeitraum)
            .build();

        // Act
        ausleihe.stornieren();

        // Assert
        assertEquals(AusleiheStatus.STORNIERT, ausleihe.getStatus());
        assertFalse(ausleihe.istAktiv());
    }

    @Test
    @DisplayName("Sollte bereits zurückgegebene Ausleihe nicht erneut zurückgeben können")
    void sollteBereitsZurueckgegebeneNichtEreutZurueckgeben() {
        // Arrange
        Ausleihe ausleihe = new Ausleihe.Builder()
            .gegenstand(gegenstand)
            .ausleiher(ausleiher)
            .zeitraum(zeitraum)
            .build();
        ausleihe.zurueckgeben(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> ausleihe.zurueckgeben(null));
    }

    @Test
    @DisplayName("Sollte stornierte Ausleihe nicht zurückgeben können")
    void sollteStornierteNichtZurueckgebenKoennen() {
        // Arrange
        Ausleihe ausleihe = new Ausleihe.Builder()
            .gegenstand(gegenstand)
            .ausleiher(ausleiher)
            .zeitraum(zeitraum)
            .build();
        ausleihe.stornieren();

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> ausleihe.zurueckgeben(null));
    }

    @Test
    @DisplayName("Sollte Überfälligkeit korrekt erkennen")
    void sollteUeberfaelligkeitKorrektErkennen() {
        // Arrange - Ausleihe mit Enddatum in der Vergangenheit
        Zeitraum vergangenerZeitraum = new Zeitraum(
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(3)
        );
        Ausleihe ausleihe = new Ausleihe.Builder()
            .gegenstand(gegenstand)
            .ausleiher(ausleiher)
            .zeitraum(vergangenerZeitraum)
            .build();

        // Assert
        assertTrue(ausleihe.istUeberfaellig());
    }

    @Test
    @DisplayName("Sollte Überfälligkeitsstatus aktualisieren")
    void sollteUeberfaelligkeitsstatusAktualisieren() {
        // Arrange
        Zeitraum vergangenerZeitraum = new Zeitraum(
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(3)
        );
        Ausleihe ausleihe = new Ausleihe.Builder()
            .gegenstand(gegenstand)
            .ausleiher(ausleiher)
            .zeitraum(vergangenerZeitraum)
            .build();

        // Act
        ausleihe.aktualisiereUeberfaelligkeitsstatus();

        // Assert
        assertEquals(AusleiheStatus.UEBERFAELLIG, ausleihe.getStatus());
    }
}
