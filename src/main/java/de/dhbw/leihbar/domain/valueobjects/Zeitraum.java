package de.dhbw.leihbar.domain.valueobjects;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Value Object fuer einen Zeitraum mit Start- und Enddatum.
 * PRE-REFACTORING: getStartdatum/getEnddatum werden spaeter zu getVon/getBis.
 * ueberschneidetSich() wird zu ueberschneidetSichMit().
 */
public final class Zeitraum {

    private final LocalDate startdatum;
    private final LocalDate enddatum;

    public Zeitraum(LocalDate startdatum, LocalDate enddatum) {
        Objects.requireNonNull(startdatum, "Startdatum darf nicht null sein");
        Objects.requireNonNull(enddatum, "Enddatum darf nicht null sein");

        if (enddatum.isBefore(startdatum)) {
            throw new IllegalArgumentException(
                "Enddatum (" + enddatum + ") darf nicht vor dem Startdatum (" + startdatum + ") liegen"
            );
        }

        this.startdatum = startdatum;
        this.enddatum = enddatum;
    }

    /**
     * Factory-Methode fuer einen Zeitraum ab heute.
     */
    public static Zeitraum abHeute(int tage) {
        if (tage < 0) {
            throw new IllegalArgumentException("Anzahl Tage darf nicht negativ sein");
        }
        LocalDate start = LocalDate.now();
        return new Zeitraum(start, start.plusDays(tage));
    }

    /**
     * Factory-Methode fuer einen Zeitraum ab einem bestimmten Datum.
     */
    public static Zeitraum ab(LocalDate start, int tage) {
        Objects.requireNonNull(start, "Startdatum darf nicht null sein");
        if (tage < 0) {
            throw new IllegalArgumentException("Anzahl Tage darf nicht negativ sein");
        }
        return new Zeitraum(start, start.plusDays(tage));
    }

    public LocalDate getStartdatum() {
        return startdatum;
    }

    public LocalDate getEnddatum() {
        return enddatum;
    }

    /**
     * Berechnet die Dauer des Zeitraums in Tagen.
     */
    public long getDauerInTagen() {
        return ChronoUnit.DAYS.between(startdatum, enddatum);
    }

    /**
     * Prueft, ob ein Datum innerhalb des Zeitraums liegt.
     */
    public boolean enthaelt(LocalDate datum) {
        Objects.requireNonNull(datum, "Datum darf nicht null sein");
        return !datum.isBefore(startdatum) && !datum.isAfter(enddatum);
    }

    /**
     * Prueft, ob das Enddatum ueberschritten ist.
     */
    public boolean istUeberfaellig() {
        return LocalDate.now().isAfter(enddatum);
    }

    /**
     * Berechnet die Anzahl der ueberfaelligen Tage.
     */
    public long getUeberfaelligeTage() {
        LocalDate heute = LocalDate.now();
        if (!heute.isAfter(enddatum)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(enddatum, heute);
    }

    /**
     * Prueft, ob dieser Zeitraum sich mit einem anderen ueberschneidet.
     */
    public boolean ueberschneidetSich(Zeitraum anderer) {
        Objects.requireNonNull(anderer, "Vergleichs-Zeitraum darf nicht null sein");
        return !this.enddatum.isBefore(anderer.startdatum) && !anderer.enddatum.isBefore(this.startdatum);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zeitraum zeitraum = (Zeitraum) o;
        return startdatum.equals(zeitraum.startdatum) && enddatum.equals(zeitraum.enddatum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startdatum, enddatum);
    }

    @Override
    public String toString() {
        return startdatum + " bis " + enddatum + " (" + getDauerInTagen() + " Tage)";
    }
}
