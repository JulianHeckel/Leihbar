package de.dhbw.leihbar.domain.entities;

import de.dhbw.leihbar.domain.valueobjects.Kontaktdaten;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für die Entity Ausleiher.
 * Testet Identität, Validierung und Mutabilität.
 */
@DisplayName("Ausleiher Entity Tests")
class AusleiherTest {

    @Test
    @DisplayName("Sollte neuen Ausleiher mit generierter ID erstellen")
    void sollteNeuenAusleiherMitGenerierterIdErstellen() {
        // Arrange
        Kontaktdaten kontakt = Kontaktdaten.of("test@example.com", null);

        // Act
        Ausleiher ausleiher = Ausleiher.neu("Max", "Mustermann", kontakt);

        // Assert
        assertNotNull(ausleiher.getId());
        assertEquals("Max", ausleiher.getVorname());
        assertEquals("Mustermann", ausleiher.getNachname());
        assertEquals(kontakt, ausleiher.getKontaktdaten());
    }

    @Test
    @DisplayName("Sollte vollständigen Namen korrekt formatieren")
    void sollteVollstaendigenNamenKorrektFormatieren() {
        // Arrange
        Kontaktdaten kontakt = Kontaktdaten.of("test@example.com", null);
        Ausleiher ausleiher = Ausleiher.neu("Max", "Mustermann", kontakt);

        // Act
        String vollerName = ausleiher.getVollerName();

        // Assert
        assertEquals("Max Mustermann", vollerName);
    }

    @Test
    @DisplayName("Sollte Ausleiher mit bestehender ID erstellen")
    void sollteAusleiherMitBestehenderIdErstellen() {
        // Arrange
        UUID id = UUID.randomUUID();
        Kontaktdaten kontakt = Kontaktdaten.of("test@example.com", null);

        // Act
        Ausleiher ausleiher = new Ausleiher(id, "Anna", "Schmidt", kontakt);

        // Assert
        assertEquals(id, ausleiher.getId());
    }

    @Test
    @DisplayName("Sollte Vorname und Nachname ändern können")
    void sollteVornameUndNachnameAendernKoennen() {
        // Arrange
        Kontaktdaten kontakt = Kontaktdaten.of("test@example.com", null);
        Ausleiher ausleiher = Ausleiher.neu("Max", "Mustermann", kontakt);

        // Act
        ausleiher.umbenennen("Moritz", "Meier");

        // Assert
        assertEquals("Moritz", ausleiher.getVorname());
        assertEquals("Meier", ausleiher.getNachname());
        assertEquals("Moritz Meier", ausleiher.getVollerName());
    }

    @Test
    @DisplayName("Sollte Kontaktdaten ändern können")
    void sollteKontaktdatenAendernKoennen() {
        // Arrange
        Kontaktdaten originalKontakt = Kontaktdaten.of("original@example.com", null);
        Ausleiher ausleiher = Ausleiher.neu("Max", "Mustermann", originalKontakt);
        Kontaktdaten neuerKontakt = Kontaktdaten.of("neu@example.com", "123456");

        // Act
        ausleiher.kontaktdatenAendern(neuerKontakt);

        // Assert
        assertEquals(neuerKontakt, ausleiher.getKontaktdaten());
        assertEquals("neu@example.com", ausleiher.getKontaktdaten().getEmail());
    }

    @Test
    @DisplayName("Sollte leeren Vornamen ablehnen")
    void sollteLeerenVornamenAblehnen() {
        // Arrange
        Kontaktdaten kontakt = Kontaktdaten.of("test@example.com", null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            Ausleiher.neu("", "Mustermann", kontakt)
        );
    }

    @Test
    @DisplayName("Sollte leeren Nachnamen ablehnen")
    void sollteLeerenNachnamenAblehnen() {
        // Arrange
        Kontaktdaten kontakt = Kontaktdaten.of("test@example.com", null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            Ausleiher.neu("Max", "", kontakt)
        );
    }

    @Test
    @DisplayName("Sollte null Kontaktdaten ablehnen")
    void sollteNullKontaktdatenAblehnen() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            Ausleiher.neu("Max", "Mustermann", null)
        );
    }

    @Test
    @DisplayName("Zwei Ausleiher mit gleicher ID sollten gleich sein")
    void zweiAusleiherMitGleicherIdSolltenGleichSein() {
        // Arrange
        UUID id = UUID.randomUUID();
        Kontaktdaten kontakt1 = Kontaktdaten.of("test1@example.com", null);
        Kontaktdaten kontakt2 = Kontaktdaten.of("test2@example.com", null);

        Ausleiher ausleiher1 = new Ausleiher(id, "Max", "Mustermann", kontakt1);
        Ausleiher ausleiher2 = new Ausleiher(id, "Anna", "Schmidt", kontakt2);

        // Assert - Entities sind gleich, wenn ihre ID gleich ist
        assertEquals(ausleiher1, ausleiher2);
        assertEquals(ausleiher1.hashCode(), ausleiher2.hashCode());
    }

    @Test
    @DisplayName("Zwei Ausleiher mit verschiedener ID sollten ungleich sein")
    void zweiAusleiherMitVerschiedenerIdSolltenUngleichSein() {
        // Arrange
        Kontaktdaten kontakt = Kontaktdaten.of("test@example.com", null);
        Ausleiher ausleiher1 = Ausleiher.neu("Max", "Mustermann", kontakt);
        Ausleiher ausleiher2 = Ausleiher.neu("Max", "Mustermann", kontakt);

        // Assert
        assertNotEquals(ausleiher1, ausleiher2);
    }
}
