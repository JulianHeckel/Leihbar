package de.dhbw.leihbar.domain.entities;

import java.util.UUID;

/**
 * Entity: Ausleiher (Person die Gegenstände ausleiht)
 *
 * Repräsentiert eine Person im System, die Gegenstände ausleihen kann.
 * Als Entity wird die Gleichheit über die ID bestimmt, nicht über die Attribute.
 *
 * === Geplante Felder ===
 * - id: UUID                     -> Eindeutige Identität
 * - vorname: String              -> Vorname der Person
 * - nachname: String             -> Nachname der Person
 * - kontaktdaten: Kontaktdaten   -> Value Object mit Email + Telefon
 *
 * === Geplante Methoden ===
 * - static neu(vorname, nachname, kontaktdaten): Ausleiher
 *      Factory Method für neue Ausleiher
 *      Generiert UUID automatisch
 *
 * - getVollstaendigerName(): String
 *      Gibt "Vorname Nachname" zurück
 *      Convenience-Methode für UI-Anzeige
 *
 * - setVorname(String): void
 *      Mit Validierung: nicht null/leer
 *
 * - setNachname(String): void
 *      Mit Validierung: nicht null/leer
 *
 * - setKontaktdaten(Kontaktdaten): void
 *      Mit Validierung: nicht null
 *
 * === Unterschied Entity vs. Value Object ===
 * Entity:
 * - Hat eine Identität (UUID)
 * - Gleichheit über ID
 * - Kann sich verändern (Name, Kontaktdaten änderbar)
 * - Lebenszyklus (wird erstellt, geändert, ggf. gelöscht)
 *
 * Value Object (zum Vergleich):
 * - Keine eigene Identität
 * - Gleichheit über alle Attribute
 * - Immutable (nicht veränderbar)
 * - Wird komplett ersetzt statt geändert
 *
 * === Invarianten ===
 * - Vorname und Nachname dürfen nicht null oder leer sein
 * - Kontaktdaten müssen gültig sein (Validierung im Kontaktdaten-VO)
 * - ID wird einmal vergeben und ändert sich nie
 *
 * === Gleichheit ===
 * - equals/hashCode basierend auf id (Entity-Semantik)
 */
public class Ausleiher {

    // TODO: Felder definieren
    // private UUID id;
    // private String vorname;
    // private String nachname;
    // private Kontaktdaten kontaktdaten;

    // TODO: Privater Konstruktor + Factory Method neu()
    // TODO: Zweiter Konstruktor für DB-Rekonstruktion (mit bestehender ID)

    // TODO: getVollstaendigerName() -> "Vorname Nachname"

    // TODO: Setter mit Validierung

    // TODO: Getter

    // TODO: equals/hashCode über id
}
