package de.dhbw.leihbar.domain.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Basis-Interface für alle Domain Events.
 * Domain Events repräsentieren fachliche Ereignisse, die in der Vergangenheit passiert sind.
 */
public interface DomainEvent {

    /**
     * Eindeutige ID des Events.
     */
    UUID getEventId();

    /**
     * Zeitpunkt, zu dem das Event erstellt wurde.
     */
    Instant getErstelltAm();

    /**
     * Name des Events (z.B. "GegenstandAusgeliehen").
     */
    String getEventName();
}
