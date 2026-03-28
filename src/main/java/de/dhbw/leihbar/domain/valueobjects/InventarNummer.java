package de.dhbw.leihbar.domain.valueobjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object für die eindeutige Inventarnummer eines Gegenstandes.
 * Format: INV-XXXX (z.B. INV-0001)
 *
 * Immutable, selbstvalidierend, Gleichheit über Wert.
 */
public final class InventarNummer {

    private static final Pattern VALID_PATTERN = Pattern.compile("^INV-\\d{4}$");
    private static final String PREFIX = "INV-";

    private final String value;

    public InventarNummer(String value) {
        Objects.requireNonNull(value, "Inventarnummer darf nicht null sein");
        String normalized = value.toUpperCase().trim();

        if (!VALID_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                "Ungültiges Inventarnummer-Format. Erwartet: INV-XXXX (z.B. INV-0001), erhalten: " + value
            );
        }

        this.value = normalized;
    }

    /**
     * Factory-Methode zur Erzeugung einer Inventarnummer aus einer fortlaufenden Nummer.
     */
    public static InventarNummer of(int nummer) {
        if (nummer < 0 || nummer > 9999) {
            throw new IllegalArgumentException("Nummer muss zwischen 0 und 9999 liegen");
        }
        return new InventarNummer(PREFIX + String.format("%04d", nummer));
    }

    /**
     * Factory-Methode zur Erzeugung aus einem String.
     */
    public static InventarNummer of(String value) {
        return new InventarNummer(value);
    }

    public String getValue() {
        return value;
    }

    /**
     * Extrahiert die numerische Komponente der Inventarnummer.
     */
    public int getNumericPart() {
        return Integer.parseInt(value.substring(PREFIX.length()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventarNummer that = (InventarNummer) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
