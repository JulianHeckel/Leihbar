package de.dhbw.leihbar.infrastructure.persistence;

import de.dhbw.leihbar.domain.entities.Ausleiher;
import de.dhbw.leihbar.domain.valueobjects.Kontaktdaten;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * JPA Entity für die Persistierung von Ausleihern.
 */
@Entity
@Table(name = "ausleiher")
public class AusleiherJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "vorname", nullable = false, length = 100)
    private String vorname;

    @Column(name = "nachname", nullable = false, length = 100)
    private String nachname;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "telefon", length = 30)
    private String telefon;

    public AusleiherJpaEntity() {
    }

    /**
     * Konvertiert ein Domain-Objekt in eine JPA-Entity.
     */
    public static AusleiherJpaEntity fromDomain(Ausleiher ausleiher) {
        AusleiherJpaEntity entity = new AusleiherJpaEntity();
        entity.id = ausleiher.getId();
        entity.vorname = ausleiher.getVorname();
        entity.nachname = ausleiher.getNachname();
        entity.email = ausleiher.getKontaktdaten().getEmail();
        entity.telefon = ausleiher.getKontaktdaten().getTelefon();
        return entity;
    }

    /**
     * Konvertiert die JPA-Entity in ein Domain-Objekt.
     */
    public Ausleiher toDomain() {
        Kontaktdaten kontaktdaten = telefon != null
            ? Kontaktdaten.of(email, telefon)
            : Kontaktdaten.nurEmail(email);

        return new Ausleiher(id, vorname, nachname, kontaktdaten);
    }

    // Getter und Setter

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        this.vorname = vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        this.nachname = nachname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }
}
