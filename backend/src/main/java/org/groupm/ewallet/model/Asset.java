package org.groupm.ewallet.model;

import jakarta.persistence.*;
import jakarta.json.bind.annotation.JsonbTransient;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Représente un actif financier (action, crypto, ETF, etc.).
 * Utilise BigDecimal pour les valeurs financières afin de garantir la
 * précision.
 */
@Entity
@Table(name = "assets")
public class Asset implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "asset_id")
    private int id;

    /** Symbole unique de l'actif, par exemple "AAPL" ou "bitcoin". */
    private String symbol;

    /** Type d'actif, par exemple "stock", "crypto" ou "etf". */
    private String type;

    /** Nom complet de l'actif. */
    @Column(name = "asset_name")
    private String assetName;

    /** Valeur unitaire de l'actif - BigDecimal pour précision financière. */
    @Column(name = "unit_value", precision = 19, scale = 8)
    private BigDecimal unitValue = BigDecimal.ZERO;

    /** Quantité détenue de cet actif - BigDecimal pour crypto notamment. */
    @Column(precision = 19, scale = 8)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "portfolio_id", insertable = false, updatable = false)
    private int portfolioID;

    /** Relation vers le portfolio propriétaire. */
    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    /** Version pour optimistic locking - détection des conflits concurrents. */
    @Version
    private Long version;

    // ===================== Constructeurs =====================

    public Asset() {
        this.unitValue = BigDecimal.ZERO;
        this.quantity = BigDecimal.ZERO;
    }

    public Asset(String symbol, String type, String assetName, double unitValue) {
        this.symbol = symbol;
        this.type = type;
        this.assetName = assetName;
        this.unitValue = BigDecimal.valueOf(unitValue);
        this.quantity = BigDecimal.ZERO;
    }

    public Asset(String symbol, String type, String assetName, BigDecimal unitValue) {
        this.symbol = symbol;
        this.type = type;
        this.assetName = assetName;
        this.unitValue = unitValue != null ? unitValue : BigDecimal.ZERO;
        this.quantity = BigDecimal.ZERO;
    }

    public Asset(String assetName, String type, double quantity, double unitValue, String symbol) {
        this.assetName = assetName;
        this.type = type;
        this.quantity = BigDecimal.valueOf(quantity);
        this.unitValue = BigDecimal.valueOf(unitValue);
        this.symbol = symbol;
    }

    public Asset(String assetName, String type, BigDecimal quantity, BigDecimal unitValue, String symbol) {
        this.assetName = assetName;
        this.type = type;
        this.quantity = quantity != null ? quantity : BigDecimal.ZERO;
        this.unitValue = unitValue != null ? unitValue : BigDecimal.ZERO;
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

    /**
     * Retourne la valeur unitaire en BigDecimal (méthode principale).
     */
    public BigDecimal getUnitValueAsBigDecimal() {
        return unitValue != null ? unitValue : BigDecimal.ZERO;
    }

    /**
     * Retourne la valeur unitaire en double pour rétrocompatibilité.
     * 
     * @deprecated Utiliser getUnitValueAsBigDecimal() pour précision financière.
     */
    @Deprecated
    @com.fasterxml.jackson.annotation.JsonProperty("unitPrice")
    public double getUnitValue() {
        return unitValue != null ? unitValue.doubleValue() : 0.0;
    }

    /**
     * Définit la valeur unitaire avec un BigDecimal.
     */
    public void setUnitValue(BigDecimal unitValue) {
        this.unitValue = unitValue != null ? unitValue : BigDecimal.ZERO;
    }

    /**
     * Définit la valeur unitaire avec un double pour rétrocompatibilité.
     * 
     * @deprecated Utiliser setUnitValue(BigDecimal) pour précision financière.
     */
    @Deprecated
    @com.fasterxml.jackson.annotation.JsonProperty("unitPrice")
    public void setUnitValue(double unitValue) {
        this.unitValue = BigDecimal.valueOf(unitValue);
    }

    // JSON-B Compatibility
    public double getUnitPrice() {
        return unitValue != null ? unitValue.doubleValue() : 0.0;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitValue = BigDecimal.valueOf(unitPrice);
    }

    /**
     * Retourne la quantité en BigDecimal (méthode principale).
     */
    public BigDecimal getQuantityAsBigDecimal() {
        return quantity != null ? quantity : BigDecimal.ZERO;
    }

    /**
     * Retourne la quantité en double pour rétrocompatibilité.
     * 
     * @deprecated Utiliser getQuantityAsBigDecimal() pour précision.
     */
    @Deprecated
    public double getQuantity() {
        return quantity != null ? quantity.doubleValue() : 0.0;
    }

    /**
     * Définit la quantité avec un BigDecimal.
     */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity != null ? quantity : BigDecimal.ZERO;
    }

    /**
     * Définit la quantité avec un double pour rétrocompatibilité.
     * 
     * @deprecated Utiliser setQuantity(BigDecimal) pour précision.
     */
    @Deprecated
    public void setQuantity(double quantity) {
        this.quantity = BigDecimal.valueOf(quantity);
    }

    public int getPortfolioID() {
        return portfolioID;
    }

    public void setPortfolioID(int portfolioID) {
        // Deprecated - use setPortfolio(Portfolio) instead
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    // ===================== Méthodes utilitaires =====================

    /**
     * Retourne la valeur totale en BigDecimal.
     */
    public BigDecimal getTotalValueAsBigDecimal() {
        BigDecimal uv = unitValue != null ? unitValue : BigDecimal.ZERO;
        BigDecimal qty = quantity != null ? quantity : BigDecimal.ZERO;
        return uv.multiply(qty);
    }

    /**
     * Retourne la valeur totale en double pour rétrocompatibilité.
     */
    public double getTotalValue() {
        return getTotalValueAsBigDecimal().doubleValue();
    }

    @Override
    public String toString() {
        return String.format("%s (%s): %s x %s", assetName, symbol, unitValue, quantity);
    }
}
