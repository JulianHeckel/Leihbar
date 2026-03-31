package de.dhbw.leihbar.infrastructure.persistence;

import de.dhbw.leihbar.domain.aggregates.Ausleihe;
import de.dhbw.leihbar.domain.valueobjects.AusleiheStatus;
import de.dhbw.leihbar.domain.valueobjects.Zeitraum;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity für die Persistierung von Ausleihen.
 */
@Entity
@Table(name = "ausleihen")
public class AusleiheJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "gegenstand_id", nullable = false)
    private UUID gegenstandId;

    @Column(name = "ausleiher_id", nullable = false)
    private UUID ausleiherId;

    @Column(name = "ausleihdatum", nullable = false)
    private LocalDate ausleihdatum;

    @Column(name = "geplantes_rueckgabedatum", nullable = false)
    private LocalDate geplantesRueckgabedatum;

    @Column(name = "tatsaechliches_rueckgabedatum")
    private LocalDate tatsaechlichesRueckgabedatum;

    @Column(name = "erstellt_am", nullable = false)
    private LocalDateTime erstelltAm;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AusleiheStatus status;

    @Column(name = "zustandsbericht", length = 1000)
    private String zustandsbericht;

    public AusleiheJpaEntity() {
    }

    /**
     * Konvertiert ein Domain-Objekt in eine JPA-Entity.
     */
    public static AusleiheJpaEntity fromDomain(Ausleihe ausleihe) {
        AusleiheJpaEntity entity = new AusleiheJpaEntity();
        entity.id = ausleihe.getId();
        entity.gegenstandId = ausleihe.getGegenstandId();
        entity.ausleiherId = ausleihe.getAusleiherId();
        entity.ausleihdatum = ausleihe.getAusleihdatum();
        entity.geplantesRueckgabedatum = ausleihe.getGeplantesRueckgabedatum();
        entity.tatsaechlichesRueckgabedatum = ausleihe.getTatsaechlichesRueckgabedatum();
        entity.erstelltAm = ausleihe.getErstelltAm();
        entity.status = ausleihe.getStatus();
        entity.zustandsbericht = ausleihe.getZustandsbericht();
        return entity;
    }

    /**
     * Konvertiert die JPA-Entity in ein Domain-Objekt.
     */
    public Ausleihe toDomain() {
        Zeitraum zeitraum = new Zeitraum(ausleihdatum, geplantesRueckgabedatum);

        return new Ausleihe(
            id,
            gegenstandId,
            ausleiherId,
            zeitraum,
            erstelltAm,
            status,
            tatsaechlichesRueckgabedatum,
            zustandsbericht
        );
    }

    // Getter und Setter

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getGegenstandId() {
        return gegenstandId;
    }

    public void setGegenstandId(UUID gegenstandId) {
        this.gegenstandId = gegenstandId;
    }

    public UUID getAusleiherId() {
        return ausleiherId;
    }

    public void setAusleiherId(UUID ausleiherId) {
        this.ausleiherId = ausleiherId;
    }

    public LocalDate getAusleihdatum() {
        return ausleihdatum;
    }

    public void setAusleihdatum(LocalDate ausleihdatum) {
        this.ausleihdatum = ausleihdatum;
    }

    public LocalDate getGeplantesRueckgabedatum() {
        return geplantesRueckgabedatum;
    }

    public void setGeplantesRueckgabedatum(LocalDate geplantesRueckgabedatum) {
        this.geplantesRueckgabedatum = geplantesRueckgabedatum;
    }

    public LocalDate getTatsaechlichesRueckgabedatum() {
        return tatsaechlichesRueckgabedatum;
    }

    public void setTatsaechlichesRueckgabedatum(LocalDate tatsaechlichesRueckgabedatum) {
        this.tatsaechlichesRueckgabedatum = tatsaechlichesRueckgabedatum;
    }

    public LocalDateTime getErstelltAm() {
        return erstelltAm;
    }

    public void setErstelltAm(LocalDateTime erstelltAm) {
        this.erstelltAm = erstelltAm;
    }

    public AusleiheStatus getStatus() {
        return status;
    }

    public void setStatus(AusleiheStatus status) {
        this.status = status;
    }

    public String getZustandsbericht() {
        return zustandsbericht;
    }

    public void setZustandsbericht(String zustandsbericht) {
        this.zustandsbericht = zustandsbericht;
    }
}
