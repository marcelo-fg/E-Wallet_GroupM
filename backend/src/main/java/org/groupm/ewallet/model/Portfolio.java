package org.groupm.ewallet.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un portefeuille d’investissement.
 */
@Entity
@Table(name = "portfolios")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private int id;

    @Column(name = "user_id", insertable = false, updatable = false)
    private String userID;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "portfolio_id")
    private List<Asset> assets = new ArrayList<>();

    @Transient // Calculé dynamiquement
    private double totalValue;

    // ===================== Constructeurs =====================

    public Portfolio() {
        this.assets = new ArrayList<>();
        this.totalValue = 0.0;
    }

    public Portfolio(String userID) {
        this();
        this.userID = userID;
    }

    // ===================== Getters / Setters =====================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }

    public List<Asset> getAssets() { return assets; }
    public void setAssets(List<Asset> assets) {
        this.assets = (assets != null) ? assets : new ArrayList<>();
        recalculateTotalValue();
    }

    public double getTotalValue() {
        // Recalculer si nécessaire car @Transient
        recalculateTotalValue();
        return totalValue;
    }
    public void setTotalValue(double totalValue) { this.totalValue = totalValue; }

    // ===================== Gestion des actifs =====================

    public void addAsset(Asset asset) {
        if (asset != null) {
            assets.add(asset);
            recalculateTotalValue();
        }
    }

    public void removeAsset(String assetName) {
        if (assetName == null) return;
        assets.removeIf(a -> assetName.equalsIgnoreCase(a.getAssetName()));
        recalculateTotalValue();
    }

    public void recalculateTotalValue() {
        double total = 0.0;
        if (assets != null) {
            for (Asset asset : assets) {
                total += asset.getTotalValue();
            }
        }
        this.totalValue = total;
    }

    @Override
    public String toString() {
        return "Portfolio{id=" + id + ", totalValue=" + totalValue + '}';
    }
}
