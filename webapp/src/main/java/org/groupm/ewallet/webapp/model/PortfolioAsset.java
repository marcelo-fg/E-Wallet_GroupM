package org.groupm.ewallet.webapp.model;

import java.time.LocalDateTime;

/**
 * Represents an asset held in a portfolio (in-memory storage).
 * Similar to PortfolioTrade but represents current holdings rather than
 * transactions.
 */
public class PortfolioAsset {
    private int portfolioId;
    private String assetName;
    private String symbol;
    private String type; // crypto, stock, etf
    private double quantity;
    private double unitPrice;
    private LocalDateTime addedAt;

    public PortfolioAsset(int portfolioId, String assetName, String symbol,
            String type, double quantity, double unitPrice,
            LocalDateTime addedAt) {
        this.portfolioId = portfolioId;
        this.assetName = assetName;
        this.symbol = symbol;
        this.type = type;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.addedAt = addedAt;
    }

    // Getters
    public int getPortfolioId() {
        return portfolioId;
    }

    public String getAssetName() {
        return assetName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getType() {
        return type;
    }

    public double getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public double getTotalValue() {
        return quantity * unitPrice;
    }

    // Setters
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
