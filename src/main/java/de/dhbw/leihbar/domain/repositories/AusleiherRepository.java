package de.dhbw.leihbar.domain.repositories;

import de.dhbw.leihbar.domain.entities.Ausleiher;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository-Interface für Ausleiher.
 * Definiert die Schnittstelle für den Datenzugriff in der Domänensprache.
 */
public interface AusleiherRepository {

    /**
     * Speichert einen Ausleiher (neu oder aktualisiert).
     */
    Ausleiher speichern(Ausleiher ausleiher);

    /**
     * Findet einen Ausleiher anhand seiner ID.
     */
    Optional<Ausleiher> findeNachId(UUID id);

    /**
     * Findet einen Ausleiher anhand seiner E-Mail-Adresse.
     */
    Optional<Ausleiher> findeNachEmail(String email);

    /**
     * Gibt alle Ausleiher zurück.
     */
    List<Ausleiher> findeAlle();

    /**
     * Sucht Ausleiher anhand eines Suchbegriffs (Name oder E-Mail).
     */
    List<Ausleiher> suche(String suchbegriff);

    /**
     * Löscht einen Ausleiher.
     */
    void loeschen(UUID id);

    /**
     * Prüft, ob eine E-Mail-Adresse bereits existiert.
     */
    boolean existiertEmail(String email);

    /**
     * Zählt alle Ausleiher.
     */
    long zaehleAlle();
}
