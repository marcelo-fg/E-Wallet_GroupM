package org.groupm.ewallet.webapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.groupm.ewallet.webapp.connector.ExternalAsset;

import java.util.List;

/**
 * Facade service for external market data APIs (crypto, stocks, ETFs).
 * Delegates to specific API services.
 */
@ApplicationScoped
public class MarketDataService {

    @Inject
    private CoinGeckoService coinGecko;

    @Inject
    private FinnhubService finnhub;

    /**
     * High-level API used by the UI to load external assets by type.
     */
    public List<ExternalAsset> loadAssetsFromApi(String type) {
        return switch (type.toLowerCase()) {
            case "crypto" -> coinGecko.loadCryptoAssets();
            case "stock" -> finnhub.loadStockAssets();
            case "etf" -> finnhub.loadEtfAssets();
            default -> List.of();
        };
    }

    /**
     * High-level lookup for a single asset price based on its id or symbol.
     */
    public double getPriceForAsset(String idOrSymbol, String type) {
        return switch (type.toLowerCase()) {
            case "crypto" -> coinGecko.getCryptoPrice(idOrSymbol);
            case "stock", "etf" -> finnhub.getStockEtfPrice(idOrSymbol);
            default -> 0.0;
        };
    }
}
