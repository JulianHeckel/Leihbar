package de.dhbw.leihbar.application.services;

import de.dhbw.leihbar.domain.entities.Ausleiher;
import de.dhbw.leihbar.domain.repositories.AusleiherRepository;
import de.dhbw.leihbar.domain.valueobjects.Kontaktdaten;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service für die Verwaltung von Ausleihern.
 */
public class AusleiherService {

    private static final Logger logger = LoggerFactory.getLogger(AusleiherService.class);

    private final AusleiherRepository ausleiherRepository;

    public AusleiherService(AusleiherRepository ausleiherRepository) {
        this.ausleiherRepository = Objects.requireNonNull(ausleiherRepository,
            "AusleiherRepository darf nicht null sein");
    }

    /**
     * Legt einen neuen Ausleiher an.
     */
    public Ausleiher ausleiherAnlegen(String vorname, String nachname, String email, String telefon) {
        logger.info("Lege neuen Ausleiher an: {} {}", vorname, nachname);

        if (ausleiherRepository.existiertEmail(email)) {
            throw new IllegalArgumentException("E-Mail-Adresse existiert bereits: " + email);
        }

        Kontaktdaten kontaktdaten = telefon != null && !telefon.isBlank()
            ? Kontaktdaten.of(email, telefon)
            : Kontaktdaten.nurEmail(email);

        Ausleiher ausleiher = Ausleiher.neu(vorname, nachname, kontaktdaten);
        Ausleiher gespeichert = ausleiherRepository.speichern(ausleiher);

        logger.info("Ausleiher angelegt: {}", gespeichert.getId());
        return gespeichert;
    }

    /**
     * Aktualisiert einen bestehenden Ausleiher.
     */
    public Ausleiher ausleiherAktualisieren(UUID id, String vorname, String nachname,
                                             String email, String telefon) {
        logger.info("Aktualisiere Ausleiher: {}", id);

        Ausleiher ausleiher = ausleiherRepository.findeNachId(id)
            .orElseThrow(() -> new IllegalArgumentException("Ausleiher nicht gefunden: " + id));

        // Prüfe ob E-Mail geändert wurde und ob neue E-Mail bereits existiert
        if (!ausleiher.getKontaktdaten().getEmail().equalsIgnoreCase(email)) {
            if (ausleiherRepository.existiertEmail(email)) {
                throw new IllegalArgumentException("E-Mail-Adresse existiert bereits: " + email);
            }
        }

        ausleiher.umbenennen(vorname, nachname);

        Kontaktdaten kontaktdaten = telefon != null && !telefon.isBlank()
            ? Kontaktdaten.of(email, telefon)
            : Kontaktdaten.nurEmail(email);
        ausleiher.kontaktdatenAendern(kontaktdaten);

        return ausleiherRepository.speichern(ausleiher);
    }

    /**
     * Findet einen Ausleiher anhand seiner ID.
     */
    public Optional<Ausleiher> findeAusleiher(UUID id) {
        return ausleiherRepository.findeNachId(id);
    }

    /**
     * Findet einen Ausleiher anhand seiner E-Mail.
     */
    public Optional<Ausleiher> findeNachEmail(String email) {
        return ausleiherRepository.findeNachEmail(email);
    }

    /**
     * Gibt alle Ausleiher zurück.
     */
    public List<Ausleiher> alleAusleiher() {
        return ausleiherRepository.findeAlle();
    }

    /**
     * Sucht Ausleiher nach einem Suchbegriff.
     */
    public List<Ausleiher> sucheAusleiher(String suchbegriff) {
        return ausleiherRepository.suche(suchbegriff);
    }

    /**
     * Löscht einen Ausleiher.
     */
    public void ausleiherLoeschen(UUID id) {
        logger.info("Lösche Ausleiher: {}", id);

        if (ausleiherRepository.findeNachId(id).isEmpty()) {
            throw new IllegalArgumentException("Ausleiher nicht gefunden: " + id);
        }

        ausleiherRepository.loeschen(id);
        logger.info("Ausleiher gelöscht: {}", id);
    }

    /**
     * Zählt alle Ausleiher.
     */
    public long zaehleAlle() {
        return ausleiherRepository.zaehleAlle();
    }
}
