package de.dhbw.leihbar.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Erste Tests fuer Kontaktdaten.
 * PRE-REFACTORING: Verwendet getTelefon().isPresent()/get() statt hatTelefon().
 * Minimale Version — wird spaeter erweitert.
 */
@DisplayName("Kontaktdaten Value Object Tests")
class KontaktdatenTest {

    @Test
    @DisplayName("Sollte Kontaktdaten mit E-Mail und Telefon erstellen")
    void sollteKontaktdatenMitEmailUndTelefonErstellen() {
        Kontaktdaten kontakt = Kontaktdaten.of("test@example.com", "+49 123 456789");

        assertEquals("test@example.com", kontakt.getEmail());
        assertTrue(kontakt.getTelefon().isPresent());
        assertEquals("+49 123 456789", kontakt.getTelefon().get());
    }

    @Test
    @DisplayName("Sollte Kontaktdaten nur mit E-Mail erstellen")
    void sollteKontaktdatenNurMitEmailErstellen() {
        Kontaktdaten kontakt = Kontaktdaten.nurEmail("test@example.com");

        assertEquals("test@example.com", kontakt.getEmail());
        assertFalse(kontakt.getTelefon().isPresent());
    }

    @Test
    @DisplayName("Sollte ungueltige E-Mail ablehnen")
    void sollteUngueltigeEmailAblehnen() {
        assertThrows(IllegalArgumentException.class, () ->
            Kontaktdaten.nurEmail("keine-email")
        );
    }
}
