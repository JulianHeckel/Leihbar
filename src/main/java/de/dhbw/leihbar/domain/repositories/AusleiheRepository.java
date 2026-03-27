package de.dhbw.leihbar.domain.repositories;

import de.dhbw.leihbar.domain.aggregates.Ausleihe;
import de.dhbw.leihbar.domain.valueobjects.AusleiheStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository-Interface für Ausleihen.
 * Definiert die Schnittstelle für den Datenzugriff in der Domänensprache.
 */
public interface AusleiheRepository {

    /**
     * Speichert eine Ausleihe (neu oder aktualisiert).
     */
    Ausleihe speichern(Ausleihe ausleihe);

    /**
     * Findet eine Ausleihe anhand ihrer ID.
     */
    Optional<Ausleihe> findeNachId(UUID id);

    /**
     * Gibt alle Ausleihen zurück.
     */
    List<Ausleihe> findeAlle();

    /**
     * Findet alle Ausleihen mit einem bestimmten Status.
     */
    List<Ausleihe> findeNachStatus(AusleiheStatus status);

    /**
     * Findet alle aktiven Ausleihen (AKTIV oder UEBERFAELLIG).
     */
    List<Ausleihe> findeAktive();

    /**
     * Findet alle überfälligen Ausleihen.
     */
    List<Ausleihe> findeUeberfaellige();

    /**
     * Findet alle Ausleihen für einen bestimmten Gegenstand.
     */
    List<Ausleihe> findeNachGegenstandId(UUID gegenstandId);

    /**
     * Findet alle Ausleihen eines bestimmten Ausleihers.
     */
    List<Ausleihe> findeNachAusleiherId(UUID ausleiherId);

    /**
     * Findet die aktuelle aktive Ausleihe für einen Gegenstand (falls vorhanden).
     */
    Optional<Ausleihe> findeAktiveAusleiheVonGegenstand(UUID gegenstandId);

    /**
     * Löscht eine Ausleihe.
     */
    void loeschen(UUID id);

    /**
     * Zählt alle Ausleihen.
     */
    long zaehleAlle();

    /**
     * Zählt alle Ausleihen mit einem bestimmten Status.
     */
    long zaehleNachStatus(AusleiheStatus status);

    /**
     * Zählt überfällige Ausleihen.
     */
    long zaehleUeberfaellige();
}
