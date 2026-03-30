package de.dhbw.leihbar.domain.services;

import de.dhbw.leihbar.domain.aggregates.Gegenstand;
import de.dhbw.leihbar.domain.repositories.AusleiheRepository;
import de.dhbw.leihbar.domain.valueobjects.Zeitraum;

import java.util.Objects;

/**
 * Domain Service zur Prüfung der Verfügbarkeit von Gegenständen.
 * Enthält Geschäftslogik, die nicht zu einer einzelnen Entity gehört.
 */
public class VerfuegbarkeitService {

    private final AusleiheRepository ausleiheRepository;

    public VerfuegbarkeitService(AusleiheRepository ausleiheRepository) {
        this.ausleiheRepository = Objects.requireNonNull(ausleiheRepository,
            "AusleiheRepository darf nicht null sein");
    }

    /**
     * Prüft, ob ein Gegenstand aktuell ausgeliehen werden kann.
     *
     * @param gegenstand Der zu prüfende Gegenstand
     * @return true, wenn der Gegenstand verfügbar ist
     */
    public boolean istVerfuegbar(Gegenstand gegenstand) {
        Objects.requireNonNull(gegenstand, "Gegenstand darf nicht null sein");

        // Prüfe den Status des Gegenstandes selbst
        if (!gegenstand.istVerfuegbar()) {
            return false;
        }

        // Prüfe, ob eine aktive Ausleihe existiert
        return ausleiheRepository.findeAktiveAusleiheVonGegenstand(gegenstand.getId()).isEmpty();
    }

    /**
     * Prüft, ob ein Gegenstand für einen bestimmten Zeitraum ausgeliehen werden kann.
     *
     * @param gegenstand Der zu prüfende Gegenstand
     * @param zeitraum   Der gewünschte Ausleihzeitraum
     * @return Ergebnis der Verfügbarkeitsprüfung
     */
    public VerfuegbarkeitErgebnis pruefeVerfuegbarkeit(Gegenstand gegenstand, Zeitraum zeitraum) {
        Objects.requireNonNull(gegenstand, "Gegenstand darf nicht null sein");
        Objects.requireNonNull(zeitraum, "Zeitraum darf nicht null sein");

        // Prüfe den Status des Gegenstandes
        if (!gegenstand.istVerfuegbar()) {
            return VerfuegbarkeitErgebnis.nichtVerfuegbar(
                "Gegenstand ist nicht verfügbar. Status: " + gegenstand.getStatus().getBezeichnung()
            );
        }

        // Prüfe, ob eine aktive Ausleihe existiert
        var aktiveAusleihe = ausleiheRepository.findeAktiveAusleiheVonGegenstand(gegenstand.getId());
        if (aktiveAusleihe.isPresent()) {
            return VerfuegbarkeitErgebnis.nichtVerfuegbar(
                "Gegenstand ist bereits ausgeliehen bis " +
                aktiveAusleihe.get().getGeplantesRueckgabedatum()
            );
        }

        // Prüfe die maximale Ausleihdauer der Kategorie
        if (!gegenstand.getKategorie().istZeitraumErlaubt(zeitraum)) {
            return VerfuegbarkeitErgebnis.nichtVerfuegbar(
                "Ausleihdauer überschreitet das Maximum von " +
                gegenstand.getKategorie().getMaxAusleihdauerTage() + " Tagen für Kategorie '" +
                gegenstand.getKategorie().getName() + "'"
            );
        }

        return VerfuegbarkeitErgebnis.verfuegbar();
    }

    /**
     * Ergebnis einer Verfügbarkeitsprüfung.
     */
    public static class VerfuegbarkeitErgebnis {
        private final boolean verfuegbar;
        private final String grund;

        private VerfuegbarkeitErgebnis(boolean verfuegbar, String grund) {
            this.verfuegbar = verfuegbar;
            this.grund = grund;
        }

        public static VerfuegbarkeitErgebnis verfuegbar() {
            return new VerfuegbarkeitErgebnis(true, null);
        }

        public static VerfuegbarkeitErgebnis nichtVerfuegbar(String grund) {
            return new VerfuegbarkeitErgebnis(false, grund);
        }

        public boolean istVerfuegbar() {
            return verfuegbar;
        }

        public String getGrund() {
            return grund;
        }
    }
}
