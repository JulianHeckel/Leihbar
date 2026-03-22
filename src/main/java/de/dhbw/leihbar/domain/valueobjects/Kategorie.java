package de.dhbw.leihbar.domain.valueobjects;

/**
 * Enum fuer die Kategorie eines Gegenstandes.
 * PRE-REFACTORING: Wird spaeter zu einer Klasse mit Factory-Methoden und maxAusleihdauer.
 */
public enum Kategorie {

    WERKZEUG("Werkzeug"),
    ELEKTRONIK("Elektronik"),
    BUECHER("Buecher"),
    SPORT("Sport"),
    SONSTIGES("Sonstiges");

    private final String bezeichnung;

    Kategorie(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String getBezeichnung() {
        return bezeichnung;
    }

    @Override
    public String toString() {
        return bezeichnung;
    }
}
