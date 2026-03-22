package de.dhbw.leihbar.domain.entities;

import de.dhbw.leihbar.domain.valueobjects.Kontaktdaten;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity fuer eine Person, die Gegenstaende ausleihen kann.
 * PRE-REFACTORING: getVollstaendigerName() wird spaeter zu getVollerName().
 * mitId() Factory wird entfernt zugunsten des direkten Konstruktors.
 */
public class Ausleiher {

    private final UUID id;
    private String vorname;
    private String nachname;
    private Kontaktdaten kontaktdaten;

    public Ausleiher(UUID id, String vorname, String nachname, Kontaktdaten kontaktdaten) {
        this.id = Objects.requireNonNull(id, "ID darf nicht null sein");
        setVorname(vorname);
        setNachname(nachname);
        setKontaktdaten(kontaktdaten);
    }

    /**
     * Factory-Methode zur Erzeugung eines neuen Ausleihers mit generierter ID.
     */
    public static Ausleiher neu(String vorname, String nachname, Kontaktdaten kontaktdaten) {
        return new Ausleiher(UUID.randomUUID(), vorname, nachname, kontaktdaten);
    }

    /**
     * Factory-Methode fuer DB-Rekonstruktion mit bestehender ID.
     * PRE-REFACTORING: Wird spaeter entfernt (direkter Konstruktor stattdessen).
     */
    public static Ausleiher mitId(UUID id, String vorname, String nachname, Kontaktdaten kontaktdaten) {
        return new Ausleiher(id, vorname, nachname, kontaktdaten);
    }

    public UUID getId() {
        return id;
    }

    public String getVorname() {
        return vorname;
    }

    public void setVorname(String vorname) {
        Objects.requireNonNull(vorname, "Vorname darf nicht null sein");
        String normalized = vorname.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Vorname darf nicht leer sein");
        }
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Vorname darf maximal 100 Zeichen haben");
        }
        this.vorname = normalized;
    }

    public String getNachname() {
        return nachname;
    }

    public void setNachname(String nachname) {
        Objects.requireNonNull(nachname, "Nachname darf nicht null sein");
        String normalized = nachname.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Nachname darf nicht leer sein");
        }
        if (normalized.length() > 100) {
            throw new IllegalArgumentException("Nachname darf maximal 100 Zeichen haben");
        }
        this.nachname = normalized;
    }

    public Kontaktdaten getKontaktdaten() {
        return kontaktdaten;
    }

    public void setKontaktdaten(Kontaktdaten kontaktdaten) {
        this.kontaktdaten = Objects.requireNonNull(kontaktdaten, "Kontaktdaten duerfen nicht null sein");
    }

    /**
     * Gibt den vollstaendigen Namen zurueck.
     * PRE-REFACTORING: Wird spaeter zu getVollerName() umbenannt.
     */
    public String getVollstaendigerName() {
        return vorname + " " + nachname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ausleiher ausleiher = (Ausleiher) o;
        return id.equals(ausleiher.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getVollstaendigerName() + " (" + kontaktdaten.getEmail() + ")";
    }
}
