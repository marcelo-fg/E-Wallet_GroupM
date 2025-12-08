package org.groupm.ewallet.webapp.model;

import java.time.LocalDateTime;

/**
 * In-memory portfolio trade used for PnL and global transaction view.
 */
public class PortfolioTrade {
    private final int portfolioId;
    private final String assetName;
    private final String symbol;
    private final String type; // "BUY" or "SELL"
    private final double quantity;
    private final double unitPrice; // trade price in USD for now
    private final LocalDateTime dateTime;

    public PortfolioTrade(int portfolioId,
            String assetName,
            String symbol,
            String type,
            double quantity,
            double unitPrice,
            LocalDateTime dateTime) {
        this.portfolioId = portfolioId;
        this.assetName = assetName;
        this.symbol = symbol;
        this.type = type;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.dateTime = dateTime;
    }

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

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Signed notional value of the trade.
     * BUY is positive, SELL is negative.
     */
    public double getSignedNotional() {
        double sign = "SELL".equalsIgnoreCase(type) ? -1.0 : 1.0;
        return sign * quantity * unitPrice;
    }
}
