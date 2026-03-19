package de.dhbw.leihbar.domain.repositories;

/**
 * Repository Interface: GegenstandRepository
 *
 * Definiert den Vertrag fuer den Datenzugriff auf Gegenstaende.
 * Das Interface liegt in der Domain-Schicht, die Implementierung
 * in der Infrastruktur-Schicht (Dependency Inversion Principle).
 *
 * Geplante Methoden:
 * - speichern(Gegenstand): Gegenstand     -> Neu anlegen oder aktualisieren
 * - findeNachId(UUID): Optional<Gegenstand> -> Einzelnen Gegenstand laden
 * - findeAlle(): List<Gegenstand>          -> Alle Gegenstaende
 * - findeVerfuegbare(): List<Gegenstand>   -> Nur ausleihbare
 * - findeNachStatus(Status): List<Gegenstand>
 * - findeNachKategorie(Kategorie): List<Gegenstand>
 * - suche(String): List<Gegenstand>        -> Volltextsuche in Name/Beschreibung
 * - loeschen(UUID): void
 * - zaehleAlle(): long
 * - zaehleNachStatus(Status): long
 *
 * Implementierung wird spaeter mit JPA/Hibernate gemacht.
 *
 * TODO: Interface-Methoden definieren
 * TODO: Domaenensprachliche Methodennamen (deutsch)
 */
public interface GegenstandRepository {
    // TODO: CRUD-Methoden
    // TODO: Spezielle Queries
    // TODO: Zaehler-Methoden
}
