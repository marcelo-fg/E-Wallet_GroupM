package org.groupm.ewallet.dto;

import java.util.List;

/**
 * DTO pour les portefeuilles d’investissement.
 */
public class PortfolioDTO {

    private int id;
    private String userID;
    private List<AssetDTO> assets;
    private double totalValue; // <-- AJOUT IMPORTANT

    // ====== Getters / Setters ======

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

    public List<AssetDTO> getAssets() {
        return assets;
    }

    public void setAssets(List<AssetDTO> assets) {
        this.assets = assets;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }
}