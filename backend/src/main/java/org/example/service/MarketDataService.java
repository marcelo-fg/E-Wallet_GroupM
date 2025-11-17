package org.example.service;

import org.example.model.Asset;
import org.example.model.Portfolio;
import org.example.service.connector.MarketDataConnector;
import org.example.service.connector.CurrencyConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsable de la gestion et de la mise à jour des données de marché.
 * Il permet de récupérer les prix des actifs depuis les connecteurs externes
 * et de mettre à jour les valeurs des portefeuilles.
 */
public class MarketDataService {

    /** Connecteur utilisé pour interagir avec les sources de données de marché (API). */
    private final MarketDataConnector connector;

    /** Liste locale d’actifs disponibles (utilisée comme données par défaut). */
    private final List<Asset> localAssets;

    /**
     * Constructeur par défaut.
     * Initialise une liste d’actifs fictifs à des fins de démonstration.
     */
    public MarketDataService() {
        this.connector = null;
        this.localAssets = new ArrayList<>();

        localAssets.add(new Asset("bitcoin", "crypto", "Bitcoin", 65000.0));
        localAssets.add(new Asset("ethereum", "crypto", "Ethereum", 3500.0));
        localAssets.add(new Asset("AAPL", "stock", "Apple Inc.", 190.0));
        localAssets.add(new Asset("SPY", "etf", "S&P 500 ETF", 520.0));
    }

    /**
     * Constructeur avec connecteur externe.
     *
     * @param connector connecteur de marché à utiliser (ex : API CoinGecko, Alpha Vantage)
     */
    public MarketDataService(MarketDataConnector connector) {
        this.connector = connector;
        this.localAssets = new ArrayList<>();
    }

    /**
     * Met à jour les prix des actifs d’un portefeuille en USD puis les convertit en CHF.
     * Si aucun connecteur n’est configuré, la méthode ne fait rien.
     *
     * @param portfolio portefeuille dont les prix doivent être mis à jour
     */
    public void refreshPortfolioPricesUsd(Portfolio portfolio) {
        if (portfolio == null || portfolio.getAssets() == null) {
            return;
        }

        for (Asset asset : portfolio.getAssets()) {
            String type = asset.getType();
            String symbol = asset.getSymbol();

            if (symbol == null || symbol.isBlank() || connector == null) {
                continue;
            }

            try {
                double priceUsd;

                if ("crypto".equalsIgnoreCase(type)) {
                    priceUsd = connector.getCryptoPriceUsd(symbol);
                } else {
                    priceUsd = connector.getQuotePriceUsd(symbol);
                }

                double priceChf = CurrencyConverter.usdToChf(priceUsd);
                asset.setUnitValue(priceChf);

            } catch (Exception e) {
                System.err.println("Erreur lors de la mise à jour de " +
                        asset.getAssetName() + " (" + symbol + ") : " + e.getMessage());
            }
        }
    }

    /**
     * Retourne la liste complète des actifs disponibles localement.
     *
     * @return liste des actifs enregistrés localement
     */
    public List<Asset> getAllAssets() {
        return new ArrayList<>(localAssets);
    }

    /**
     * Recherche un actif à partir de son symbole.
     *
     * @param symbol symbole de l’actif (ex : "AAPL", "BTC")
     * @return l’actif correspondant ou null s’il n’existe pas
     */
    public Asset getAssetBySymbol(String symbol) {
        if (symbol == null) {
            return null;
        }

        for (Asset asset : localAssets) {
            if (asset.getSymbol().equalsIgnoreCase(symbol)) {
                return asset;
            }
        }
        return null;
    }

    /**
     * Ajoute un nouvel actif à la liste locale.
     *
     * @param asset actif à ajouter
     */
    public void addAsset(Asset asset) {
        if (asset != null) {
            localAssets.add(asset);
        }
    }
}