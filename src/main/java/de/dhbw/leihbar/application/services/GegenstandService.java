package de.dhbw.leihbar.application.services;

import de.dhbw.leihbar.domain.aggregates.Gegenstand;
import de.dhbw.leihbar.domain.repositories.GegenstandRepository;
import de.dhbw.leihbar.domain.valueobjects.InventarNummer;
import de.dhbw.leihbar.domain.valueobjects.Kategorie;
import de.dhbw.leihbar.domain.valueobjects.VerfuegbarkeitsStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service für die Verwaltung von Gegenständen.
 * Koordiniert die Use Cases und orchestriert die Domain-Objekte.
 */
public class GegenstandService {

    private static final Logger logger = LoggerFactory.getLogger(GegenstandService.class);

    private final GegenstandRepository gegenstandRepository;

    public GegenstandService(GegenstandRepository gegenstandRepository) {
        this.gegenstandRepository = Objects.requireNonNull(gegenstandRepository,
            "GegenstandRepository darf nicht null sein");
    }

    /**
     * Legt einen neuen Gegenstand an.
     *
     * @param name        Name des Gegenstandes
     * @param beschreibung Optionale Beschreibung
     * @param kategorie   Kategorie des Gegenstandes
     * @return Der angelegte Gegenstand
     */
    public Gegenstand gegenstandAnlegen(String name, String beschreibung, Kategorie kategorie) {
        logger.info("Lege neuen Gegenstand an: {}", name);

        InventarNummer inventarNummer = gegenstandRepository.naechsteFreieInventarNummer();

        Gegenstand gegenstand = Gegenstand.neu(inventarNummer, name, beschreibung, kategorie);
        Gegenstand gespeichert = gegenstandRepository.speichern(gegenstand);

        logger.info("Gegenstand angelegt mit Inventarnummer: {}", inventarNummer);
        return gespeichert;
    }

    /**
     * Legt einen neuen Gegenstand mit benutzerdefinierter Inventarnummer an.
     */
    public Gegenstand gegenstandAnlegenMitInventarNummer(InventarNummer inventarNummer, String name,
                                                          String beschreibung, Kategorie kategorie) {
        logger.info("Lege neuen Gegenstand an mit Inventarnummer: {}", inventarNummer);

        if (gegenstandRepository.existiertInventarNummer(inventarNummer)) {
            throw new IllegalArgumentException(
                "Inventarnummer " + inventarNummer + " existiert bereits"
            );
        }

        Gegenstand gegenstand = Gegenstand.neu(inventarNummer, name, beschreibung, kategorie);
        return gegenstandRepository.speichern(gegenstand);
    }

    /**
     * Aktualisiert einen bestehenden Gegenstand.
     */
    public Gegenstand gegenstandAktualisieren(UUID id, String name, String beschreibung, Kategorie kategorie) {
        logger.info("Aktualisiere Gegenstand: {}", id);

        Gegenstand gegenstand = gegenstandRepository.findeNachId(id)
            .orElseThrow(() -> new IllegalArgumentException("Gegenstand nicht gefunden: " + id));

        gegenstand.setName(name);
        gegenstand.setBeschreibung(beschreibung);
        gegenstand.setKategorie(kategorie);

        return gegenstandRepository.speichern(gegenstand);
    }

    /**
     * Findet einen Gegenstand anhand seiner ID.
     */
    public Optional<Gegenstand> findeGegenstand(UUID id) {
        return gegenstandRepository.findeNachId(id);
    }

    /**
     * Findet einen Gegenstand anhand seiner Inventarnummer.
     */
    public Optional<Gegenstand> findeNachInventarNummer(InventarNummer inventarNummer) {
        return gegenstandRepository.findeNachInventarNummer(inventarNummer);
    }

    /**
     * Gibt alle Gegenstände zurück.
     */
    public List<Gegenstand> alleGegenstaende() {
        return gegenstandRepository.findeAlle();
    }

    /**
     * Gibt alle verfügbaren Gegenstände zurück.
     */
    public List<Gegenstand> verfuegbareGegenstaende() {
        return gegenstandRepository.findeVerfuegbare();
    }

    /**
     * Sucht Gegenstände nach einem Suchbegriff (Name, Beschreibung, Kategorie, Inventarnummer).
     */
    public List<Gegenstand> sucheGegenstaende(String suchbegriff) {
        return gegenstandRepository.suche(suchbegriff);
    }

    /**
     * Filtert Gegenstände nach Status.
     */
    public List<Gegenstand> findeNachStatus(VerfuegbarkeitsStatus status) {
        return gegenstandRepository.findeNachStatus(status);
    }

    /**
     * Löscht einen Gegenstand.
     */
    public void gegenstandLoeschen(UUID id) {
        logger.info("Lösche Gegenstand: {}", id);

        Gegenstand gegenstand = gegenstandRepository.findeNachId(id)
            .orElseThrow(() -> new IllegalArgumentException("Gegenstand nicht gefunden: " + id));

        if (gegenstand.getStatus() == VerfuegbarkeitsStatus.AUSGELIEHEN) {
            throw new IllegalStateException("Ausgeliehener Gegenstand kann nicht gelöscht werden");
        }

        gegenstandRepository.loeschen(id);
        logger.info("Gegenstand gelöscht: {}", id);
    }

    /**
     * Setzt einen Gegenstand in Wartung.
     */
    public Gegenstand inWartungSetzen(UUID id) {
        logger.info("Setze Gegenstand in Wartung: {}", id);

        Gegenstand gegenstand = gegenstandRepository.findeNachId(id)
            .orElseThrow(() -> new IllegalArgumentException("Gegenstand nicht gefunden: " + id));

        gegenstand.inWartungSetzen();
        return gegenstandRepository.speichern(gegenstand);
    }

    /**
     * Beendet die Wartung eines Gegenstandes.
     */
    public Gegenstand wartungBeenden(UUID id) {
        logger.info("Beende Wartung für Gegenstand: {}", id);

        Gegenstand gegenstand = gegenstandRepository.findeNachId(id)
            .orElseThrow(() -> new IllegalArgumentException("Gegenstand nicht gefunden: " + id));

        gegenstand.wartungBeenden();
        return gegenstandRepository.speichern(gegenstand);
    }

    /**
     * Mustert einen Gegenstand aus (endgültig).
     */
    public Gegenstand ausmustern(UUID id) {
        logger.info("Mustere Gegenstand aus: {}", id);

        Gegenstand gegenstand = gegenstandRepository.findeNachId(id)
            .orElseThrow(() -> new IllegalArgumentException("Gegenstand nicht gefunden: " + id));

        gegenstand.ausmustern();
        return gegenstandRepository.speichern(gegenstand);
    }

    /**
     * Zählt alle Gegenstände.
     */
    /**
     * Gibt alle verwendeten Kategorienamen zurück (für Vorschläge im UI).
     */
    public List<String> alleKategorienamen() {
        return gegenstandRepository.findeAlleKategorienamen();
    }

    public long zaehleAlle() {
        return gegenstandRepository.zaehleAlle();
    }

    /**
     * Zählt Gegenstände nach Status.
     */
    public long zaehleNachStatus(VerfuegbarkeitsStatus status) {
        return gegenstandRepository.zaehleNachStatus(status);
    }
}
