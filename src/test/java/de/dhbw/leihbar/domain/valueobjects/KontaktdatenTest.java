package de.dhbw.leihbar.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit Tests für das Value Object Kontaktdaten.
 * Testet E-Mail-Validierung und optionale Telefonnummer.
 */
@DisplayName("Kontaktdaten Value Object Tests")
class KontaktdatenTest {

    @Test
    @DisplayName("Sollte Kontaktdaten mit E-Mail und Telefon erstellen")
    void sollteKontaktdatenMitEmailUndTelefonErstellen() {
        // Arrange & Act
        Kontaktdaten kontakt = Kontaktdaten.of("test@example.com", "+49 123 456789");

        // Assert
        assertEquals("test@example.com", kontakt.getEmail());
        assertTrue(kontakt.hatTelefon());
        assertEquals("+49 123 456789", kontakt.getTelefon());
    }

    @Test
    @DisplayName("Sollte Kontaktdaten nur mit E-Mail erstellen")
    void sollteKontaktdatenNurMitEmailErstellen() {
        // Arrange & Act
        Kontaktdaten kontakt = Kontaktdaten.nurEmail("test@example.com");

        // Assert
        assertEquals("test@example.com", kontakt.getEmail());
        assertFalse(kontakt.hatTelefon());
        assertNull(kontakt.getTelefon());
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@domain.com", "test.user@example.org", "a@b.de"})
    @DisplayName("Sollte gültige E-Mail-Adressen akzeptieren")
    void sollteGueltigeEmailsAkzeptieren(String email) {
        // Arrange & Act
        Kontaktdaten kontakt = Kontaktdaten.nurEmail(email);

        // Assert
        assertNotNull(kontakt.getEmail());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "no@domain", "@domain.com"})
    @DisplayName("Sollte ungültige E-Mail-Adressen ablehnen")
    void sollteUngueltigeEmailsAblehnen(String email) {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> Kontaktdaten.nurEmail(email));
    }

    @Test
    @DisplayName("Sollte null E-Mail ablehnen")
    void sollteNullEmailAblehnen() {
        // Arrange & Act & Assert
        assertThrows(NullPointerException.class, () -> Kontaktdaten.nurEmail(null));
    }

    @Test
    @DisplayName("Sollte leeres Telefon als null behandeln")
    void sollteLeereTelefonAlsNullBehandeln() {
        // Arrange & Act
        Kontaktdaten kontakt = Kontaktdaten.of("test@example.com", "");

        // Assert
        assertFalse(kontakt.hatTelefon());
        assertNull(kontakt.getTelefon());
    }

    @Test
    @DisplayName("Sollte Gleichheit korrekt implementieren")
    void sollteGleichheitKorrektImplementieren() {
        // Arrange
        Kontaktdaten kontakt1 = Kontaktdaten.of("test@example.com", "123456");
        Kontaktdaten kontakt2 = Kontaktdaten.of("test@example.com", "123456");
        Kontaktdaten kontakt3 = Kontaktdaten.of("other@example.com", "123456");

        // Assert
        assertEquals(kontakt1, kontakt2);
        assertNotEquals(kontakt1, kontakt3);
        assertEquals(kontakt1.hashCode(), kontakt2.hashCode());
    }

    @Test
    @DisplayName("Sollte formatierte Ausgabe liefern")
    void sollteFormatatierteAusgabeLiefern() {
        // Arrange
        Kontaktdaten kontakt = Kontaktdaten.of("test@example.com", "123456");

        // Act
        String formatted = kontakt.formatiert();

        // Assert
        assertTrue(formatted.contains("test@example.com"));
        assertTrue(formatted.contains("123456"));
    }
}
