package de.dhbw.leihbar.domain.repositories;

/**
 * Repository Interface: AusleiherRepository
 *
 * Datenzugriff fuer Ausleiher (Personen). Liegt in der Domain-Schicht.
 *
 * Geplante Methoden:
 * - speichern(Ausleiher): Ausleiher
 * - findeNachId(UUID): Optional<Ausleiher>
 * - findeAlle(): List<Ausleiher>
 * - findeNachEmail(String): Optional<Ausleiher>  -> Email ist eindeutig
 * - suche(String): List<Ausleiher>               -> Suche in Name/Email
 * - existiertEmail(String): boolean               -> Duplikat-Pruefung
 * - loeschen(UUID): void
 * - zaehleAlle(): long
 *
 * TODO: Interface-Methoden definieren
 */
public interface AusleiherRepository {
    // TODO: CRUD-Methoden
    // TODO: Email-basierte Queries
}
