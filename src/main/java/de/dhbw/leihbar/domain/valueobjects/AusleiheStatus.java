package de.dhbw.leihbar.domain.valueobjects;

/**
 * Enum fuer den Status einer Ausleihe.
 *
 * Geplante Werte:
 * - AKTIV:           Ausleihe laeuft, Gegenstand ist draussen
 * - ZURUECKGEGEBEN:  Gegenstand wurde ordnungsgemaess zurueckgegeben
 * - UEBERFAELLIG:    Rueckgabedatum ueberschritten, Gegenstand noch draussen
 * - STORNIERT:       Ausleihe wurde storniert (Fehler, Defekt, etc.)
 *
 * Geplante Methoden:
 * - getBezeichnung(): String
 * - istAktiv(): boolean -> true fuer AKTIV und UEBERFAELLIG
 *   (beides bedeutet: Gegenstand ist noch nicht zurueck)
 * - toString(): gibt Bezeichnung zurueck
 *
 * TODO: Enum-Konstanten mit Bezeichnung
 * TODO: istAktiv()-Logik: AKTIV || UEBERFAELLIG
 */
public enum AusleiheStatus {
    // TODO: AKTIV("Aktiv"),
    // TODO: ZURUECKGEGEBEN("Zurueckgegeben"),
    // TODO: UEBERFAELLIG("Ueberfaellig"),
    // TODO: STORNIERT("Storniert");
}
