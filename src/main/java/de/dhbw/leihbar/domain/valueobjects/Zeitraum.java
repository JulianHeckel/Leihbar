package de.dhbw.leihbar.domain.valueobjects;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Value Object für einen Zeitraum mit Start- und Enddatum.
 * Wird für Ausleihen verwendet.
 *
 * Immutable, selbstvalidierend, Gleichheit über Wert.
 */
public final class Zeitraum {

    private final LocalDate von;
    private final LocalDate bis;

    public Zeitraum(LocalDate von, LocalDate bis) {
        Objects.requireNonNull(von, "Startdatum darf nicht null sein");
        Objects.requireNonNull(bis, "Enddatum darf nicht null sein");

        if (bis.isBefore(von)) {
            throw new IllegalArgumentException(
                "Enddatum (" + bis + ") darf nicht vor dem Startdatum (" + von + ") liegen"
            );
        }

        this.von = von;
        this.bis = bis;
    }

    /**
     * Factory-Methode für einen Zeitraum ab heute.
     */
    public static Zeitraum abHeute(int tage) {
        if (tage < 0) {
            throw new IllegalArgumentException("Anzahl Tage darf nicht negativ sein");
        }
        LocalDate start = LocalDate.now();
        return new Zeitraum(start, start.plusDays(tage));
    }

    /**
     * Factory-Methode für einen Zeitraum ab einem bestimmten Datum.
     */
    public static Zeitraum ab(LocalDate start, int tage) {
        Objects.requireNonNull(start, "Startdatum darf nicht null sein");
        if (tage < 0) {
            throw new IllegalArgumentException("Anzahl Tage darf nicht negativ sein");
        }
        return new Zeitraum(start, start.plusDays(tage));
    }

    public LocalDate getVon() {
        return von;
    }

    public LocalDate getBis() {
        return bis;
    }

    /**
     * Berechnet die Dauer des Zeitraums in Tagen.
     */
    public long getDauerInTagen() {
        return ChronoUnit.DAYS.between(von, bis);
    }

    /**
     * Prüft, ob ein Datum innerhalb des Zeitraums liegt.
     */
    public boolean enthaelt(LocalDate datum) {
        Objects.requireNonNull(datum, "Datum darf nicht null sein");
        return !datum.isBefore(von) && !datum.isAfter(bis);
    }

    /**
     * Prüft, ob das Enddatum überschritten ist.
     */
    public boolean istUeberfaellig() {
        return LocalDate.now().isAfter(bis);
    }

    /**
     * Berechnet die Anzahl der überfälligen Tage.
     * Gibt 0 zurück, wenn nicht überfällig.
     */
    public long getUeberfaelligeTage() {
        LocalDate heute = LocalDate.now();
        if (!heute.isAfter(bis)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(bis, heute);
    }

    /**
     * Prüft, ob dieser Zeitraum sich mit einem anderen überschneidet.
     */
    public boolean ueberschneidetSichMit(Zeitraum anderer) {
        Objects.requireNonNull(anderer, "Vergleichs-Zeitraum darf nicht null sein");
        return !this.bis.isBefore(anderer.von) && !anderer.bis.isBefore(this.von);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zeitraum zeitraum = (Zeitraum) o;
        return von.equals(zeitraum.von) && bis.equals(zeitraum.bis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(von, bis);
    }

    @Override
    public String toString() {
        return von + " bis " + bis + " (" + getDauerInTagen() + " Tage)";
    }
}
