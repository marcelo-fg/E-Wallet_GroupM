package org.groupm.ewallet.webapp.connector;

public class ExternalAsset {

    private String name;   // Nom complet : Bitcoin, Apple Inc.
    private String symbol; // Symbole : BTC, AAPL
    private String apiId;  // ID CoinGecko (bitcoin) OU symbol (AAPL)

    public ExternalAsset(String name, String symbol, String apiId) {
        this.name = name;
        this.symbol = symbol;
        this.apiId = apiId;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getApiId() {
        return apiId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    @Override
    public String toString() {
        return name + " (" + symbol + ")";
    }
}