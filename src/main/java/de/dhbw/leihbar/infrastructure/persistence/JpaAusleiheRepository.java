package de.dhbw.leihbar.infrastructure.persistence;

import de.dhbw.leihbar.domain.aggregates.Ausleihe;
import de.dhbw.leihbar.domain.repositories.AusleiheRepository;
import de.dhbw.leihbar.domain.valueobjects.AusleiheStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA-Implementierung des AusleiheRepository.
 */
public class JpaAusleiheRepository extends AbstractJpaRepository implements AusleiheRepository {

    public JpaAusleiheRepository(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public Ausleihe speichern(Ausleihe ausleihe) {
        AusleiheJpaEntity entity = AusleiheJpaEntity.fromDomain(ausleihe);

        inTransaction(() -> {
            if (entityManager.find(AusleiheJpaEntity.class, entity.getId()) != null) {
                entityManager.merge(entity);
            } else {
                entityManager.persist(entity);
            }
        });

        return ausleihe;
    }

    @Override
    public Optional<Ausleihe> findeNachId(UUID id) {
        AusleiheJpaEntity entity = entityManager.find(AusleiheJpaEntity.class, id);
        return Optional.ofNullable(entity).map(AusleiheJpaEntity::toDomain);
    }

    @Override
    public List<Ausleihe> findeAlle() {
        TypedQuery<AusleiheJpaEntity> query = entityManager.createQuery(
            "SELECT a FROM AusleiheJpaEntity a ORDER BY a.erstelltAm DESC",
            AusleiheJpaEntity.class
        );
        return query.getResultList().stream()
            .map(AusleiheJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Ausleihe> findeNachStatus(AusleiheStatus status) {
        TypedQuery<AusleiheJpaEntity> query = entityManager.createQuery(
            "SELECT a FROM AusleiheJpaEntity a WHERE a.status = :status ORDER BY a.erstelltAm DESC",
            AusleiheJpaEntity.class
        );
        query.setParameter("status", status);
        return query.getResultList().stream()
            .map(AusleiheJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Ausleihe> findeAktive() {
        TypedQuery<AusleiheJpaEntity> query = entityManager.createQuery(
            "SELECT a FROM AusleiheJpaEntity a WHERE a.status IN (:aktiv, :ueberfaellig) " +
            "ORDER BY a.geplantesRueckgabedatum ASC",
            AusleiheJpaEntity.class
        );
        query.setParameter("aktiv", AusleiheStatus.AKTIV);
        query.setParameter("ueberfaellig", AusleiheStatus.UEBERFAELLIG);
        return query.getResultList().stream()
            .map(AusleiheJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Ausleihe> findeUeberfaellige() {
        return findeNachStatus(AusleiheStatus.UEBERFAELLIG);
    }

    @Override
    public List<Ausleihe> findeNachGegenstandId(UUID gegenstandId) {
        TypedQuery<AusleiheJpaEntity> query = entityManager.createQuery(
            "SELECT a FROM AusleiheJpaEntity a WHERE a.gegenstandId = :gId ORDER BY a.erstelltAm DESC",
            AusleiheJpaEntity.class
        );
        query.setParameter("gId", gegenstandId);
        return query.getResultList().stream()
            .map(AusleiheJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Ausleihe> findeNachAusleiherId(UUID ausleiherId) {
        TypedQuery<AusleiheJpaEntity> query = entityManager.createQuery(
            "SELECT a FROM AusleiheJpaEntity a WHERE a.ausleiherId = :aId ORDER BY a.erstelltAm DESC",
            AusleiheJpaEntity.class
        );
        query.setParameter("aId", ausleiherId);
        return query.getResultList().stream()
            .map(AusleiheJpaEntity::toDomain)
            .toList();
    }

    @Override
    public Optional<Ausleihe> findeAktiveAusleiheVonGegenstand(UUID gegenstandId) {
        TypedQuery<AusleiheJpaEntity> query = entityManager.createQuery(
            "SELECT a FROM AusleiheJpaEntity a WHERE a.gegenstandId = :gId " +
            "AND a.status IN (:aktiv, :ueberfaellig)",
            AusleiheJpaEntity.class
        );
        query.setParameter("gId", gegenstandId);
        query.setParameter("aktiv", AusleiheStatus.AKTIV);
        query.setParameter("ueberfaellig", AusleiheStatus.UEBERFAELLIG);

        List<AusleiheJpaEntity> results = query.getResultList();
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0).toDomain());
    }

    @Override
    public void loeschen(UUID id) {
        inTransaction(() -> {
            AusleiheJpaEntity entity = entityManager.find(AusleiheJpaEntity.class, id);
            if (entity != null) {
                entityManager.remove(entity);
            }
        });
    }

    @Override
    public long zaehleAlle() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(a) FROM AusleiheJpaEntity a",
            Long.class
        );
        return query.getSingleResult();
    }

    @Override
    public long zaehleNachStatus(AusleiheStatus status) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(a) FROM AusleiheJpaEntity a WHERE a.status = :status",
            Long.class
        );
        query.setParameter("status", status);
        return query.getSingleResult();
    }

    @Override
    public long zaehleUeberfaellige() {
        return zaehleNachStatus(AusleiheStatus.UEBERFAELLIG);
    }
}
