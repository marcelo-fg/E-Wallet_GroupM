package org.example.service;

import org.example.model.Asset;
import org.example.model.Portfolio;
import org.example.service.connector.MarketDataConnector;
import org.example.service.connector.CurrencyConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Service pour la gestion et la mise à jour des données de marché.
 */
public class MarketDataService {

    private final MarketDataConnector connector;
    private final List<Asset> localAssets;

    public MarketDataService() {
        this.connector = null;
        this.localAssets = new ArrayList<>();

        localAssets.add(new Asset("bitcoin", "crypto", "Bitcoin", 65000.0));
        localAssets.add(new Asset("ethereum", "crypto", "Ethereum", 3500.0));
        localAssets.add(new Asset("AAPL", "stock", "Apple Inc.", 190.0));
        localAssets.add(new Asset("SPY", "etf", "S&P 500 ETF", 520.0));
    }

    public MarketDataService(MarketDataConnector connector) {
        this.connector = connector;
        this.localAssets = new ArrayList<>();
    }

    public void refreshPortfolioPricesUsd(Portfolio portfolio) {
        if (portfolio == null || portfolio.getAssets() == null) return;

        for (Asset a : portfolio.getAssets()) {
            String type = a.getType();
            String symbol = a.getSymbol();
            if (symbol == null || symbol.isBlank()) continue;

            try {
                double priceUsd;
                if (connector == null) continue;

                if ("crypto".equalsIgnoreCase(type)) {
                    priceUsd = connector.getCryptoPriceUsd(symbol);
                } else {
                    priceUsd = connector.getQuotePriceUsd(symbol);
                }

                double priceChf = CurrencyConverter.usdToChf(priceUsd);
                a.setUnitValue(priceChf);

            } catch (Exception e) {
                System.err.println("⚠️ Impossible de mettre à jour " +
                        a.getAssetName() + " (" + symbol + "): " + e.getMessage());
            }
        }
    }

    public List<Asset> getAllAssets() {
        return new ArrayList<>(localAssets);
    }

    public Asset getAssetBySymbol(String symbol) {
        if (symbol == null) return null;
        for (Asset asset : localAssets) {
            if (asset.getSymbol().equalsIgnoreCase(symbol)) {
                return asset;
            }
        }
        return null;
    }

    public void addAsset(Asset asset) {
        if (asset != null) {
            localAssets.add(asset);
        }
    }
}