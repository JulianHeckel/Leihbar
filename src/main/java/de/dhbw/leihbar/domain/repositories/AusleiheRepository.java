package de.dhbw.leihbar.domain.repositories;

/**
 * Repository Interface: AusleiheRepository
 *
 * Datenzugriff fuer Ausleihen. Liegt in der Domain-Schicht.
 *
 * Geplante Methoden:
 * - speichern(Ausleihe): Ausleihe
 * - findeNachId(UUID): Optional<Ausleihe>
 * - findeAlle(): List<Ausleihe>
 * - findeAktive(): List<Ausleihe>            -> Nur laufende Ausleihen
 * - findeUeberfaellige(): List<Ausleihe>     -> Ueberfaellige Ausleihen
 * - findeNachGegenstandId(UUID): List<Ausleihe>  -> Historie eines Gegenstands
 * - findeNachAusleiherId(UUID): List<Ausleihe>   -> Ausleihen einer Person
 * - findeAktiveAusleiheVonGegenstand(UUID): Optional<Ausleihe>
 * - loeschen(UUID): void
 * - zaehleAlle(): long
 * - zaehleUeberfaellige(): long
 *
 * TODO: Interface-Methoden definieren
 */
public interface AusleiheRepository {
    // TODO: CRUD-Methoden
    // TODO: Status-basierte Queries
    // TODO: Beziehungs-Queries (nach Gegenstand, nach Ausleiher)
}
