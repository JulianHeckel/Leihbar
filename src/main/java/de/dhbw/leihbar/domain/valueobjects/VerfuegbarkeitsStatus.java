package de.dhbw.leihbar.domain.valueobjects;

/**
 * Enum fuer den Verfuegbarkeitsstatus eines Gegenstandes.
 *
 * Geplante Werte:
 * - VERFUEGBAR:   Gegenstand kann ausgeliehen werden
 * - AUSGELIEHEN:  Gegenstand ist aktuell verliehen
 * - IN_WARTUNG:   Gegenstand temporaer gesperrt (Reparatur, Reinigung)
 * - AUSGEMUSTERT: Gegenstand endgueltig aus dem Bestand entfernt
 *
 * Jeder Status soll eine Bezeichnung (String) und einen
 * boolean istAusleihbar haben.
 *
 * Geplante Methoden:
 * - getBezeichnung(): String (menschenlesbare Bezeichnung)
 * - istAusleihbar(): boolean (nur VERFUEGBAR = true)
 * - toString(): gibt Bezeichnung zurueck
 *
 * Wird von Gegenstand.istVerfuegbar() genutzt.
 *
 * TODO: Enum-Konstanten mit Bezeichnung und ausleihbar-Flag
 * TODO: Konstruktor, Getter, toString
 */
public enum VerfuegbarkeitsStatus {
    // TODO: VERFUEGBAR("Verfuegbar", true),
    // TODO: AUSGELIEHEN("Ausgeliehen", false),
    // TODO: IN_WARTUNG("In Wartung", false),
    // TODO: AUSGEMUSTERT("Ausgemustert", false);
}
