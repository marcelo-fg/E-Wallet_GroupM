package org.example.service;

import org.example.model.Asset;
import org.example.model.Portfolio;
import org.example.service.connector.MarketDataConnector;
import org.example.service.connector.CurrencyConverter;

public class MarketDataService {

    private final MarketDataConnector connector;

    public MarketDataService(MarketDataConnector connector) {
        this.connector = connector;
    }

    /**
     * Met à jour in-place les unitValue des assets selon leur type/symbol.
     * Convention symbol:
     *  - crypto: symbol contient l'ID CoinGecko (ex: "bitcoin", "ethereum")
     *  - stock/etf: symbol contient le ticker (ex: "AAPL", "SPY")
     */
    public void refreshPortfolioPricesUsd(Portfolio portfolio) {
        for (Asset a : portfolio.getAssets()) {
            String type = a.getType();
            String symbol = a.getSymbol();
            if (symbol == null || symbol.isBlank()) continue; // rien à faire

            try {
                double priceUsd;
                if ("crypto".equalsIgnoreCase(type)) {
                    priceUsd = connector.getCryptoPriceUsd(symbol);
                } else { // "stock" ou "etf"
                    priceUsd = connector.getQuotePriceUsd(symbol);
                }
                double priceChf = CurrencyConverter.usdToChf(priceUsd);
                a.setUnitValue(priceChf);
            } catch (Exception e) {
                System.err.println("⚠️ Impossible de mettre à jour " + a.getAssetName() + " (" + symbol + "): " + e.getMessage());
            }
        }
    }
}