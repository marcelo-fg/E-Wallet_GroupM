package org.groupm.ewallet.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente le portefeuille d’investissement d’un utilisateur.
 * Un portefeuille contient plusieurs actifs (actions, cryptomonnaies, etc.)
 * et permet de calculer leur valeur totale.
 */
public class Portfolio {

    /** Identifiant unique du portefeuille. */
    private int id;

    /** Identifiant de l’utilisateur propriétaire du portefeuille. */
    private String userID;

    /** Liste des actifs détenus dans le portefeuille. */
    private List<Asset> assets;

    /** Valeur totale du portefeuille. */
    private double totalValue;

    /**
     * Constructeur par défaut.
     * Initialise une liste vide d’actifs.
     */
    public Portfolio() {
        this.assets = new ArrayList<>();
    }

    /**
     * Constructeur initialisant le portefeuille avec un identifiant.
     *
     * @param id identifiant unique du portefeuille
     */
    public Portfolio(int id) {
        this.id = id;
        this.assets = new ArrayList<>();
    }

    /**
     * Constructeur complet initialisant le portefeuille avec un identifiant et un identifiant d’utilisateur.
     *
     * @param id identifiant unique du portefeuille
     * @param userID identifiant de l’utilisateur propriétaire
     */
    public Portfolio(int id, String userID) {
        this.id = id;
        this.userID = userID;
        this.assets = new ArrayList<>();
    }

    // ===================== Getters et Setters =====================

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

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    // ===================== Gestion des actifs =====================

    /**
     * Ajoute un actif au portefeuille.
     *
     * @param asset actif à ajouter
     */
    public void addAsset(Asset asset) {
        assets.add(asset);
        double total = 0.0;
        for (Asset a : assets) {
            total += a.getTotalValue();
        }
        this.totalValue = total;
    }

    /**
     * Supprime un actif du portefeuille en fonction de son nom.
     * La recherche est insensible à la casse.
     *
     * @param assetName nom de l’actif à supprimer
     */
    public void removeAsset(String assetName) {
        assets.removeIf(a -> a.getAssetName().equalsIgnoreCase(assetName));
        double total = 0.0;
        for (Asset a : assets) {
            total += a.getTotalValue();
        }
        this.totalValue = total;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Portfolio (ID: " + id + ", UserID: " + userID + ")\n");
        for (Asset asset : assets) {
            sb.append(" - ").append(asset.toString()).append("\n");
        }
        sb.append("Valeur totale du portefeuille : ").append(getTotalValue()).append(" CHF");
        return sb.toString();
    }
}