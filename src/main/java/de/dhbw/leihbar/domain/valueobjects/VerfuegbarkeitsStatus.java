package de.dhbw.leihbar.domain.valueobjects;

/**
 * Enum für den Verfügbarkeitsstatus eines Gegenstandes.
 */
public enum VerfuegbarkeitsStatus {

    VERFUEGBAR("Verfügbar", true),
    AUSGELIEHEN("Ausgeliehen", false),
    IN_WARTUNG("In Wartung", false),
    AUSGEMUSTERT("Ausgemustert", false);

    private final String bezeichnung;
    private final boolean ausleihbar;

    VerfuegbarkeitsStatus(String bezeichnung, boolean ausleihbar) {
        this.bezeichnung = bezeichnung;
        this.ausleihbar = ausleihbar;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    /**
     * Gibt an, ob ein Gegenstand mit diesem Status ausgeliehen werden kann.
     */
    public boolean istAusleihbar() {
        return ausleihbar;
    }

    @Override
    public String toString() {
        return bezeichnung;
    }
}
