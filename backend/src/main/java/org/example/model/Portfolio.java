package org.example.model;

import java.util.ArrayList;
import java.util.List;

/**
 * La classe Portfolio représente le portefeuille d'investissement
 * d'un utilisateur. Elle contient plusieurs actifs (actions, cryptos, etc.)
 * et permet de calculer leur valeur totale.
 */
public class Portfolio {

    private int id;                  // Identifiant unique du portefeuille
    private String userID;           // ✅ Identifiant de l'utilisateur propriétaire
    private List<Asset> assets;      // Liste des actifs détenus

    // Constructeur par défaut
    public Portfolio() {
        this.assets = new ArrayList<>();
    }

    // Constructeur avec ID portefeuille
    public Portfolio(int id) {
        this.id = id;
        this.assets = new ArrayList<>();
    }

    // ✅ Constructeur complet (avec userID)
    public Portfolio(int id, String userID) {
        this.id = id;
        this.userID = userID;
        this.assets = new ArrayList<>();
    }

    // --- Getters & Setters ---
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
        this.userID = userID;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }

    // --- Gestion des actifs ---
    public void addAsset(Asset asset) {
        assets.add(asset);
    }

    public void removeAsset(String assetName) {
        assets.removeIf(a -> a.getAssetName().equalsIgnoreCase(assetName));
    }

    /**
     * Calcule la valeur totale du portefeuille.
     */
    public double getTotalValue() {
        double total = 0;
        for (Asset a : assets) {
            total += a.getTotalValue();
        }
        return total;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Portfolio (ID: " + id + ", UserID: " + userID + ")\n");
        for (Asset a : assets) {
            sb.append(" - ").append(a.toString()).append("\n");
        }
        sb.append("Valeur totale du portefeuille : ").append(getTotalValue()).append(" CHF");
        return sb.toString();
    }
}