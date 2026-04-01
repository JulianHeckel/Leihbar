package de.dhbw.leihbar.application.services;

import de.dhbw.leihbar.domain.aggregates.Ausleihe;
import de.dhbw.leihbar.domain.aggregates.Gegenstand;
import de.dhbw.leihbar.domain.entities.Ausleiher;
import de.dhbw.leihbar.domain.repositories.AusleiheRepository;
import de.dhbw.leihbar.domain.repositories.AusleiherRepository;
import de.dhbw.leihbar.domain.repositories.GegenstandRepository;
import de.dhbw.leihbar.domain.services.VerfuegbarkeitService;
import de.dhbw.leihbar.domain.valueobjects.Zeitraum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service für die Verwaltung von Ausleihen.
 * Implementiert die Hauptgeschäftslogik für den Ausleihe-Workflow.
 */
public class AusleiheService {

    private static final Logger logger = LoggerFactory.getLogger(AusleiheService.class);

    private final AusleiheRepository ausleiheRepository;
    private final GegenstandRepository gegenstandRepository;
    private final AusleiherRepository ausleiherRepository;
    private final VerfuegbarkeitService verfuegbarkeitService;

    public AusleiheService(AusleiheRepository ausleiheRepository,
                          GegenstandRepository gegenstandRepository,
                          AusleiherRepository ausleiherRepository,
                          VerfuegbarkeitService verfuegbarkeitService) {
        this.ausleiheRepository = Objects.requireNonNull(ausleiheRepository);
        this.gegenstandRepository = Objects.requireNonNull(gegenstandRepository);
        this.ausleiherRepository = Objects.requireNonNull(ausleiherRepository);
        this.verfuegbarkeitService = Objects.requireNonNull(verfuegbarkeitService);
    }

    /**
     * Führt eine neue Ausleihe durch.
     *
     * @param gegenstandId ID des auszuleihenden Gegenstandes
     * @param ausleiherId  ID des Ausleihers
     * @param rueckgabedatum Geplantes Rückgabedatum
     * @return Die erstellte Ausleihe
     * @throws IllegalArgumentException wenn Gegenstand oder Ausleiher nicht existiert
     * @throws IllegalStateException wenn Gegenstand nicht verfügbar ist
     */
    public Ausleihe ausleihen(UUID gegenstandId, UUID ausleiherId, LocalDate rueckgabedatum) {
        logger.info("Starte Ausleihe: Gegenstand={}, Ausleiher={}, bis={}",
            gegenstandId, ausleiherId, rueckgabedatum);

        // Lade Gegenstand und Ausleiher
        Gegenstand gegenstand = gegenstandRepository.findeNachId(gegenstandId)
            .orElseThrow(() -> new IllegalArgumentException("Gegenstand nicht gefunden: " + gegenstandId));

        Ausleiher ausleiher = ausleiherRepository.findeNachId(ausleiherId)
            .orElseThrow(() -> new IllegalArgumentException("Ausleiher nicht gefunden: " + ausleiherId));

        // Erstelle Zeitraum
        Zeitraum zeitraum = new Zeitraum(LocalDate.now(), rueckgabedatum);

        // Prüfe Verfügbarkeit
        var verfuegbarkeitErgebnis = verfuegbarkeitService.pruefeVerfuegbarkeit(gegenstand, zeitraum);
        if (!verfuegbarkeitErgebnis.istVerfuegbar()) {
            throw new IllegalStateException(
                "Ausleihe nicht möglich: " + verfuegbarkeitErgebnis.getGrund()
            );
        }

        // Erstelle Ausleihe mit Builder
        Ausleihe ausleihe = new Ausleihe.Builder()
            .gegenstand(gegenstand)
            .ausleiher(ausleiher)
            .zeitraum(zeitraum)
            .build();

        // Aktualisiere Gegenstand-Status
        gegenstand.ausleihen();
        gegenstandRepository.speichern(gegenstand);

        // Speichere Ausleihe
        Ausleihe gespeichert = ausleiheRepository.speichern(ausleihe);

        logger.info("Ausleihe erfolgreich erstellt: {}", gespeichert.getId());
        return gespeichert;
    }

