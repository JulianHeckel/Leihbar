package de.dhbw.leihbar.infrastructure.persistence;

import de.dhbw.leihbar.domain.aggregates.Gegenstand;
import de.dhbw.leihbar.domain.repositories.GegenstandRepository;
import de.dhbw.leihbar.domain.valueobjects.InventarNummer;
import de.dhbw.leihbar.domain.valueobjects.Kategorie;
import de.dhbw.leihbar.domain.valueobjects.VerfuegbarkeitsStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA-Implementierung des GegenstandRepository.
 * Bildet die Schnittstelle zwischen Domain und Datenbank.
 */
public class JpaGegenstandRepository implements GegenstandRepository {

    private final EntityManager entityManager;

    public JpaGegenstandRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Gegenstand speichern(Gegenstand gegenstand) {
        GegenstandJpaEntity entity = GegenstandJpaEntity.fromDomain(gegenstand);

        entityManager.getTransaction().begin();
        try {
            if (entityManager.find(GegenstandJpaEntity.class, entity.getId()) != null) {
                entityManager.merge(entity);
            } else {
                entityManager.persist(entity);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }

        return gegenstand;
    }

    @Override
    public Optional<Gegenstand> findeNachId(UUID id) {
        GegenstandJpaEntity entity = entityManager.find(GegenstandJpaEntity.class, id);
        return Optional.ofNullable(entity).map(GegenstandJpaEntity::toDomain);
    }

    @Override
    public Optional<Gegenstand> findeNachInventarNummer(InventarNummer inventarNummer) {
        try {
            TypedQuery<GegenstandJpaEntity> query = entityManager.createQuery(
                "SELECT g FROM GegenstandJpaEntity g WHERE g.inventarNummer = :invNr",
                GegenstandJpaEntity.class
            );
            query.setParameter("invNr", inventarNummer.getValue());
            return Optional.of(query.getSingleResult().toDomain());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Gegenstand> findeAlle() {
        TypedQuery<GegenstandJpaEntity> query = entityManager.createQuery(
            "SELECT g FROM GegenstandJpaEntity g ORDER BY g.inventarNummer",
            GegenstandJpaEntity.class
        );
        return query.getResultList().stream()
            .map(GegenstandJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Gegenstand> findeNachStatus(VerfuegbarkeitsStatus status) {
        TypedQuery<GegenstandJpaEntity> query = entityManager.createQuery(
            "SELECT g FROM GegenstandJpaEntity g WHERE g.status = :status ORDER BY g.inventarNummer",
            GegenstandJpaEntity.class
        );
        query.setParameter("status", status);
        return query.getResultList().stream()
            .map(GegenstandJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Gegenstand> findeVerfuegbare() {
        return findeNachStatus(VerfuegbarkeitsStatus.VERFUEGBAR);
    }

    @Override
    public List<Gegenstand> findeNachKategorie(Kategorie kategorie) {
        TypedQuery<GegenstandJpaEntity> query = entityManager.createQuery(
            "SELECT g FROM GegenstandJpaEntity g WHERE g.kategorieName = :kat ORDER BY g.inventarNummer",
            GegenstandJpaEntity.class
        );
        query.setParameter("kat", kategorie.getName());
        return query.getResultList().stream()
            .map(GegenstandJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Gegenstand> suche(String suchbegriff) {
        String pattern = "%" + suchbegriff.toLowerCase() + "%";
        TypedQuery<GegenstandJpaEntity> query = entityManager.createQuery(
            "SELECT g FROM GegenstandJpaEntity g WHERE " +
            "LOWER(g.name) LIKE :pattern OR LOWER(g.beschreibung) LIKE :pattern " +
            "OR LOWER(g.kategorieName) LIKE :pattern OR LOWER(g.inventarNummer) LIKE :pattern " +
            "ORDER BY g.inventarNummer",
            GegenstandJpaEntity.class
        );
        query.setParameter("pattern", pattern);
        return query.getResultList().stream()
            .map(GegenstandJpaEntity::toDomain)
            .toList();
    }

    @Override
    public void loeschen(UUID id) {
        entityManager.getTransaction().begin();
        try {
            GegenstandJpaEntity entity = entityManager.find(GegenstandJpaEntity.class, id);
            if (entity != null) {
                entityManager.remove(entity);
            }
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw e;
        }
    }

    @Override
    public boolean existiertInventarNummer(InventarNummer inventarNummer) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(g) FROM GegenstandJpaEntity g WHERE g.inventarNummer = :invNr",
            Long.class
        );
        query.setParameter("invNr", inventarNummer.getValue());
        return query.getSingleResult() > 0;
    }

    @Override
    public InventarNummer naechsteFreieInventarNummer() {
        TypedQuery<String> query = entityManager.createQuery(
            "SELECT g.inventarNummer FROM GegenstandJpaEntity g ORDER BY g.inventarNummer DESC",
            String.class
        );
        query.setMaxResults(1);

        List<String> results = query.getResultList();
        if (results.isEmpty()) {
            return InventarNummer.of(1);
        }

        InventarNummer letzte = InventarNummer.of(results.get(0));
        return InventarNummer.of(letzte.getNumericPart() + 1);
    }

    @Override
    public long zaehleAlle() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(g) FROM GegenstandJpaEntity g",
            Long.class
        );
        return query.getSingleResult();
    }

    @Override
    public List<String> findeAlleKategorienamen() {
        TypedQuery<String> query = entityManager.createQuery(
            "SELECT DISTINCT g.kategorieName FROM GegenstandJpaEntity g ORDER BY g.kategorieName",
            String.class
        );
        return query.getResultList();
    }

    @Override
    public long zaehleNachStatus(VerfuegbarkeitsStatus status) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(g) FROM GegenstandJpaEntity g WHERE g.status = :status",
            Long.class
        );
        query.setParameter("status", status);
        return query.getSingleResult();
    }
}
