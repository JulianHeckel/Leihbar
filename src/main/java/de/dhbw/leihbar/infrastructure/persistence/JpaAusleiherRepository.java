package de.dhbw.leihbar.infrastructure.persistence;

import de.dhbw.leihbar.domain.entities.Ausleiher;
import de.dhbw.leihbar.domain.repositories.AusleiherRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA-Implementierung des AusleiherRepository.
 */
public class JpaAusleiherRepository extends AbstractJpaRepository implements AusleiherRepository {

    public JpaAusleiherRepository(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public Ausleiher speichern(Ausleiher ausleiher) {
        AusleiherJpaEntity entity = AusleiherJpaEntity.fromDomain(ausleiher);

        inTransaction(() -> {
            if (entityManager.find(AusleiherJpaEntity.class, entity.getId()) != null) {
                entityManager.merge(entity);
            } else {
                entityManager.persist(entity);
            }
        });

        return ausleiher;
    }

    @Override
    public Optional<Ausleiher> findeNachId(UUID id) {
        AusleiherJpaEntity entity = entityManager.find(AusleiherJpaEntity.class, id);
        return Optional.ofNullable(entity).map(AusleiherJpaEntity::toDomain);
    }

    @Override
    public Optional<Ausleiher> findeNachEmail(String email) {
        try {
            TypedQuery<AusleiherJpaEntity> query = entityManager.createQuery(
                "SELECT a FROM AusleiherJpaEntity a WHERE LOWER(a.email) = LOWER(:email)",
                AusleiherJpaEntity.class
            );
            query.setParameter("email", email);
            return Optional.of(query.getSingleResult().toDomain());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Ausleiher> findeAlle() {
        TypedQuery<AusleiherJpaEntity> query = entityManager.createQuery(
            "SELECT a FROM AusleiherJpaEntity a ORDER BY a.nachname, a.vorname",
            AusleiherJpaEntity.class
        );
        return query.getResultList().stream()
            .map(AusleiherJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Ausleiher> suche(String suchbegriff) {
        String pattern = "%" + suchbegriff.toLowerCase() + "%";
        TypedQuery<AusleiherJpaEntity> query = entityManager.createQuery(
            "SELECT a FROM AusleiherJpaEntity a WHERE " +
            "LOWER(a.vorname) LIKE :pattern OR " +
            "LOWER(a.nachname) LIKE :pattern OR " +
            "LOWER(a.email) LIKE :pattern " +
            "ORDER BY a.nachname, a.vorname",
            AusleiherJpaEntity.class
        );
        query.setParameter("pattern", pattern);
        return query.getResultList().stream()
            .map(AusleiherJpaEntity::toDomain)
            .toList();
    }

    @Override
    public void loeschen(UUID id) {
        inTransaction(() -> {
            AusleiherJpaEntity entity = entityManager.find(AusleiherJpaEntity.class, id);
            if (entity != null) {
                entityManager.remove(entity);
            }
        });
    }

    @Override
    public boolean existiertEmail(String email) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(a) FROM AusleiherJpaEntity a WHERE LOWER(a.email) = LOWER(:email)",
            Long.class
        );
        query.setParameter("email", email);
        return query.getSingleResult() > 0;
    }

    @Override
    public long zaehleAlle() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(a) FROM AusleiherJpaEntity a",
            Long.class
        );
        return query.getSingleResult();
    }
}
