package de.dhbw.leihbar.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.Objects;

/**
 * Basisklasse der JPA-Repositories.
 *
 * <p>Bündelt den gemeinsamen EntityManager und die Transaktionsbehandlung für
 * schreibende Operationen ({@code speichern}, {@code loeschen}). Vor der
 * Extraktion war der begin/commit/rollback-Block in jeder einzelnen
 * Schreibmethode aller drei Repositories dupliziert.</p>
 *
 * <p>{@link #inTransaction(Runnable)} nimmt an einer bereits laufenden
 * Transaktion teil, statt eine neue zu öffnen. So bleibt eine vom
 * {@code JpaTransactionRunner} geöffnete äußere Transaktionsklammer wirksam,
 * und mehrere Repository-Aufrufe werden gemeinsam committet oder verworfen.</p>
 */
abstract class AbstractJpaRepository {

    protected final EntityManager entityManager;

    protected AbstractJpaRepository(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager, "EntityManager darf nicht null sein");
    }

    /**
     * Führt eine schreibende Operation transaktional aus. Läuft bereits eine
     * Transaktion, wird diese mitgenutzt; andernfalls wird eine eigene geöffnet
     * und bei Erfolg committet bzw. bei Fehler zurückgerollt.
     */
    protected void inTransaction(Runnable work) {
        EntityTransaction tx = entityManager.getTransaction();

        if (tx.isActive()) {
            work.run();
            return;
        }

        tx.begin();
        try {
            work.run();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }
}
