package org.example.model;

import java.util.ArrayList;
import java.util.List;

/**
 * La classe Portfolio représente le portefeuille d'investissement
 * d'un utilisateur. Elle contient plusieurs actifs (actions, cryptos, etc.)
 * et permet de calculer leur valeur totale.
 */
public class Portfolio {

    private List<Asset> assets; // Liste des actifs détenus

    public Portfolio() {
        this.assets = new ArrayList<>();
    }

    public void addAsset(Asset asset) {
        assets.add(asset);
    }

    public void removeAsset(String assetName) {
        assets.removeIf(a -> a.getAssetName().equalsIgnoreCase(assetName));
    }

    public List<Asset> getAssets() {
        return assets;
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
        StringBuilder sb = new StringBuilder("Portfolio:\n");
        for (Asset a : assets) {
            sb.append(" - ").append(a.toString()).append("\n");
        }
        sb.append("Valeur totale du portefeuille : ").append(getTotalValue()).append(" CHF");
        return sb.toString();
    }
}