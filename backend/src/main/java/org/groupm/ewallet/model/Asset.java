package org.groupm.ewallet.model;

import jakarta.persistence.*;

/**
 * Représente un actif financier (action, crypto, ETF, etc.).
 */
@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private int id;

    /** Symbole unique de l’actif, par exemple "AAPL" ou "bitcoin". */
    private String symbol;

    /** Type d’actif, par exemple "stock", "crypto" ou "etf". */
    private String type;

    /** Nom complet de l’actif. */
    @Column(name = "asset_name")
    private String assetName;

    /** Valeur unitaire de l’actif. */
    @Column(name = "unit_value")
    private double unitValue;

    /** Quantité détenue de cet actif. */
    private double quantity;

    @Column(name = "portfolio_id", insertable = false, updatable = false)
    private int portfolioID;

    // ===================== Constructeurs =====================

    public Asset() {
    }

    public Asset(String symbol, String type, String assetName, double unitValue) {
        this.symbol = symbol;
        this.type = type;
        this.assetName = assetName;
        this.unitValue = unitValue;
        this.quantity = 0.0;
    }

    public Asset(String assetName, String type, double quantity, double unitValue, String symbol) {
        this.assetName = assetName;
        this.type = type;
        this.quantity = quantity;
        this.unitValue = unitValue;
        this.symbol = symbol;
    }

    // ===================== Getters et Setters =====================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("unitPrice")
    public double getUnitValue() {
        return unitValue;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("unitPrice")
    public void setUnitValue(double unitValue) {
        this.unitValue = unitValue;
    }

    // JSON-B Compatibility (Payara uses Yasson by default, which might ignore
    // Jackson annotations)
    public double getUnitPrice() {
        return unitValue;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitValue = unitPrice;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public int getPortfolioID() {
        return portfolioID;
    }

    public void setPortfolioID(int portfolioID) {
        this.portfolioID = portfolioID;
    }

    // ===================== Méthodes utilitaires =====================

    public double getTotalValue() {
        return unitValue * quantity;
    }

    @Override
    public String toString() {
        return String.format("%s (%s): %.2f x %.2f", assetName, symbol, unitValue, quantity);
    }
}
