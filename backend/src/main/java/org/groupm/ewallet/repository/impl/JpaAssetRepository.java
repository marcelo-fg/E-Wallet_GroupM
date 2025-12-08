package org.groupm.ewallet.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.groupm.ewallet.model.Asset;
import org.groupm.ewallet.repository.AssetRepository;

import java.util.List;

/**
 * JPA implementation of AssetRepository.
 */
public class JpaAssetRepository implements AssetRepository {

    private final EntityManagerFactory emf;

    public JpaAssetRepository() {
        // Simple singleton pattern for EMF in this scope or per-request if managed by
        // container
        // Ideally should be injected, but for simplicity we create it here or reuse a
        // global one.
        // Given existing code pattern:
        this.emf = Persistence.createEntityManagerFactory("ewalletPU");
        System.out.println("[JpaAssetRepository] EntityManagerFactory created successfully");
    }

    @Override
    public void save(Asset asset) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (asset.getId() == 0) {
                em.persist(asset);
            } else {
                em.merge(asset);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Asset findById(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Asset.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public Asset findBySymbol(String symbol) {
        EntityManager em = emf.createEntityManager();
        try {
            List<Asset> results = em.createQuery("SELECT a FROM Asset a WHERE a.symbol = :symbol", Asset.class)
                    .setParameter("symbol", symbol)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Asset> findByType(String type) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT a FROM Asset a WHERE a.type = :type", Asset.class)
                    .setParameter("type", type)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Asset> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT a FROM Asset a", Asset.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Asset asset = em.find(Asset.class, id);
            if (asset != null) {
                em.remove(asset);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
