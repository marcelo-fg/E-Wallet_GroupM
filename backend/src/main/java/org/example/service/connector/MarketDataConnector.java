package org.example.service.connector;

public interface MarketDataConnector {
    double getCryptoPriceUsd(String coingeckoId) throws Exception;   // ex: "bitcoin", "ethereum"
    double getQuotePriceUsd(String symbol) throws Exception;         // ex: "AAPL", "SPY"
}