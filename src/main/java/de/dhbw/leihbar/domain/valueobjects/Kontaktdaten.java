package de.dhbw.leihbar.domain.valueobjects;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Value Object fuer Kontaktdaten einer Person.
 * PRE-REFACTORING: getTelefon() gibt Optional zurueck.
 * Spaeter wird das zu hatTelefon() + nullable getTelefon() geaendert.
 */
public final class Kontaktdaten {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern TELEFON_PATTERN = Pattern.compile(
        "^[+]?[0-9\\s-]{6,20}$"
    );

    private final String email;
    private final String telefon;

    public Kontaktdaten(String email, String telefon) {
        Objects.requireNonNull(email, "E-Mail darf nicht null sein");

        String normalizedEmail = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("Ungueltiges E-Mail-Format: " + email);
        }

        if (telefon != null && !telefon.isBlank()) {
            String normalizedTelefon = telefon.trim();
            if (!TELEFON_PATTERN.matcher(normalizedTelefon).matches()) {
                throw new IllegalArgumentException("Ungueltiges Telefonnummer-Format: " + telefon);
            }
            this.telefon = normalizedTelefon;
        } else {
            this.telefon = null;
        }

        this.email = normalizedEmail;
    }

    /**
     * Factory-Methode nur mit E-Mail.
     */
    public static Kontaktdaten nurEmail(String email) {
        return new Kontaktdaten(email, null);
    }

    /**
     * Factory-Methode mit E-Mail und Telefon.
     */
    public static Kontaktdaten of(String email, String telefon) {
        return new Kontaktdaten(email, telefon);
    }

    public String getEmail() {
        return email;
    }

    /**
     * Gibt die Telefonnummer als Optional zurueck.
     * PRE-REFACTORING: Wird spaeter zu direktem String + hatTelefon().
     */
    public Optional<String> getTelefon() {
        return Optional.ofNullable(telefon);
    }

    /**
     * Gibt eine formatierte Darstellung der Kontaktdaten zurueck.
     */
    public String formatiert() {
        if (telefon != null) {
            return email + " | " + telefon;
        }
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kontaktdaten that = (Kontaktdaten) o;
        return email.equals(that.email) && Objects.equals(telefon, that.telefon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, telefon);
    }

    @Override
    public String toString() {
        return formatiert();
    }
}
