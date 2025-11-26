package org.groupm.ewallet.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un portefeuille d’investissement.
 * Un utilisateur peut avoir plusieurs portefeuilles.
 */
public class Portfolio {

    /** Générateur simple d'identifiants en mémoire. */
    private static int NEXT_ID = 1;

    /** Identifiant unique du portefeuille. */
    private int id;

    /** Identifiant de l’utilisateur propriétaire. */
    private String userID;

    /** Actifs détenus dans ce portefeuille. */
    private List<Asset> assets = new ArrayList<>();

    /** Valeur totale du portefeuille (somme des actifs). */
    private double totalValue;

    // ===================== Constructeurs =====================

    /**
     * Constructeur par défaut.
     * Génère automatiquement un identifiant unique.
     */
    public Portfolio() {
        this.id = NEXT_ID++;
        this.assets = new ArrayList<>();
        this.totalValue = 0.0;
    }

    /**
     * Constructeur liant directement le portefeuille à un utilisateur.
     * @param userID identifiant de l'utilisateur propriétaire
     */
    public Portfolio(String userID) {
        this();
        this.userID = userID;
    }

    // ===================== Getters / Setters =====================

    public int getId() {
        return id;
    }

    /** Utilisé uniquement si besoin pour restaurer un ID existant. */
    public void setId(int id) {
        this.id = id;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = (assets != null) ? assets : new ArrayList<>();
        recalculateTotalValue();
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    // ===================== Gestion des actifs =====================

    /** Ajoute un actif au portefeuille et met à jour la valeur totale. */
    public void addAsset(Asset asset) {
        if (asset != null) {
            assets.add(asset);
            recalculateTotalValue();
        }
    }

    /** Supprime un actif par son nom (insensible à la casse). */
    public void removeAsset(String assetName) {
        if (assetName == null) return;
        assets.removeIf(a -> assetName.equalsIgnoreCase(a.getAssetName()));
        recalculateTotalValue();
    }

    /** Recalcule la valeur totale à partir de la liste d’actifs. */
    public void recalculateTotalValue() {
        double total = 0.0;
        for (Asset asset : assets) {
            total += asset.getTotalValue();
        }
        this.totalValue = total;
    }

    @Override
    public String toString() {
        return "Portfolio{id=" + id +
                ", userID='" + userID + '\'' +
                ", assets=" + assets.size() +
                ", totalValue=" + totalValue +
                '}';
    }
}