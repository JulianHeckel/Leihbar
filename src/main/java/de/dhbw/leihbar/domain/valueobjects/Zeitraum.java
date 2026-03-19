package de.dhbw.leihbar.domain.valueobjects;

/**
 * Value Object: Zeitraum (Ausleih-Zeitraum)
 *
 * Repraesentiert einen Zeitraum mit Start- und Enddatum.
 * Wird fuer Ausleihen verwendet um den geplanten Ausleihzeitraum abzubilden.
 *
 * === Geplante Felder ===
 * - startdatum: LocalDate  (Beginn des Zeitraums)
 * - enddatum: LocalDate    (Ende des Zeitraums)
 *
 * === Immutabilitaet ===
 * - final class, final fields
 * - Keine Setter, nur Getter
 * - Operationen geben neue Instanzen zurueck
 *
 * === Geplante Methoden ===
 * - getStartdatum(): LocalDate
 * - getEnddatum(): LocalDate
 * - getDauerInTagen(): long (ChronoUnit.DAYS.between)
 * - enthaelt(LocalDate): boolean (liegt Datum im Zeitraum?)
 * - istUeberfaellig(): boolean (ist enddatum < heute?)
 * - getUeberfaelligeTage(): long (Tage ueber enddatum hinaus)
 * - ueberschneidetSich(Zeitraum): boolean (ueberlappen sich zwei Zeitraeume?)
 *
 * === Factory Methoden ===
 * - static abHeute(int tage): Zeitraum ab heute fuer X Tage
 * - static ab(LocalDate start, int tage): Zeitraum ab Datum fuer X Tage
 *
 * === Validierung ===
 * - startdatum und enddatum duerfen nicht null sein
 * - enddatum muss >= startdatum sein
 *
 * === Gleichheit ===
 * - equals/hashCode ueber startdatum + enddatum (Wert-Semantik)
 *
 * TODO: Felder, Konstruktor mit Validierung
 * TODO: Getter, Berechnungsmethoden
 * TODO: Factory Methoden
 * TODO: equals/hashCode/toString
 */
public final class Zeitraum {
    // TODO: Implementierung
}
