package de.dhbw.leihbar;

/**
 * Einstiegspunkt der LeihBar-Anwendung.
 *
 * Geplanter Ablauf beim Start:
 * 1. JPA EntityManagerFactory initialisieren (Hibernate + H2)
 * 2. Repository-Implementierungen erzeugen
 * 3. Domain Services mit Repositories verdrahten
 * 4. Application Services mit Domain Services verbinden
 * 5. JavaFX Application starten und MainView anzeigen
 *
 * Architektur-Hinweis:
 * - Separate Launcher-Klasse nötig für Fat-JAR (JavaFX-Einschränkung)
 * - Die eigentliche Application-Klasse (LeihBarApplication) wird hier aufgerufen
 * - LeihBarApplication erbt von javafx.application.Application
 *
 * TODO: LeihBarApplication-Klasse erstellen
 * TODO: Fat-JAR Build mit maven-shade-plugin konfigurieren
 */
public class Launcher {

    public static void main(String[] args) {
        // TODO: LeihBarApplication.main(args) aufrufen sobald UI steht
        System.out.println("LeihBar - Ausleihe-Verwaltungssystem");
        System.out.println("Version 0.1 - In Entwicklung");
    }
}
