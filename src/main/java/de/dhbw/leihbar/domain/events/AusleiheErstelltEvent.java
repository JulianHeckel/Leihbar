package de.dhbw.leihbar.domain.events;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Domain Event, das ausgelöst wird, wenn eine neue Ausleihe erstellt wird.
 * Immutable Record.
 */
public record AusleiheErstelltEvent(
    UUID eventId,
    Instant erstelltAm,
    UUID ausleiheId,
    UUID gegenstandId,
    UUID ausleiherId,
    LocalDate ausleihdatum,
    LocalDate geplantesRueckgabedatum
) implements DomainEvent {

    public AusleiheErstelltEvent {
        Objects.requireNonNull(eventId, "EventId darf nicht null sein");
        Objects.requireNonNull(erstelltAm, "ErstelltAm darf nicht null sein");
        Objects.requireNonNull(ausleiheId, "AusleiheId darf nicht null sein");
        Objects.requireNonNull(gegenstandId, "GegenstandId darf nicht null sein");
        Objects.requireNonNull(ausleiherId, "AusleiherId darf nicht null sein");
        Objects.requireNonNull(ausleihdatum, "Ausleihdatum darf nicht null sein");
        Objects.requireNonNull(geplantesRueckgabedatum, "GeplantesRueckgabedatum darf nicht null sein");
    }

    /**
     * Factory-Methode zur einfachen Erstellung.
     */
    public static AusleiheErstelltEvent of(UUID ausleiheId, UUID gegenstandId, UUID ausleiherId,
                                            LocalDate ausleihdatum, LocalDate geplantesRueckgabedatum) {
        return new AusleiheErstelltEvent(
            UUID.randomUUID(),
            Instant.now(),
            ausleiheId,
            gegenstandId,
            ausleiherId,
            ausleihdatum,
            geplantesRueckgabedatum
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
        return "AusleiheErstellt";
    }
}
