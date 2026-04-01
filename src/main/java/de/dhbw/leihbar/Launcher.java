package de.dhbw.leihbar;

/**
 * Launcher-Klasse für die LeihBar-Anwendung.
 * Diese Klasse ist notwendig, um JavaFX-Anwendungen aus einem Fat-JAR zu starten.
 * JavaFX erfordert, dass die Main-Klasse eines Fat-JARs nicht von Application erbt.
 */
public class Launcher {

    public static void main(String[] args) {
        LeihBarApplication.main(args);
    }
}
