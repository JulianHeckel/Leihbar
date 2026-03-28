package de.dhbw.leihbar.domain.aggregates;

import de.dhbw.leihbar.domain.valueobjects.InventarNummer;
import de.dhbw.leihbar.domain.valueobjects.Kategorie;
import de.dhbw.leihbar.domain.valueobjects.VerfuegbarkeitsStatus;

import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root für einen ausleihbaren Gegenstand.
 * Der Gegenstand kontrolliert seinen eigenen Zustand und Lebenszyklus.
 */
public class Gegenstand {

    private final UUID id;
    private final InventarNummer inventarNummer;
    private String name;
    private String beschreibung;
    private Kategorie kategorie;
    private VerfuegbarkeitsStatus status;

    public Gegenstand(UUID id, InventarNummer inventarNummer, String name,
                      String beschreibung, Kategorie kategorie) {
        this.id = Objects.requireNonNull(id, "ID darf nicht null sein");
        this.inventarNummer = Objects.requireNonNull(inventarNummer, "Inventarnummer darf nicht null sein");
        setName(name);
        this.beschreibung = beschreibung != null ? beschreibung.trim() : "";
        this.kategorie = Objects.requireNonNull(kategorie, "Kategorie darf nicht null sein");
        this.status = VerfuegbarkeitsStatus.VERFUEGBAR;
    }

    /**
     * Factory-Methode zur Erzeugung eines neuen Gegenstandes.
     */
    public static Gegenstand neu(InventarNummer inventarNummer, String name,
                                  String beschreibung, Kategorie kategorie) {
        return new Gegenstand(UUID.randomUUID(), inventarNummer, name, beschreibung, kategorie);
    }

    public UUID getId() {
        return id;
    }

    public InventarNummer getInventarNummer() {
        return inventarNummer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Objects.requireNonNull(name, "Name darf nicht null sein");
        String normalized = name.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Name darf nicht leer sein");
        }
        if (normalized.length() > 200) {
            throw new IllegalArgumentException("Name darf maximal 200 Zeichen haben");
        }
        this.name = normalized;
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
     * Prüft, ob der Gegenstand aktuell ausgeliehen werden kann.
     */
    public boolean istVerfuegbar() {
        return status.istAusleihbar();
    }

    /**
     * Markiert den Gegenstand als ausgeliehen.
     * Wirft eine Exception, wenn der Gegenstand nicht verfügbar ist.
     */
    public void ausleihen() {
        if (!istVerfuegbar()) {
            throw new IllegalStateException(
                "Gegenstand '" + name + "' kann nicht ausgeliehen werden. " +
                "Aktueller Status: " + status.getBezeichnung()
            );
        }
        this.status = VerfuegbarkeitsStatus.AUSGELIEHEN;
    }

    /**
     * Markiert den Gegenstand als zurückgegeben und wieder verfügbar.
     */
    public void zurueckgeben() {
        if (status != VerfuegbarkeitsStatus.AUSGELIEHEN) {
            throw new IllegalStateException(
                "Gegenstand '" + name + "' ist nicht ausgeliehen und kann nicht zurückgegeben werden. " +
                "Aktueller Status: " + status.getBezeichnung()
            );
        }
        this.status = VerfuegbarkeitsStatus.VERFUEGBAR;
    }

    /**
     * Setzt den Gegenstand in den Wartungsstatus.
     */
    public void inWartungSetzen() {
        if (status == VerfuegbarkeitsStatus.AUSGELIEHEN) {
            throw new IllegalStateException(
                "Ausgeliehener Gegenstand kann nicht in Wartung gesetzt werden"
            );
        }
        this.status = VerfuegbarkeitsStatus.IN_WARTUNG;
    }

    /**
     * Beendet die Wartung und macht den Gegenstand wieder verfügbar.
     */
    public void wartungBeenden() {
        if (status != VerfuegbarkeitsStatus.IN_WARTUNG) {
            throw new IllegalStateException("Gegenstand ist nicht in Wartung");
        }
        this.status = VerfuegbarkeitsStatus.VERFUEGBAR;
    }

    /**
     * Mustert den Gegenstand aus (endgültig).
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
        return inventarNummer + " - " + name + " [" + status.getBezeichnung() + "]";
    }
}
