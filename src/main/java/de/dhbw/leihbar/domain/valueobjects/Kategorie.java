package de.dhbw.leihbar.domain.valueobjects;

import java.util.Objects;

/**
 * Value Object für eine Kategorie von Gegenständen.
 * Jede Kategorie hat einen Namen und eine maximale Ausleihdauer in Tagen.
 *
 * Immutable, selbstvalidierend, Gleichheit über Wert.
 */
public final class Kategorie {

    private static final int DEFAULT_MAX_AUSLEIHTAGE = 14;
    private static final int MIN_AUSLEIHTAGE = 1;
    private static final int MAX_AUSLEIHTAGE = 365;

    private final String name;
    private final int maxAusleihdauerTage;

    public Kategorie(String name, int maxAusleihdauerTage) {
        Objects.requireNonNull(name, "Kategoriename darf nicht null sein");

        String normalizedName = name.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Kategoriename darf nicht leer sein");
        }
        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException("Kategoriename darf maximal 100 Zeichen haben");
        }

        if (maxAusleihdauerTage < MIN_AUSLEIHTAGE || maxAusleihdauerTage > MAX_AUSLEIHTAGE) {
            throw new IllegalArgumentException(
                "Maximale Ausleihdauer muss zwischen " + MIN_AUSLEIHTAGE +
                " und " + MAX_AUSLEIHTAGE + " Tagen liegen, war: " + maxAusleihdauerTage
            );
        }

        this.name = normalizedName;
        this.maxAusleihdauerTage = maxAusleihdauerTage;
    }

    /**
     * Factory-Methode mit Standard-Ausleihdauer.
     */
    public static Kategorie of(String name) {
        return new Kategorie(name, DEFAULT_MAX_AUSLEIHTAGE);
    }

    /**
     * Factory-Methode mit benutzerdefinierter Ausleihdauer.
     */
    public static Kategorie of(String name, int maxAusleihdauerTage) {
        return new Kategorie(name, maxAusleihdauerTage);
    }

    public String getName() {
        return name;
    }

    public int getMaxAusleihdauerTage() {
        return maxAusleihdauerTage;
    }

    /**
     * Prüft, ob die angegebene Ausleihdauer innerhalb des erlaubten Rahmens liegt.
     */
    public boolean istAusleihdauerErlaubt(int tage) {
        return tage >= MIN_AUSLEIHTAGE && tage <= maxAusleihdauerTage;
    }

    /**
     * Prüft, ob ein Zeitraum die maximale Ausleihdauer überschreitet.
     */
    public boolean istZeitraumErlaubt(Zeitraum zeitraum) {
        Objects.requireNonNull(zeitraum, "Zeitraum darf nicht null sein");
        return zeitraum.getDauerInTagen() <= maxAusleihdauerTage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kategorie kategorie = (Kategorie) o;
        return name.equalsIgnoreCase(kategorie.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }

    @Override
    public String toString() {
        return name + " (max. " + maxAusleihdauerTage + " Tage)";
    }
}
