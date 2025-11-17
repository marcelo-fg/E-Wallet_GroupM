package org.example.service.connector;

/**
 * Interface d’accès aux données de marché externes.
 * Toutes les méthodes retournent des prix exprimés en USD.
 * Implémentations typiques : CoinGecko (cryptomonnaies) et Alpha Vantage (actions/ETF).
 */
public interface MarketDataConnector {

    /**
     * Retourne le prix courant d’une cryptomonnaie en USD via CoinGecko.
     *
     * @param coingeckoId identifiant de l’actif sur CoinGecko (ex. : "bitcoin", "ethereum")
     * @return prix courant en USD
     * @throws Exception en cas d’erreur réseau ou de réponse invalide
     */
    double getCryptoPriceUsd(String coingeckoId) throws Exception;

    /**
     * Retourne le prix courant d’une action ou d’un ETF en USD via Alpha Vantage.
     *
     * @param symbol symbole boursier (ex. : "AAPL", "SPY")
     * @return prix courant en USD
     * @throws Exception en cas d’erreur réseau ou de réponse invalide
     */
    double getQuotePriceUsd(String symbol) throws Exception;
}