package org.groupm.ewallet.model;

import jakarta.persistence.*;
import jakarta.json.bind.annotation.JsonbTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un portefeuille d'investissement.
 * Utilise BigDecimal pour les valeurs financières.
 */
@Entity
@Table(name = "portfolios")
public class Portfolio implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private int id;

    @Column(name = "user_id", insertable = false, updatable = false)
    private String userID;

    @Column(name = "name")
    private String name;

    /** Relation vers l'utilisateur propriétaire. */
    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Asset> assets = new ArrayList<>();

    @Transient // Calculé dynamiquement
    private BigDecimal totalValue = BigDecimal.ZERO;

    /** Version pour optimistic locking - détection des conflits concurrents. */
    @Version
    private Long version;

    // ===================== Constructeurs =====================

    public Portfolio() {
        this.assets = new ArrayList<>();
        this.totalValue = BigDecimal.ZERO;
    }

    public Portfolio(String userID) {
        this();
        this.userID = userID;
    }

    // ===================== Getters / Setters =====================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        // Deprecated - use setUser(User) instead
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = (assets != null) ? assets : new ArrayList<>();
        recalculateTotalValue();
    }

    /**
     * Retourne la valeur totale en BigDecimal.
     */
    public BigDecimal getTotalValueAsBigDecimal() {
        recalculateTotalValueAsBigDecimal();
        return totalValue != null ? totalValue : BigDecimal.ZERO;
    }

    /**
     * Retourne la valeur totale en double pour rétrocompatibilité.
     */
    public double getTotalValue() {
        recalculateTotalValue();
        return totalValue != null ? totalValue.doubleValue() : 0.0;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue != null ? totalValue : BigDecimal.ZERO;
    }

    @Deprecated
    public void setTotalValue(double totalValue) {
        this.totalValue = BigDecimal.valueOf(totalValue);
    }

    // ===================== Gestion des actifs =====================

    public void addAsset(Asset asset) {
        if (asset == null)
            return;

        // Check if an asset with the same symbol already exists
        for (Asset existing : assets) {
            if (existing.getSymbol() != null && existing.getSymbol().equalsIgnoreCase(asset.getSymbol())) {

                // FOUND: Aggregate instead of adding duplicate
                BigDecimal oldQty = existing.getQuantityAsBigDecimal();
                BigDecimal oldPrice = existing.getUnitValueAsBigDecimal();
                BigDecimal newQty = asset.getQuantityAsBigDecimal();
                BigDecimal newPrice = asset.getUnitValueAsBigDecimal();

                BigDecimal totalQty = oldQty.add(newQty);

                // Weighted Average Purchase Price
                if (totalQty.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal weightedAvg = (oldQty.multiply(oldPrice).add(newQty.multiply(newPrice)))
                            .divide(totalQty, 8, java.math.RoundingMode.HALF_UP);
                    existing.setUnitValue(weightedAvg);
                }

                existing.setQuantity(totalQty);
                recalculateTotalValue();
                return; // Done, do not add as new
            }
        }

        // NOT FOUND: Add as new
        asset.setPortfolio(this); // Maintain bidirectional relationship
        assets.add(asset);
        recalculateTotalValue();
    }

    public void removeAsset(String assetName) {
        if (assetName == null)
            return;
        assets.removeIf(a -> assetName.equalsIgnoreCase(a.getAssetName()));
        recalculateTotalValue();
    }

    public void recalculateTotalValueAsBigDecimal() {
        BigDecimal total = BigDecimal.ZERO;
        if (assets != null) {
            for (Asset asset : assets) {
                total = total.add(asset.getTotalValueAsBigDecimal());
            }
        }
        this.totalValue = total;
    }

    public void recalculateTotalValue() {
        recalculateTotalValueAsBigDecimal();
    }

    @Override
    public String toString() {
        return "Portfolio{id=" + id + ", totalValue=" + totalValue + '}';
    }
}
