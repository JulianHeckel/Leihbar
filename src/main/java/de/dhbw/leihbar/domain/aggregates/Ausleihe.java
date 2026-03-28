package de.dhbw.leihbar.domain.aggregates;

import de.dhbw.leihbar.domain.entities.Ausleiher;
import de.dhbw.leihbar.domain.valueobjects.AusleiheStatus;
import de.dhbw.leihbar.domain.valueobjects.Zeitraum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root für eine Ausleihe.
 * Verwaltet den vollständigen Lebenszyklus einer Ausleihe
 * von der Erstellung bis zur Rückgabe.
 *
 * Verwendet das Builder-Pattern für die Konstruktion.
 */
public class Ausleihe {

    private final UUID id;
    private final UUID gegenstandId;
    private final UUID ausleiherId;
    private final Zeitraum geplanterzeitraum;
    private final LocalDateTime erstelltAm;

    private AusleiheStatus status;
    private LocalDate tatsaechlichesRueckgabedatum;
    private String zustandsbericht;

    private Ausleihe(Builder builder) {
        this.id = UUID.randomUUID();
        this.gegenstandId = Objects.requireNonNull(builder.gegenstandId, "GegenstandId darf nicht null sein");
        this.ausleiherId = Objects.requireNonNull(builder.ausleiherId, "AusleiherId darf nicht null sein");
        this.geplanterzeitraum = Objects.requireNonNull(builder.zeitraum, "Zeitraum darf nicht null sein");
        this.erstelltAm = LocalDateTime.now();
        this.status = AusleiheStatus.AKTIV;
        this.tatsaechlichesRueckgabedatum = null;
        this.zustandsbericht = null;
    }

    /**
     * Konstruktor für die Wiederherstellung aus der Datenbank.
     */
    public Ausleihe(UUID id, UUID gegenstandId, UUID ausleiherId, Zeitraum zeitraum,
                    LocalDateTime erstelltAm, AusleiheStatus status,
                    LocalDate tatsaechlichesRueckgabedatum, String zustandsbericht) {
        this.id = Objects.requireNonNull(id);
        this.gegenstandId = Objects.requireNonNull(gegenstandId);
        this.ausleiherId = Objects.requireNonNull(ausleiherId);
        this.geplanterzeitraum = Objects.requireNonNull(zeitraum);
        this.erstelltAm = Objects.requireNonNull(erstelltAm);
        this.status = Objects.requireNonNull(status);
        this.tatsaechlichesRueckgabedatum = tatsaechlichesRueckgabedatum;
        this.zustandsbericht = zustandsbericht;
    }

    public UUID getId() {
        return id;
    }

    public UUID getGegenstandId() {
        return gegenstandId;
    }

    public UUID getAusleiherId() {
        return ausleiherId;
    }

    public Zeitraum getGeplanterZeitraum() {
        return geplanterzeitraum;
    }

    public LocalDate getAusleihdatum() {
        return geplanterzeitraum.getVon();
    }

    public LocalDate getGeplantesRueckgabedatum() {
        return geplanterzeitraum.getBis();
    }

    public LocalDateTime getErstelltAm() {
        return erstelltAm;
    }

    public AusleiheStatus getStatus() {
        return status;
    }

    public LocalDate getTatsaechlichesRueckgabedatum() {
        return tatsaechlichesRueckgabedatum;
    }

    public String getZustandsbericht() {
        return zustandsbericht;
    }

    /**
     * Prüft, ob die Ausleihe noch aktiv ist.
     */
    public boolean istAktiv() {
        return status.istAktiv();
    }

    /**
     * Prüft, ob die Ausleihe überfällig ist.
     */
    public boolean istUeberfaellig() {
        return istAktiv() && geplanterzeitraum.istUeberfaellig();
    }

    /**
     * Gibt die Anzahl der überfälligen Tage zurück.
     */
    public long getUeberfaelligeTage() {
        if (!istAktiv()) {
            return 0;
        }
        return geplanterzeitraum.getUeberfaelligeTage();
    }

    /**
     * Aktualisiert den Status auf UEBERFAELLIG, wenn das Rückgabedatum überschritten ist.
     */
    public void aktualisiereUeberfaelligkeitsstatus() {
        if (status == AusleiheStatus.AKTIV && geplanterzeitraum.istUeberfaellig()) {
            this.status = AusleiheStatus.UEBERFAELLIG;
        }
    }

    /**
     * Erfasst die Rückgabe des ausgeliehenen Gegenstandes.
     */
    public void zurueckgeben(String zustandsbericht) {
        if (!istAktiv()) {
            throw new IllegalStateException(
                "Ausleihe kann nicht zurückgegeben werden. Aktueller Status: " + status
            );
        }
        this.tatsaechlichesRueckgabedatum = LocalDate.now();
        this.zustandsbericht = zustandsbericht;
        this.status = AusleiheStatus.ZURUECKGEGEBEN;
    }

    /**
     * Storniert die Ausleihe.
     */
    public void stornieren() {
        if (!istAktiv()) {
            throw new IllegalStateException(
                "Ausleihe kann nicht storniert werden. Aktueller Status: " + status
            );
        }
        this.status = AusleiheStatus.STORNIERT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ausleihe ausleihe = (Ausleihe) o;
        return id.equals(ausleihe.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Ausleihe{" +
               "id=" + id +
               ", gegenstandId=" + gegenstandId +
               ", ausleiherId=" + ausleiherId +
               ", zeitraum=" + geplanterzeitraum +
               ", status=" + status +
               '}';
    }

    /**
     * Builder für die Erstellung einer neuen Ausleihe.
     * Implementiert das Builder-Entwurfsmuster.
     */
    public static class Builder {
        private UUID gegenstandId;
        private UUID ausleiherId;
        private Zeitraum zeitraum;

        public Builder() {
        }

        public Builder gegenstand(Gegenstand gegenstand) {
            Objects.requireNonNull(gegenstand, "Gegenstand darf nicht null sein");
            this.gegenstandId = gegenstand.getId();
            return this;
        }

        public Builder gegenstandId(UUID gegenstandId) {
            this.gegenstandId = gegenstandId;
            return this;
        }

        public Builder ausleiher(Ausleiher ausleiher) {
            Objects.requireNonNull(ausleiher, "Ausleiher darf nicht null sein");
            this.ausleiherId = ausleiher.getId();
            return this;
        }

        public Builder ausleiherId(UUID ausleiherId) {
            this.ausleiherId = ausleiherId;
            return this;
        }

        public Builder zeitraum(Zeitraum zeitraum) {
            this.zeitraum = zeitraum;
            return this;
        }

        public Builder fuer(int tage) {
            this.zeitraum = Zeitraum.abHeute(tage);
            return this;
        }

        public Builder von(LocalDate von) {
            // Temporär speichern, bis 'bis' gesetzt wird
            if (this.zeitraum != null) {
                this.zeitraum = new Zeitraum(von, this.zeitraum.getBis());
            }
            return this;
        }

        public Builder bis(LocalDate bis) {
            LocalDate von = this.zeitraum != null ? this.zeitraum.getVon() : LocalDate.now();
            this.zeitraum = new Zeitraum(von, bis);
            return this;
        }

        public Ausleihe build() {
            return new Ausleihe(this);
        }
    }
}
