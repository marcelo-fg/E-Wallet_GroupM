package org.groupm.ewallet.model;

import org.groupm.ewallet.service.connector.CurrencyConverter;
import java.util.ArrayList;
import java.util.List;

/**
 * Suit la richesse totale d’un utilisateur (comptes + portefeuilles).
 * Calcule la valeur en USD, en CHF et le taux de croissance.
 */
public class WealthTracker {

    /** Utilisateur suivi. */
    private User user;

    /** Richesse totale en USD. */
    private double totalWealthUsd;

    /** Taux de croissance depuis la première mesure. */
    private double growthRate;

    /** Historique des valeurs enregistrées (en USD). */
    private final List<Double> historicalValues;

    // ============================================================
    //                        CONSTRUCTEURS
    // ============================================================

    /**
     * Constructeur normal : suivi direct d’un utilisateur.
     */
    public WealthTracker(User user) {
        this.user = user;
        this.totalWealthUsd = 0.0;
        this.growthRate = 0.0;
        this.historicalValues = new ArrayList<>();
    }


    // ============================================================
    //                 CALCUL PRINCIPAL DE LA RICHESSE
    // ============================================================

    /**
     * Met à jour la richesse totale :
     *  - Total des comptes (CHF converti en USD)
     *  - Total de tous les portefeuilles (CHF converti en USD)
     *  - Met à jour : croissance, historique
     */
    public void updateWealth() {

        // =====================
        //   1. Comptes en CHF
        // =====================
        double accountsChf = user.getTotalBalance();
        double accountsUsd = CurrencyConverter.chfToUsd(accountsChf);

        // ================================================
        //   2. Sommation de TOUS les portefeuilles du user
        // ================================================
        double portfoliosUsd = 0.0;

        if (user.getPortfolios() != null) {
            for (Portfolio p : user.getPortfolios()) {
                if (p.getAssets() != null) {
                    for (Asset asset : p.getAssets()) {
                        double assetChf = asset.getTotalValue();
                        portfoliosUsd += CurrencyConverter.chfToUsd(assetChf);
                    }
                }
            }
        }

        // ====================
        //   3. Total Wealth
        // ====================
        totalWealthUsd = accountsUsd + portfoliosUsd;

        // ================================
        //   4. Mise à jour de l’historique
        // ================================
        historicalValues.add(totalWealthUsd);

        // ================================
        //   5. Calcul du taux de croissance
        // ================================
        if (historicalValues.size() > 1) {
            double initial = historicalValues.get(0);
            if (initial != 0) {
                growthRate = ((totalWealthUsd - initial) / initial) * 100.0;
            }
        }
    }

    // ============================================================
    //                     GETTERS CALCULÉS
    // ============================================================

    public double getTotalWealthUsd() {
        return totalWealthUsd;
    }

    public double getTotalWealthChf() {
        return CurrencyConverter.usdToChf(totalWealthUsd);
    }

    public double getGrowthRate() {
        return growthRate;
    }

    public List<Double> getHistoricalValues() {
        return historicalValues;
    }

    public User getUser() {
        return user;
    }

    // ============================================================
    //                     TO STRING
    // ============================================================

    @Override
    public String toString() {
        return String.format(
                "WealthTracker{user=%s, totalWealth=%.2f USD (≈ %.2f CHF), growthRate=%.2f%%}",
                user.getFirstName() + " " + user.getLastName(),
                totalWealthUsd,
                getTotalWealthChf(),
                growthRate
        );
    }
}