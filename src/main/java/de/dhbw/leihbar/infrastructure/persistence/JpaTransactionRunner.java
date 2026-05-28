package de.dhbw.leihbar.infrastructure.persistence;

import de.dhbw.leihbar.application.services.TransactionRunner;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * JPA-Implementierung des {@link TransactionRunner}.
 *
 * <p>Öffnet eine RESOURCE_LOCAL-Transaktion auf dem gemeinsam genutzten
 * EntityManager, führt die Arbeit aus und committet. Tritt eine Ausnahme auf,
 * wird zurückgerollt.</p>
 *
 * <p>Ist beim Aufruf bereits eine Transaktion aktiv (verschachtelter Aufruf),
 * nimmt die Arbeit an der laufenden Transaktion teil, ohne eine neue zu öffnen.
 * Dadurch funktionieren die Repository-Methoden sowohl eigenständig als auch
 * innerhalb einer äußeren Transaktionsklammer.</p>
 */
public class JpaTransactionRunner implements TransactionRunner {

    private final EntityManager entityManager;

    public JpaTransactionRunner(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager, "EntityManager darf nicht null sein");
    }

    @Override
    public <T> T execute(Supplier<T> work) {
        EntityTransaction tx = entityManager.getTransaction();

        // An bereits laufender Transaktion teilnehmen (kein verschachtelter begin()).
        if (tx.isActive()) {
            return work.get();
        }

        tx.begin();
        try {
            T result = work.get();
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }
}
