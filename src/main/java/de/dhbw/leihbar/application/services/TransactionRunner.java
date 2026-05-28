package de.dhbw.leihbar.application.services;

import java.util.function.Supplier;

/**
 * Port für die Steuerung von Transaktionsgrenzen auf Use-Case-Ebene.
 *
 * <p>Use Cases, die mehrere Aggregate in einem Schritt verändern (z.B. eine
 * Ausleihe anlegen und gleichzeitig den Gegenstand-Status setzen), müssen diese
 * Änderungen atomar speichern. Dieser Port erlaubt es dem Application Service,
 * eine Transaktionsklammer um mehrere Repository-Aufrufe zu legen, ohne die
 * konkrete Persistenztechnologie (JPA) zu kennen.</p>
 *
 * <p>Die Implementierung liegt in der Infrastructure-Schicht
 * ({@code JpaTransactionRunner}). Damit bleibt die Abhängigkeitsrichtung
 * gewahrt: Application definiert den Port, Infrastructure implementiert ihn.</p>
 */
public interface TransactionRunner {

    /**
     * Führt die übergebene Arbeit innerhalb einer Transaktion aus und gibt
     * deren Ergebnis zurück. Bei einer Ausnahme wird die Transaktion
     * zurückgerollt und die Ausnahme weitergereicht.
     */
    <T> T execute(Supplier<T> work);

    /**
     * Variante ohne Rückgabewert.
     */
    default void execute(Runnable work) {
        execute(() -> {
            work.run();
            return null;
        });
    }
}
