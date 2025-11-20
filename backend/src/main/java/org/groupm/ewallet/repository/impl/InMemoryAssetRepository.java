package org.groupm.ewallet.repository.impl;

import org.groupm.ewallet.model.Asset;
import org.groupm.ewallet.repository.AssetRepository;

import java.util.*;

/**
 * Implémentation in-memory de AssetRepository pour tests, démo, ou développement local.
 */
public class InMemoryAssetRepository implements AssetRepository {

    // Stockage en mémoire : symbol -> Asset
    private final Map<String, Asset> assets = new HashMap<>();

    @Override
    public void save(Asset asset) {
        assets.put(asset.getSymbol(), asset);
    }

    @Override
    public Asset findBySymbol(String symbol) {
        return assets.get(symbol);
    }

    @Override
    public void delete(String symbol) {
        assets.remove(symbol);
    }

    @Override
    public List<Asset> findAll() {
        return new ArrayList<>(assets.values());
    }

    @Override
    public List<Asset> findByType(String type) {
        List<Asset> filtered = new ArrayList<>();
        for (Asset asset : assets.values()) {
            if (asset.getType() != null && asset.getType().equals(type)) {
                filtered.add(asset);
            }
        }
        return filtered;
    }
}
