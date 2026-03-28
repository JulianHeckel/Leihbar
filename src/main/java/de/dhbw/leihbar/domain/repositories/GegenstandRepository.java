package de.dhbw.leihbar.domain.repositories;

import de.dhbw.leihbar.domain.aggregates.Gegenstand;
import de.dhbw.leihbar.domain.valueobjects.InventarNummer;
import de.dhbw.leihbar.domain.valueobjects.Kategorie;
import de.dhbw.leihbar.domain.valueobjects.VerfuegbarkeitsStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository-Interface für Gegenstände.
 * Definiert die Schnittstelle für den Datenzugriff in der Domänensprache.
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
     * Findet einen Gegenstand anhand seiner Inventarnummer.
     */
    Optional<Gegenstand> findeNachInventarNummer(InventarNummer inventarNummer);

    /**
     * Gibt alle Gegenstände zurück.
     */
    List<Gegenstand> findeAlle();

    /**
     * Findet alle Gegenstände mit einem bestimmten Status.
     */
    List<Gegenstand> findeNachStatus(VerfuegbarkeitsStatus status);

    /**
     * Findet alle verfügbaren Gegenstände (die ausgeliehen werden können).
     */
    List<Gegenstand> findeVerfuegbare();

    /**
     * Findet alle Gegenstände einer bestimmten Kategorie.
     */
    List<Gegenstand> findeNachKategorie(Kategorie kategorie);

    /**
     * Sucht Gegenstände anhand eines Suchbegriffs (Name oder Beschreibung).
     */
    List<Gegenstand> suche(String suchbegriff);

    /**
     * Löscht einen Gegenstand.
     */
    void loeschen(UUID id);

    /**
     * Prüft, ob eine Inventarnummer bereits existiert.
     */
    boolean existiertInventarNummer(InventarNummer inventarNummer);

    /**
     * Ermittelt die nächste freie Inventarnummer.
     */
    InventarNummer naechsteFreieInventarNummer();

    /**
     * Zählt alle Gegenstände.
     */
    long zaehleAlle();

    /**
     * Zählt alle Gegenstände mit einem bestimmten Status.
     */
    long zaehleNachStatus(VerfuegbarkeitsStatus status);
}
