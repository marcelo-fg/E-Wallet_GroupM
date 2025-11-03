// src/main/java/org/example/model/Asset.java
package org.example.model;

public class Asset {

    private String assetName;   // ex: "Apple", "Bitcoin"
    private String type;        // "stock", "crypto", "etf"
    private double quantity;
    private double unitValue;
    private String symbol;      // ✅ ex: "AAPL", "BTC", "SPY", "BTC" (id CoinGecko: "bitcoin")

    // Ancien constructeur (reste compatible)
    public Asset(String assetName, String type, double quantity, double unitValue) {
        this(assetName, type, quantity, unitValue, null);
    }

    // Nouveau constructeur avec symbol
    public Asset(String assetName, String type, double quantity, double unitValue, String symbol) {
        this.assetName = assetName;
        this.type = type;
        this.quantity = quantity;
        this.unitValue = unitValue;
        this.symbol = symbol;
    }

    public String getAssetName() { return assetName; }
    public String getType() { return type; }
    public double getQuantity() { return quantity; }
    public double getUnitValue() { return unitValue; }
    public String getSymbol() { return symbol; }

    public void setUnitValue(double unitValue) { this.unitValue = unitValue; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public double getTotalValue() { return quantity * unitValue; }

    @Override
    public String toString() {
        double valueChf = getTotalValue();
        double valueUsd = org.example.service.connector.CurrencyConverter.chfToUsd(valueChf);
        double unitUsd = org.example.service.connector.CurrencyConverter.chfToUsd(unitValue);

        String s = symbol != null ? " [" + symbol + "]" : "";
        return assetName + s + " (" + type + ") - " + quantity +
                " unités à " + String.format("%.2f", unitValue) + " CHF" +
                " (≈ " + String.format("%.2f", unitUsd) + " USD)" +
                " -> " + String.format("%.2f", valueChf) + " CHF" +
                " (≈ " + String.format("%.2f", valueUsd) + " USD)";
    }
}