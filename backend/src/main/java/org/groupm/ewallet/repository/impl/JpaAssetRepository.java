package org.groupm.ewallet.repository.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.groupm.ewallet.model.Asset;
import org.groupm.ewallet.repository.AssetRepository;

import java.util.List;

/**
 * JPA implementation of AssetRepository.
 * Uses injected request-scoped EntityManager for proper transaction
 * coordination.
 */
@ApplicationScoped
public class JpaAssetRepository implements AssetRepository {

    @Inject
    private EntityManager em;

    @Override
    public void save(Asset asset) {
        if (asset.getId() == 0) {
            em.persist(asset);
        } else {
            em.merge(asset);
        }
    }

    @Override
    public Asset findById(int id) {
        return em.find(Asset.class, id);
    }

    @Override
    public Asset findBySymbol(String symbol) {
        List<Asset> results = em.createQuery("SELECT a FROM Asset a WHERE a.symbol = :symbol", Asset.class)
                .setParameter("symbol", symbol)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Asset> findByType(String type) {
        return em.createQuery("SELECT a FROM Asset a WHERE a.type = :type", Asset.class)
                .setParameter("type", type)
                .getResultList();
    }

    @Override
    public List<Asset> findAll() {
        return em.createQuery("SELECT a FROM Asset a", Asset.class).getResultList();
    }

    @Override
    public void delete(int id) {
        Asset asset = em.find(Asset.class, id);
        if (asset != null) {
            em.remove(asset);
        }
    }
}
