package de.dhbw.leihbar.domain.entities;

import de.dhbw.leihbar.domain.valueobjects.Kontaktdaten;

import java.util.Objects;
import java.util.UUID;

/**
 * Entity für eine Person, die Gegenstände ausleihen kann.
 * Identität wird über die eindeutige ID bestimmt.
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

    public UUID getId() {
        return id;
    }

    public String getVorname() {
        return vorname;
    }

    public String getNachname() {
        return nachname;
    }

    public Kontaktdaten getKontaktdaten() {
        return kontaktdaten;
    }

    /**
     * Benennt den Ausleiher um (z.B. nach Heirat).
     * Die Identität (id) bleibt dabei unverändert.
     */
    public void umbenennen(String vorname, String nachname) {
        setVorname(vorname);
        setNachname(nachname);
    }

    /**
     * Aktualisiert die Kontaktdaten des Ausleihers.
     */
    public void kontaktdatenAendern(Kontaktdaten kontaktdaten) {
        setKontaktdaten(kontaktdaten);
    }

    private void setVorname(String vorname) {
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

    private void setNachname(String nachname) {
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

    private void setKontaktdaten(Kontaktdaten kontaktdaten) {
        this.kontaktdaten = Objects.requireNonNull(kontaktdaten, "Kontaktdaten dürfen nicht null sein");
    }

    /**
     * Gibt den vollständigen Namen zurück.
     */
    public String getVollerName() {
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
        return getVollerName() + " (" + kontaktdaten.getEmail() + ")";
    }
}
