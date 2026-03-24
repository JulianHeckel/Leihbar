package de.dhbw.leihbar.domain.aggregates;

import de.dhbw.leihbar.domain.valueobjects.Kategorie;
import de.dhbw.leihbar.domain.valueobjects.VerfuegbarkeitsStatus;

import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root fuer einen ausleihbaren Gegenstand.
 * PRE-REFACTORING:
 * - inventarnummer ist ein String (Primitive Obsession) -> wird zu InventarNummer VO
 * - bezeichnung -> wird spaeter zu name umbenannt
 * - getBezeichnung() -> wird zu getName()
 * - inWartung() -> wird zu inWartungSetzen()
 * - wartungAbschliessen() -> wird zu wartungBeenden()
 * - neu(bez,beschr,kat) -> wird zu neu(invNr,name,beschr,kat)
 */
public class Gegenstand {

    private final UUID id;
    private String inventarnummer;
    private String bezeichnung;
    private String beschreibung;
    private Kategorie kategorie;
    private VerfuegbarkeitsStatus status;

    public Gegenstand(UUID id, String inventarnummer, String bezeichnung,
                      String beschreibung, Kategorie kategorie) {
        this.id = Objects.requireNonNull(id, "ID darf nicht null sein");
        this.inventarnummer = inventarnummer != null ? inventarnummer : "";
        setBezeichnung(bezeichnung);
        this.beschreibung = beschreibung != null ? beschreibung.trim() : "";
        this.kategorie = Objects.requireNonNull(kategorie, "Kategorie darf nicht null sein");
        this.status = VerfuegbarkeitsStatus.VERFUEGBAR;
    }

    /**
     * Factory-Methode zur Erzeugung eines neuen Gegenstandes.
     * PRE-REFACTORING: Ohne InventarNummer-Parameter.
     */
    public static Gegenstand neu(String bezeichnung, String beschreibung, Kategorie kategorie) {
        return new Gegenstand(UUID.randomUUID(), "", bezeichnung, beschreibung, kategorie);
    }

    public UUID getId() {
        return id;
    }

    public String getInventarnummer() {
        return inventarnummer;
    }

    public void setInventarnummer(String inventarnummer) {
        this.inventarnummer = inventarnummer != null ? inventarnummer : "";
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    public void setBezeichnung(String bezeichnung) {
        Objects.requireNonNull(bezeichnung, "Bezeichnung darf nicht null sein");
        String normalized = bezeichnung.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Bezeichnung darf nicht leer sein");
        }
        if (normalized.length() > 200) {
            throw new IllegalArgumentException("Bezeichnung darf maximal 200 Zeichen haben");
        }
        this.bezeichnung = normalized;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung != null ? beschreibung.trim() : "";
    }

    public Kategorie getKategorie() {
        return kategorie;
    }

    public void setKategorie(Kategorie kategorie) {
        this.kategorie = Objects.requireNonNull(kategorie, "Kategorie darf nicht null sein");
    }

    public VerfuegbarkeitsStatus getStatus() {
        return status;
    }

    /**
     * Prueft, ob der Gegenstand aktuell ausgeliehen werden kann.
     */
    public boolean istVerfuegbar() {
        return status.istAusleihbar();
    }

    /**
     * Markiert den Gegenstand als ausgeliehen.
     */
    public void ausleihen() {
        if (!istVerfuegbar()) {
            throw new IllegalStateException(
                "Gegenstand '" + bezeichnung + "' kann nicht ausgeliehen werden. " +
                "Aktueller Status: " + status.getBezeichnung()
            );
        }
        this.status = VerfuegbarkeitsStatus.AUSGELIEHEN;
    }

    /**
     * Markiert den Gegenstand als zurueckgegeben.
     */
    public void zurueckgeben() {
        if (status != VerfuegbarkeitsStatus.AUSGELIEHEN) {
            throw new IllegalStateException(
                "Gegenstand '" + bezeichnung + "' ist nicht ausgeliehen und kann nicht zurueckgegeben werden. " +
                "Aktueller Status: " + status.getBezeichnung()
            );
        }
        this.status = VerfuegbarkeitsStatus.VERFUEGBAR;
    }

    /**
     * Setzt den Gegenstand in Wartung.
     * PRE-REFACTORING: Wird spaeter zu inWartungSetzen() umbenannt.
     */
    public void inWartung() {
        if (status == VerfuegbarkeitsStatus.AUSGELIEHEN) {
            throw new IllegalStateException(
                "Ausgeliehener Gegenstand kann nicht in Wartung gesetzt werden"
            );
        }
        this.status = VerfuegbarkeitsStatus.IN_WARTUNG;
    }

    /**
     * Beendet die Wartung.
     * PRE-REFACTORING: Wird spaeter zu wartungBeenden() umbenannt.
     */
    public void wartungAbschliessen() {
        if (status != VerfuegbarkeitsStatus.IN_WARTUNG) {
            throw new IllegalStateException("Gegenstand ist nicht in Wartung");
        }
        this.status = VerfuegbarkeitsStatus.VERFUEGBAR;
    }

    /**
     * Mustert den Gegenstand aus (endgueltig).
     */
    public void ausmustern() {
        if (status == VerfuegbarkeitsStatus.AUSGELIEHEN) {
            throw new IllegalStateException(
                "Ausgeliehener Gegenstand kann nicht ausgemustert werden"
            );
        }
        this.status = VerfuegbarkeitsStatus.AUSGEMUSTERT;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gegenstand that = (Gegenstand) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return inventarnummer + " - " + bezeichnung + " [" + status.getBezeichnung() + "]";
    }
}
