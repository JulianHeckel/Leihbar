package de.dhbw.leihbar.domain.valueobjects;

/**
 * Enum für den Status einer Ausleihe.
 */
public enum AusleiheStatus {

    AKTIV("Aktiv"),
    ZURUECKGEGEBEN("Zurückgegeben"),
    UEBERFAELLIG("Überfällig"),
    STORNIERT("Storniert");

    private final String bezeichnung;

    AusleiheStatus(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    /**
     * Prüft, ob die Ausleihe noch aktiv ist (nicht abgeschlossen).
     */
    public boolean istAktiv() {
        return this == AKTIV || this == UEBERFAELLIG;
    }

    @Override
    public String toString() {
        return bezeichnung;
    }
}
