package de.dhbw.leihbar.domain.aggregates;

import de.dhbw.leihbar.domain.valueobjects.AusleiheStatus;
import de.dhbw.leihbar.domain.valueobjects.Zeitraum;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate Root fuer eine Ausleihe.
 * PRE-REFACTORING:
 * - Langer Konstruktor statt Builder Pattern (Code Smell: Long Parameter List)
 * - getZeitraum() -> wird spaeter zu getGeplanterZeitraum()
 * - getZustandsbericht() gibt Optional zurueck -> wird spaeter nullable String
 */
public class Ausleihe {

    private final UUID id;
    private final UUID gegenstandId;
    private final UUID ausleiherId;
    private final Zeitraum zeitraum;
    private final LocalDateTime erstelltAm;

    private AusleiheStatus status;
    private LocalDate tatsaechlichesRueckgabedatum;
    private String zustandsbericht;

    /**
     * Konstruktor fuer neue Ausleihe.
     * PRE-REFACTORING: Langer Konstruktor, wird spaeter durch Builder ersetzt.
     */
    public Ausleihe(UUID gegenstandId, UUID ausleiherId,
                    LocalDate ausleihdatum, LocalDate geplantesRueckgabedatum) {
        this.id = UUID.randomUUID();
        this.gegenstandId = Objects.requireNonNull(gegenstandId, "GegenstandId darf nicht null sein");
        this.ausleiherId = Objects.requireNonNull(ausleiherId, "AusleiherId darf nicht null sein");
        Objects.requireNonNull(ausleihdatum, "Ausleihdatum darf nicht null sein");
        Objects.requireNonNull(geplantesRueckgabedatum, "Rueckgabedatum darf nicht null sein");
        this.zeitraum = new Zeitraum(ausleihdatum, geplantesRueckgabedatum);
        this.erstelltAm = LocalDateTime.now();
        this.status = AusleiheStatus.AKTIV;
        this.tatsaechlichesRueckgabedatum = null;
        this.zustandsbericht = null;
    }

    /**
     * Konstruktor fuer die Wiederherstellung aus der Datenbank.
     */
    public Ausleihe(UUID id, UUID gegenstandId, UUID ausleiherId, Zeitraum zeitraum,
                    LocalDateTime erstelltAm, AusleiheStatus status,
                    LocalDate tatsaechlichesRueckgabedatum, String zustandsbericht) {
        this.id = Objects.requireNonNull(id);
        this.gegenstandId = Objects.requireNonNull(gegenstandId);
        this.ausleiherId = Objects.requireNonNull(ausleiherId);
        this.zeitraum = Objects.requireNonNull(zeitraum);
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

    /**
     * PRE-REFACTORING: Wird spaeter zu getGeplanterZeitraum() umbenannt.
     */
    public Zeitraum getZeitraum() {
        return zeitraum;
    }

    public LocalDate getAusleihdatum() {
        return zeitraum.getStartdatum();
    }

    public LocalDate getGeplantesRueckgabedatum() {
        return zeitraum.getEnddatum();
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

    /**
     * PRE-REFACTORING: Gibt Optional zurueck, wird spaeter zu nullable String.
     */
    public Optional<String> getZustandsbericht() {
        return Optional.ofNullable(zustandsbericht);
    }

    /**
     * Prueft, ob die Ausleihe noch aktiv ist.
     */
    public boolean istAktiv() {
        return status.istAktiv();
    }

    /**
     * Prueft, ob die Ausleihe ueberfaellig ist.
     */
    public boolean istUeberfaellig() {
        return istAktiv() && zeitraum.istUeberfaellig();
    }

    /**
     * Gibt die Anzahl der ueberfaelligen Tage zurueck.
     */
    public long getUeberfaelligeTage() {
        if (!istAktiv()) {
            return 0;
        }
        return zeitraum.getUeberfaelligeTage();
    }

    /**
     * Aktualisiert den Status auf UEBERFAELLIG.
     */
    public void aktualisiereUeberfaelligkeitsstatus() {
        if (status == AusleiheStatus.AKTIV && zeitraum.istUeberfaellig()) {
            this.status = AusleiheStatus.UEBERFAELLIG;
        }
    }

    /**
     * Erfasst die Rueckgabe des ausgeliehenen Gegenstandes.
     */
    public void zurueckgeben(String zustandsbericht) {
        if (!istAktiv()) {
            throw new IllegalStateException(
                "Ausleihe kann nicht zurueckgegeben werden. Aktueller Status: " + status
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
               ", zeitraum=" + zeitraum +
               ", status=" + status +
               '}';
    }
}