    /**
     * Erfasst die Rückgabe einer Ausleihe.
     *
     * @param ausleiheId ID der Ausleihe
     * @param zustandsbericht Optionaler Zustandsbericht
     * @return Die aktualisierte Ausleihe
     */
    public Ausleihe zurueckgeben(UUID ausleiheId, String zustandsbericht) {
        logger.info("Erfasse Rückgabe für Ausleihe: {}", ausleiheId);

        Ausleihe ausleihe = ausleiheRepository.findeNachId(ausleiheId)
            .orElseThrow(() -> new IllegalArgumentException("Ausleihe nicht gefunden: " + ausleiheId));

        if (!ausleihe.istAktiv()) {
            throw new IllegalStateException(
                "Ausleihe kann nicht zurückgegeben werden. Status: " + ausleihe.getStatus()
            );
        }

        // Aktualisiere Gegenstand-Status
        Gegenstand gegenstand = gegenstandRepository.findeNachId(ausleihe.getGegenstandId())
            .orElseThrow(() -> new IllegalStateException("Gegenstand nicht gefunden"));

        gegenstand.zurueckgeben();
        gegenstandRepository.speichern(gegenstand);

        // Aktualisiere Ausleihe
        ausleihe.zurueckgeben(zustandsbericht);
        Ausleihe aktualisiert = ausleiheRepository.speichern(ausleihe);

        logger.info("Rückgabe erfolgreich erfasst für Ausleihe: {}", ausleiheId);
        return aktualisiert;
    }

    /**
     * Storniert eine Ausleihe.
     */
    public Ausleihe stornieren(UUID ausleiheId) {
        logger.info("Storniere Ausleihe: {}", ausleiheId);

        Ausleihe ausleihe = ausleiheRepository.findeNachId(ausleiheId)
            .orElseThrow(() -> new IllegalArgumentException("Ausleihe nicht gefunden: " + ausleiheId));

        if (!ausleihe.istAktiv()) {
            throw new IllegalStateException(
                "Ausleihe kann nicht storniert werden. Status: " + ausleihe.getStatus()
            );
        }

        // Aktualisiere Gegenstand-Status
        Gegenstand gegenstand = gegenstandRepository.findeNachId(ausleihe.getGegenstandId())
            .orElseThrow(() -> new IllegalStateException("Gegenstand nicht gefunden"));

        gegenstand.zurueckgeben();
        gegenstandRepository.speichern(gegenstand);

        // Storniere Ausleihe
        ausleihe.stornieren();
        return ausleiheRepository.speichern(ausleihe);
    }

    /**
     * Findet eine Ausleihe anhand ihrer ID.
     */
    public Optional<Ausleihe> findeAusleihe(UUID id) {
        return ausleiheRepository.findeNachId(id);
    }

    /**
     * Gibt alle Ausleihen zurück.
     */
    public List<Ausleihe> alleAusleihen() {
        return ausleiheRepository.findeAlle();
    }

    /**
     * Gibt alle aktiven Ausleihen zurück.
     */
    public List<Ausleihe> aktiveAusleihen() {
        return ausleiheRepository.findeAktive();
    }

    /**
     * Gibt alle überfälligen Ausleihen zurück.
     */
    public List<Ausleihe> ueberfaelligeAusleihen() {
        // Aktualisiere zuerst den Status
        aktualisiereUeberfaelligkeitsstatus();
        return ausleiheRepository.findeUeberfaellige();
    }

    /**
     * Gibt die Ausleihhistorie für einen Gegenstand zurück.
     */
    public List<Ausleihe> ausleihhistorieFuerGegenstand(UUID gegenstandId) {
        return ausleiheRepository.findeNachGegenstandId(gegenstandId);
    }

    /**
     * Gibt alle Ausleihen eines Ausleihers zurück.
     */
    public List<Ausleihe> ausleihenVonAusleiher(UUID ausleiherId) {
        return ausleiheRepository.findeNachAusleiherId(ausleiherId);
    }

    /**
     * Aktualisiert den Überfälligkeitsstatus aller aktiven Ausleihen.
     */
    public int aktualisiereUeberfaelligkeitsstatus() {
        List<Ausleihe> aktive = ausleiheRepository.findeAktive();
        int aktualisiert = 0;

        for (Ausleihe ausleihe : aktive) {
            if (ausleihe.istUeberfaellig()) {
                ausleihe.aktualisiereUeberfaelligkeitsstatus();
                ausleiheRepository.speichern(ausleihe);
                aktualisiert++;
            }
        }

        if (aktualisiert > 0) {
            logger.info("{} Ausleihen als überfällig markiert", aktualisiert);
        }

        return aktualisiert;
    }

    /**
     * Zählt alle Ausleihen.
     */
    public long zaehleAlle() {
        return ausleiheRepository.zaehleAlle();
    }

    /**
     * Zählt überfällige Ausleihen.
     */
    public long zaehleUeberfaellige() {
        return ausleiheRepository.zaehleUeberfaellige();
    }
}
