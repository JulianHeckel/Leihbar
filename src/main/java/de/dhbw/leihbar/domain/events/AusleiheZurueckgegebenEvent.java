package de.dhbw.leihbar.domain.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain Event, das ausgelöst wird, wenn eine Ausleihe zurückgegeben wird.
 * Immutable Record.
 */
public record AusleiheZurueckgegebenEvent(
    UUID eventId,
    Instant erstelltAm,
    UUID ausleiheId,
    UUID gegenstandId,
    LocalDate rueckgabedatum,
    boolean warUeberfaellig,
    long ueberfaelligeTage
) implements DomainEvent {

    public AusleiheZurueckgegebenEvent {
        Objects.requireNonNull(eventId, "EventId darf nicht null sein");
        Objects.requireNonNull(erstelltAm, "ErstelltAm darf nicht null sein");
        Objects.requireNonNull(ausleiheId, "AusleiheId darf nicht null sein");
        Objects.requireNonNull(gegenstandId, "GegenstandId darf nicht null sein");
        Objects.requireNonNull(rueckgabedatum, "Rueckgabedatum darf nicht null sein");
    }

    /**
     * Factory-Methode zur einfachen Erstellung.
     */
    public static AusleiheZurueckgegebenEvent of(UUID ausleiheId, UUID gegenstandId,
                                                   LocalDate rueckgabedatum,
                                                   boolean warUeberfaellig, long ueberfaelligeTage) {
        return new AusleiheZurueckgegebenEvent(
            UUID.randomUUID(),
            Instant.now(),
            ausleiheId,
            gegenstandId,
            rueckgabedatum,
            warUeberfaellig,
            ueberfaelligeTage
        );
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getErstelltAm() {
        return erstelltAm;
    }

    @Override
    public String getEventName() {
        return "AusleiheZurueckgegeben";
    }
}
