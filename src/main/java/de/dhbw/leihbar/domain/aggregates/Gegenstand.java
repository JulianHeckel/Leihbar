package de.dhbw.leihbar.domain.aggregates;

import java.util.UUID;

/**
 * Aggregate Root: Gegenstand (ausleihbarer Gegenstand)
 *
 * Repräsentiert einen physischen Gegenstand der ausgeliehen werden kann.
 * Als Aggregate Root kontrolliert diese Klasse alle Zustandsänderungen
 * und stellt die Konsistenz sicher.
 *
 * === Geplante Felder ===
 * - id: UUID                         -> Eindeutige Identität
 * - inventarnummer: String            -> Format "INV-XXXX" (z.B. INV-0001)
 *      ÜBERLEGUNG: Eigenes Value Object für Inventarnummer?
 *      + Validierung des Formats wäre gekapselt
 *      + Typsicherheit (kein versehentliches String-Mischen)
 *      - Mehr Komplexität, erstmal als String belassen
 *      -> Entscheidung: Vorerst String, ggf. später refactoren
 * - bezeichnung: String               -> Name des Gegenstands
 * - beschreibung: String              -> Optionale Beschreibung
 * - kategorie: Kategorie              -> Enum (WERKZEUG, ELEKTRONIK, etc.)
 * - status: VerfuegbarkeitsStatus     -> Aktueller Verfügbarkeitsstatus
 *
 * === Geplante Statusübergänge ===
 *
 *   [VERFUEGBAR] --ausleihen()--> [AUSGELIEHEN]
 *        |                              |
 *        |                        zurueckgeben()
 *        |                              |
 *        v                              v
 *   [IN_WARTUNG] <---wartungAbschliessen()
 *        |
 *   inWartung()
 *        |
 *        v
 *   [AUSGEMUSTERT]  (Endstatus, nicht umkehrbar)
 *
 * === Geplante Methoden ===
 * - static neu(bezeichnung, beschreibung, kategorie): Gegenstand
 *      Factory Method für neue Gegenstände
 *      Generiert automatisch UUID
 *      Startstatus: VERFUEGBAR
 *
 * - ausleihen(): void
 *      Vorbedingung: status == VERFUEGBAR
 *      Nachbedingung: status == AUSGELIEHEN
 *      Wirft: IllegalStateException bei ungültigem Status
 *
 * - zurueckgeben(): void
 *      Vorbedingung: status == AUSGELIEHEN
 *      Nachbedingung: status == VERFUEGBAR
 *
 * - inWartung(): void
 *      Vorbedingung: status == VERFUEGBAR
 *      Nachbedingung: status == IN_WARTUNG
 *
 * - wartungAbschliessen(): void
 *      Vorbedingung: status == IN_WARTUNG
 *      Nachbedingung: status == VERFUEGBAR
 *
 * - ausmustern(): void
 *      Vorbedingung: status != AUSGELIEHEN (kein Ausmustern während Ausleihe)
 *      Nachbedingung: status == AUSGEMUSTERT
 *      Hinweis: Endgültig, kann nicht rückgängig gemacht werden!
 *
 * - istVerfuegbar(): boolean
 *      Prüft ob status.istAusleihbar()
 *
 * - getBezeichnung(), getBeschreibung(), getKategorie(), getStatus(): Getter
 *
 * === Invarianten ===
 * - bezeichnung darf nicht null oder leer sein
 * - inventarnummer muss dem Format INV-XXXX entsprechen
 * - Eindeutigkeit der Inventarnummer wird via Repository geprüft
 * - Statusübergänge nur gemäß obigem Diagramm erlaubt
 * - Ausgemustert ist endgültig (kein Weg zurück)
 *
 * === Gleichheit ===
 * - equals/hashCode basierend auf id (Entity-Semantik, nicht Wert-Semantik)
 */
public class Gegenstand {

    // TODO: Felder definieren
    // private UUID id;
    // private String inventarnummer;  // Format: INV-XXXX
    // private String bezeichnung;
    // private String beschreibung;
    // private Kategorie kategorie;    // -> Enum mit Kategorien
    // private VerfuegbarkeitsStatus status;

    // TODO: Privater Konstruktor (nur über Factory Method erzeugen)
    // TODO: Zweiter Konstruktor für Rekonstruktion aus DB (mit bestehender ID)

    // TODO: Factory Method
    // public static Gegenstand neu(String bezeichnung, String beschreibung, Kategorie kategorie) {
    //     - UUID generieren
    //     - Inventarnummer: erstmal leer, wird vom Repository vergeben?
    //       Oder: Inventarnummer als Parameter? -> Muss ich nochmal überlegen
    //     - Status auf VERFUEGBAR setzen
    //     - Validierung: bezeichnung nicht leer
    // }

    // TODO: Statusübergänge (jeder prüft Vorbedingung)

    // TODO: Getter (getBezeichnung, getBeschreibung, etc.)

    // TODO: equals/hashCode über id

    // TODO: toString für Debugging
}
