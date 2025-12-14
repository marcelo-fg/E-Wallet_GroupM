package org.groupm.ewallet.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a portfolio transaction (BUY or SELL of an asset).
 * This is separate from bank Account transactions.
 */
@Entity
@Table(name = "portfolio_transactions")
public class PortfolioTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    /** Portfolio ID this transaction belongs to. */
    @Column(name = "portfolio_id", nullable = false)
    private int portfolioId;

    /** Asset symbol (e.g., BTC, AAPL). */
    @Column(name = "symbol", nullable = false)
    private String symbol;

    /** Asset name (e.g., Bitcoin, Apple Inc.). */
    @Column(name = "asset_name")
    private String assetName;

    /** Transaction type: BUY or SELL. */
    @Column(name = "type", nullable = false)
    private String type;

    /** Quantity of assets traded. */
    @Column(name = "quantity", precision = 19, scale = 8, nullable = false)
    private BigDecimal quantity = BigDecimal.ZERO;

    /** Unit price at time of transaction. */
    @Column(name = "unit_price", precision = 19, scale = 4, nullable = false)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    /** Total value of the transaction (quantity * unitPrice). */
    @Column(name = "total_value", precision = 19, scale = 4)
    private BigDecimal totalValue = BigDecimal.ZERO;

    /** Timestamp of the transaction. */
    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    /** Version for optimistic locking. */
    @Version
    private Long version;

    // ===================== Constructors =====================

    public PortfolioTransaction() {
        this.timestamp = LocalDateTime.now();
    }

    public PortfolioTransaction(int portfolioId, String symbol, String assetName,
            String type, double quantity, double unitPrice) {
        this();
        this.portfolioId = portfolioId;
        this.symbol = symbol;
        this.assetName = assetName;
        this.type = type;
        this.quantity = BigDecimal.valueOf(quantity);
        this.unitPrice = BigDecimal.valueOf(unitPrice);
        this.totalValue = this.quantity.multiply(this.unitPrice);
    }

    // ===================== Lifecycle Callbacks =====================

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
        if (this.totalValue == null || this.totalValue.equals(BigDecimal.ZERO)) {
            this.totalValue = this.quantity.multiply(this.unitPrice);
        }
    }

    // ===================== Getters =====================

    public long getId() {
        return id;
    }

    public int getPortfolioId() {
        return portfolioId;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getAssetName() {
        return assetName;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getQuantity() {
        return quantity != null ? quantity : BigDecimal.ZERO;
    }

    public double getQuantityAsDouble() {
        return quantity != null ? quantity.doubleValue() : 0.0;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice != null ? unitPrice : BigDecimal.ZERO;
    }

    public double getUnitPriceAsDouble() {
        return unitPrice != null ? unitPrice.doubleValue() : 0.0;
    }

    public BigDecimal getTotalValue() {
        return totalValue != null ? totalValue : BigDecimal.ZERO;
    }

    public double getTotalValueAsDouble() {
        return totalValue != null ? totalValue.doubleValue() : 0.0;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Long getVersion() {
        return version;
    }

    // ===================== Setters =====================

    public void setId(long id) {
        this.id = id;
    }

    public void setPortfolioId(int portfolioId) {
        this.portfolioId = portfolioId;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity != null ? quantity : BigDecimal.ZERO;
        recalculateTotalValue();
    }

    public void setQuantity(double quantity) {
        this.quantity = BigDecimal.valueOf(quantity);
        recalculateTotalValue();
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice != null ? unitPrice : BigDecimal.ZERO;
        recalculateTotalValue();
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = BigDecimal.valueOf(unitPrice);
        recalculateTotalValue();
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    // ===================== Helper Methods =====================

    private void recalculateTotalValue() {
        if (this.quantity != null && this.unitPrice != null) {
            this.totalValue = this.quantity.multiply(this.unitPrice);
        }
    }

    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return timestamp != null ? timestamp.format(formatter) : "";
    }

    /**
     * Returns signed notional value (positive for BUY, negative for SELL).
     */
    public double getSignedNotional() {
        double sign = "SELL".equalsIgnoreCase(type) ? -1.0 : 1.0;
        return sign * getTotalValueAsDouble();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %s %.4f @ %.2f = %.2f",
                getFormattedTimestamp(), type, symbol, getQuantityAsDouble(),
                getUnitPriceAsDouble(), getTotalValueAsDouble());
    }
}
