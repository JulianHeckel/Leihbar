package de.dhbw.leihbar.domain.aggregates;

import java.util.UUID;

/**
 * Aggregate: Ausleihe (ein Ausleih-Vorgang)
 *
 * Repräsentiert den Vorgang, dass ein Ausleiher einen Gegenstand für
 * einen bestimmten Zeitraum ausleiht. Verwaltet den Lebenszyklus
 * einer Ausleihe von der Erstellung bis zur Rückgabe.
 *
 * === Geplante Felder ===
 * - id: UUID                              -> Eindeutige Identität
 * - gegenstandId: UUID                    -> Referenz auf Gegenstand (über ID, nicht Objekt!)
 *      Hinweis: Aggregates referenzieren einander NUR über IDs,
 *      nicht über direkte Objektreferenzen (DDD-Prinzip)
 * - ausleiherId: UUID                     -> Referenz auf Ausleiher
 * - ausleihdatum: LocalDate               -> Wann wurde ausgeliehen
 * - geplantesRueckgabedatum: LocalDate    -> Wann soll zurückgegeben werden
 * - tatsaechlichesRueckgabedatum: LocalDate -> Wann wurde tatsächlich zurückgegeben (nullable)
 * - status: AusleiheStatus                -> AKTIV, ZURUECKGEGEBEN, UEBERFAELLIG, STORNIERT
 * - zustandsbericht: String               -> Optionaler Bericht bei Rückgabe
 * - erstelltAm: LocalDateTime             -> Zeitstempel der Erstellung
 *
 * === Geplanter Konstruktor ===
 * public Ausleihe(UUID gegenstandId, UUID ausleiherId,
 *                 LocalDate ausleihdatum, LocalDate geplantesRueckgabedatum,
 *                 String notiz)
 *
 * HINWEIS: Das sind viele Parameter... Wenn es zu unübersichtlich wird,
 * evtl. Builder Pattern in Betracht ziehen?
 * Erstmal so implementieren und schauen ob es handhabbar bleibt.
 *
 * === Geplante Methoden ===
 * - zurueckgeben(String zustandsbericht): void
 *      - Setzt tatsaechlichesRueckgabedatum auf heute
 *      - Speichert Zustandsbericht
 *      - Status -> ZURUECKGEGEBEN
 *      - Vorbedingung: istAktiv() == true
 *
 * - stornieren(): void
 *      - Status -> STORNIERT
 *      - Vorbedingung: istAktiv() == true
 *      - Anwendungsfall: Fehlerhafte Ausleihe, Gegenstand defekt etc.
 *
 * - aktualisiereUeberfaelligkeitsstatus(): void
 *      - Prüft ob geplantesRueckgabedatum überschritten
 *      - Falls ja und Status == AKTIV: Status -> UEBERFAELLIG
 *      - Wird regelmäßig vom UeberfaelligkeitService aufgerufen
 *
 * - istAktiv(): boolean
 *      - true wenn status == AKTIV oder UEBERFAELLIG
 *      - (beides bedeutet: Gegenstand ist noch draußen)
 *
 * - istUeberfaellig(): boolean
 *      - Prüft ob Rückgabedatum überschritten UND noch nicht zurückgegeben
 *
 * - getUeberfaelligeTage(): long
 *      - Anzahl Tage über dem geplanten Rückgabedatum
 *      - 0 wenn nicht überfällig
 *
 * - getZeitraum(): Zeitraum
 *      - Gibt den geplanten Ausleihzeitraum als Value Object zurück
 *
 * - getZustandsbericht(): Optional<String>
 *      - Optionaler Zustandsbericht nach Rückgabe
 *
 * === Invarianten ===
 * - gegenstandId und ausleiherId dürfen nicht null sein
 * - geplantesRueckgabedatum muss nach ausleihdatum liegen
 * - Rückgabe und Stornierung nur bei aktiven Ausleihen möglich
 * - Nach Rückgabe/Stornierung sind keine weiteren Statusänderungen erlaubt
 *
 * === Domain Events (geplant) ===
 * - AusleiheErstelltEvent: Nach Erstellung einer neuen Ausleihe
 * - AusleiheZurueckgegebenEvent: Nach erfolgreicher Rückgabe
 * -> Events als Java Records implementieren
 *
 * === Gleichheit ===
 * - equals/hashCode über id
 */
public class Ausleihe {

    // TODO: Felder definieren (siehe oben)

    // TODO: Konstruktor mit Validierung
    // Ausleihe(UUID gegenstandId, UUID ausleiherId, LocalDate start, LocalDate ende, String notiz)
    // - Alle Pflichtfelder prüfen (requireNonNull)
    // - Ende nach Start prüfen
    // - ID generieren
    // - Status auf AKTIV setzen
    // - erstelltAm auf jetzt setzen

    // TODO: Zweiter Konstruktor für DB-Rekonstruktion (mit bestehender ID und Status)

    // TODO: Methoden implementieren (siehe Javadoc oben)

    // TODO: equals/hashCode über id
}
