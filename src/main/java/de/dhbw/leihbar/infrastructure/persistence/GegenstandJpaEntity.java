package de.dhbw.leihbar.infrastructure.persistence;

import de.dhbw.leihbar.domain.aggregates.Gegenstand;
import de.dhbw.leihbar.domain.valueobjects.InventarNummer;
import de.dhbw.leihbar.domain.valueobjects.Kategorie;
import de.dhbw.leihbar.domain.valueobjects.VerfuegbarkeitsStatus;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * JPA Entity für die Persistierung von Gegenständen.
 * Dient als Mapping zwischen Domain-Objekt und Datenbank.
 */
@Entity
@Table(name = "gegenstaende")
public class GegenstandJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "inventar_nummer", nullable = false, unique = true, length = 10)
    private String inventarNummer;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "beschreibung", length = 1000)
    private String beschreibung;

    @Column(name = "kategorie_name", nullable = false, length = 100)
    private String kategorieName;

    @Column(name = "kategorie_max_tage", nullable = false)
    private int kategorieMaxTage;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private VerfuegbarkeitsStatus status;

    public GegenstandJpaEntity() {
    }

    /**
     * Konvertiert ein Domain-Objekt in eine JPA-Entity.
     */
    public static GegenstandJpaEntity fromDomain(Gegenstand gegenstand) {
        GegenstandJpaEntity entity = new GegenstandJpaEntity();
        entity.id = gegenstand.getId();
        entity.inventarNummer = gegenstand.getInventarNummer().getValue();
        entity.name = gegenstand.getName();
        entity.beschreibung = gegenstand.getBeschreibung();
        entity.kategorieName = gegenstand.getKategorie().getName();
        entity.kategorieMaxTage = gegenstand.getKategorie().getMaxAusleihdauerTage();
        entity.status = gegenstand.getStatus();
        return entity;
    }

    /**
     * Konvertiert die JPA-Entity in ein Domain-Objekt.
     */
    public Gegenstand toDomain() {
        Gegenstand gegenstand = new Gegenstand(
            id,
            InventarNummer.of(inventarNummer),
            name,
            beschreibung,
            Kategorie.of(kategorieName, kategorieMaxTage)
        );

        // Status wiederherstellen über Reflection oder direkte Manipulation
        // Da Gegenstand den Status nur über Methoden ändert, müssen wir hier
        // einen Workaround verwenden
        setzeStatus(gegenstand, status);

        return gegenstand;
    }

    /**
     * Hilfsmethode zum Setzen des Status (für Wiederherstellung aus DB).
     */
    private void setzeStatus(Gegenstand gegenstand, VerfuegbarkeitsStatus zielStatus) {
        // Der Status ist initial VERFUEGBAR
        if (zielStatus == VerfuegbarkeitsStatus.VERFUEGBAR) {
            return; // Nichts zu tun
        }

        // Für andere Status verwenden wir die verfügbaren Methoden
        switch (zielStatus) {
            case AUSGELIEHEN -> gegenstand.ausleihen();
            case IN_WARTUNG -> gegenstand.inWartungSetzen();
            case AUSGEMUSTERT -> gegenstand.ausmustern();
        }
    }

    // Getter und Setter

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getInventarNummer() {
        return inventarNummer;
    }

    public void setInventarNummer(String inventarNummer) {
        this.inventarNummer = inventarNummer;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public String getKategorieName() {
        return kategorieName;
    }

    public void setKategorieName(String kategorieName) {
        this.kategorieName = kategorieName;
    }

    public int getKategorieMaxTage() {
        return kategorieMaxTage;
    }

    public void setKategorieMaxTage(int kategorieMaxTage) {
        this.kategorieMaxTage = kategorieMaxTage;
    }

    public VerfuegbarkeitsStatus getStatus() {
        return status;
    }

    public void setStatus(VerfuegbarkeitsStatus status) {
        this.status = status;
    }
}
