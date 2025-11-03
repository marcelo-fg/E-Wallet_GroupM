package org.example.model;

/**
 * Classe représentant un actif (action, crypto, ETF, etc.).
 */
public class Asset {

    private String symbol;     // ex: "AAPL", "bitcoin"
    private String type;       // ex: "stock", "crypto", "etf"
    private String assetName;  // nom complet
    private double unitValue;  // prix unitaire (CHF ou USD selon contexte)
    private double quantity;   // quantité détenue

    // ✅ Constructeur complet utilisé dans MarketDataService
    public Asset(String symbol, String type, String assetName, double unitValue) {
        this.symbol = symbol;
        this.type = type;
        this.assetName = assetName;
        this.unitValue = unitValue;
        this.quantity = 0.0;
    }

    // ✅ Nouveau constructeur compatible avec Main.java
    public Asset(String assetName, String type, double quantity, double unitValue, String symbol) {
        this.assetName = assetName;
        this.type = type;
        this.quantity = quantity;
        this.unitValue = unitValue;
        this.symbol = symbol;
    }

    // ✅ Constructeur alternatif si besoin d’un actif sans valeur initiale
    public Asset(String symbol, String type, String assetName) {
        this(symbol, type, assetName, 0.0);
    }

    // --- Getters et Setters ---
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public double getUnitValue() { return unitValue; }
    public void setUnitValue(double unitValue) { this.unitValue = unitValue; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    // ✅ Méthode pratique pour calculer la valeur totale de l’actif
    public double getTotalValue() {
        return unitValue * quantity;
    }

    @Override
    public String toString() {
        return String.format("%s (%s): %.2f CHF x %.2f = %.2f CHF",
                assetName, symbol, unitValue, quantity, getTotalValue());
    }
}