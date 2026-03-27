package de.dhbw.leihbar.domain.repositories;

import de.dhbw.leihbar.domain.aggregates.Gegenstand;
import de.dhbw.leihbar.domain.valueobjects.Kategorie;
import de.dhbw.leihbar.domain.valueobjects.VerfuegbarkeitsStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository-Interface fuer Gegenstaende.
 * PRE-REFACTORING: Ohne InventarNummer-spezifische Methoden.
 * Diese werden im Refactoring-Commit hinzugefuegt:
 * - findeNachInventarNummer(InventarNummer)
 * - existiertInventarNummer(InventarNummer)
 * - naechsteFreieInventarNummer()
 */
public interface GegenstandRepository {

    /**
     * Speichert einen Gegenstand (neu oder aktualisiert).
     */
    Gegenstand speichern(Gegenstand gegenstand);

    /**
     * Findet einen Gegenstand anhand seiner ID.
     */
    Optional<Gegenstand> findeNachId(UUID id);

    /**
     * Gibt alle Gegenstaende zurueck.
     */
    List<Gegenstand> findeAlle();

    /**
     * Findet alle Gegenstaende mit einem bestimmten Status.
     */
    List<Gegenstand> findeNachStatus(VerfuegbarkeitsStatus status);

    /**
     * Findet alle verfuegbaren Gegenstaende.
     */
    List<Gegenstand> findeVerfuegbare();

    /**
     * Findet alle Gegenstaende einer bestimmten Kategorie.
     */
    List<Gegenstand> findeNachKategorie(Kategorie kategorie);

    /**
     * Sucht Gegenstaende anhand eines Suchbegriffs.
     */
    List<Gegenstand> suche(String suchbegriff);

    /**
     * Loescht einen Gegenstand.
     */
    void loeschen(UUID id);

    /**
     * Zaehlt alle Gegenstaende.
     */
    long zaehleAlle();

    /**
     * Zaehlt alle Gegenstaende mit einem bestimmten Status.
     */
    long zaehleNachStatus(VerfuegbarkeitsStatus status);
}
