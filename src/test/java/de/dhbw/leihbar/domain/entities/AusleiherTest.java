package de.dhbw.leihbar.domain.entities;

import de.dhbw.leihbar.domain.valueobjects.Kontaktdaten;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Erste Tests fuer Ausleiher.
 * PRE-REFACTORING: Verwendet getVollstaendigerName() und Ausleiher.mitId().
 * Minimale Version — wird spaeter erweitert.
 */
@DisplayName("Ausleiher Entity Tests")
class AusleiherTest {

    @Test
    @DisplayName("Sollte neuen Ausleiher erstellen")
    void sollteNeuenAusleiherErstellen() {
        Kontaktdaten kontakt = Kontaktdaten.of("test@example.com", null);

        Ausleiher ausleiher = Ausleiher.neu("Max", "Mustermann", kontakt);

        assertNotNull(ausleiher.getId());
        assertEquals("Max", ausleiher.getVorname());
        assertEquals("Mustermann", ausleiher.getNachname());
    }

    @Test
    @DisplayName("Sollte vollstaendigen Namen formatieren")
    void sollteVollstaendigenNamenFormatieren() {
        Kontaktdaten kontakt = Kontaktdaten.of("test@example.com", null);
        Ausleiher ausleiher = Ausleiher.neu("Max", "Mustermann", kontakt);

        assertEquals("Max Mustermann", ausleiher.getVollstaendigerName());
    }

    @Test
    @DisplayName("Sollte leeren Vornamen ablehnen")
    void sollteLeerenVornamenAblehnen() {
        Kontaktdaten kontakt = Kontaktdaten.of("test@example.com", null);

        assertThrows(IllegalArgumentException.class, () ->
            Ausleiher.neu("", "Mustermann", kontakt)
        );
    }
}
