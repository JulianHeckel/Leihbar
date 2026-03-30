package de.dhbw.leihbar.domain.services;

import de.dhbw.leihbar.domain.aggregates.Ausleihe;
import de.dhbw.leihbar.domain.repositories.AusleiheRepository;

import java.util.List;
import java.util.Objects;

/**
 * Domain Service zur Verwaltung und Prüfung überfälliger Ausleihen.
 * Enthält Geschäftslogik für das Mahnwesen.
 */
public class UeberfaelligkeitService {

    private final AusleiheRepository ausleiheRepository;

    public UeberfaelligkeitService(AusleiheRepository ausleiheRepository) {
        this.ausleiheRepository = Objects.requireNonNull(ausleiheRepository,
            "AusleiheRepository darf nicht null sein");
    }

    /**
     * Ermittelt alle überfälligen Ausleihen.
     *
     * @return Liste der überfälligen Ausleihen, sortiert nach Überfälligkeitsdauer
     */
    public List<Ausleihe> ermittleUeberfaelligeAusleihen() {
        List<Ausleihe> aktiveAusleihen = ausleiheRepository.findeAktive();

        return aktiveAusleihen.stream()
            .filter(Ausleihe::istUeberfaellig)
            .sorted((a1, a2) -> Long.compare(a2.getUeberfaelligeTage(), a1.getUeberfaelligeTage()))
            .toList();
    }

    /**
     * Aktualisiert den Status aller aktiven Ausleihen auf UEBERFAELLIG,
     * falls das Rückgabedatum überschritten ist.
     *
     * @return Anzahl der aktualisierten Ausleihen
     */
    public int aktualisiereUeberfaelligkeitsstatus() {
        List<Ausleihe> aktiveAusleihen = ausleiheRepository.findeAktive();
        int aktualisiert = 0;

        for (Ausleihe ausleihe : aktiveAusleihen) {
            if (ausleihe.istUeberfaellig()) {
                ausleihe.aktualisiereUeberfaelligkeitsstatus();
                ausleiheRepository.speichern(ausleihe);
                aktualisiert++;
            }
        }

        return aktualisiert;
    }

    /**
     * Zählt die Anzahl überfälliger Ausleihen.
     */
    public long zaehleUeberfaellige() {
        return ausleiheRepository.findeAktive().stream()
            .filter(Ausleihe::istUeberfaellig)
            .count();
    }

    /**
     * Berechnet die durchschnittliche Überfälligkeitsdauer in Tagen.
     */
    public double berechneDurchschnittlicheUeberfaelligkeit() {
        List<Ausleihe> ueberfaellige = ermittleUeberfaelligeAusleihen();

        if (ueberfaellige.isEmpty()) {
            return 0.0;
        }

        long gesamtTage = ueberfaellige.stream()
            .mapToLong(Ausleihe::getUeberfaelligeTage)
            .sum();

        return (double) gesamtTage / ueberfaellige.size();
    }

    /**
     * Ermittelt Ausleihen, die innerhalb der nächsten Tage fällig werden.
     *
     * @param tage Anzahl der Tage in der Zukunft
     * @return Liste der bald fälligen Ausleihen
     */
    public List<Ausleihe> ermittleBaldFaellige(int tage) {
        if (tage < 0) {
            throw new IllegalArgumentException("Tage darf nicht negativ sein");
        }

        return ausleiheRepository.findeAktive().stream()
            .filter(a -> !a.istUeberfaellig())
            .filter(a -> a.getGeplanterZeitraum().getUeberfaelligeTage() >= -tage)
            .toList();
    }
}
