package org.groupm.ewallet.service.connector;

import org.groupm.ewallet.model.Asset;
import org.groupm.ewallet.model.Portfolio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service responsable de la gestion et de la mise à jour des données de marché.
 * Récupère les prix des actifs via connecteur/externe,
 * gère un cache local d'actifs pour usages techniques.
 */
public class MarketDataService {

    private final MarketDataConnector connector;
    private final List<Asset> localAssets;

    /**
     * Constructeur standard, utilisé en production.
     * @param connector connecteur API marché
     */
    public MarketDataService(MarketDataConnector connector) {
        this.connector = connector;
        this.localAssets = new ArrayList<>();
    }

    /**
     * Constructeur alternatif pour test/démo : passe un jeu de données externes.
     * @param connector connecteur API marché
     * @param demoAssets liste d’actifs fictifs à injecter (pour tests)
     */
    public MarketDataService(MarketDataConnector connector, List<Asset> demoAssets) {
        this.connector = connector;
        this.localAssets = new ArrayList<>(demoAssets);
    }

    // Plus aucune logique de "populateDemoAssets" ici : à externaliser.
    // Exemple dans TestDataInitializer (cf plus bas).

    public void refreshPortfolioPricesUsd(Portfolio portfolio) {
        if (portfolio == null || portfolio.getAssets() == null || connector == null) {
            return;
        }

        for (Asset asset : portfolio.getAssets()) {
            String type = asset.getType();
            String symbol = asset.getSymbol();
            if (symbol == null || symbol.isBlank()) continue;

            double priceUsd;
            try {
                if ("crypto".equalsIgnoreCase(type)) {
                    priceUsd = connector.getCryptoPriceUsd(symbol);
                } else {
                    priceUsd = connector.getQuotePriceUsd(symbol);
                }
                double priceChf = CurrencyConverter.usdToChf(priceUsd);
                asset.setUnitValue(priceChf);
            } catch (Exception e) {
                // Utilise un système de log en prod :
                System.err.println("Erreur MAJ actif [" +
                        asset.getAssetName() + "] (" + symbol + ") : " + e.getMessage());
            }
        }
    }

    public List<Asset> getAllAssets() {
        return new ArrayList<>(localAssets);
    }

    public Asset getAssetBySymbol(String symbol) {
        if (symbol == null) return null;
        return localAssets.stream()
                .filter(a -> symbol.equalsIgnoreCase(a.getSymbol()))
                .findFirst()
                .orElse(null);
    }

    public void addAsset(Asset asset) {
        if (asset != null && localAssets.stream().noneMatch(a ->
                Objects.equals(a.getSymbol(), asset.getSymbol()))) {
            localAssets.add(asset);
        }
    }
}
