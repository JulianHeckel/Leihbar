package de.dhbw.leihbar.domain.valueobjects;

/**
 * Value Object: Kontaktdaten (E-Mail + optionale Telefonnummer)
 *
 * Kapselt die Kontaktinformationen eines Ausleihers.
 * E-Mail ist Pflicht, Telefon ist optional.
 *
 * === Geplante Felder ===
 * - email: String        (Pflichtfeld, wird validiert)
 * - telefon: String      (optional, nullable)
 *
 * === Validierung ===
 * - E-Mail: Regex-Pattern fuer gueltiges Format
 * - Telefon: Optional, aber wenn vorhanden: Regex fuer Nummern
 *   Pattern: ^[+]?[0-9\s-]{6,20}$
 * - E-Mail wird normalisiert (lowercase, trim)
 *
 * === Geplante Methoden ===
 * - getEmail(): String
 * - getTelefon(): Optional<String>
 *      -> Ueberlegung: Optional oder nullable?
 *      -> Erstmal Optional, ist sicherer
 * - formatiert(): String (z.B. "email | telefon")
 *
 * === Factory Methoden ===
 * - static nurEmail(String email): Kontaktdaten
 * - static of(String email, String telefon): Kontaktdaten
 *
 * === Gleichheit ===
 * - equals/hashCode ueber email + telefon
 *
 * TODO: Felder, Konstruktor, Validierung
 * TODO: Factory Methoden
 * TODO: Getter
 * TODO: equals/hashCode/toString
 */
public final class Kontaktdaten {
    // TODO: Implementierung
}
