package org.example.model;

import org.example.service.connector.CurrencyConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe qui suit la richesse totale d'un utilisateur (comptes + portefeuille)
 * et affiche désormais la valeur en USD et CHF, ainsi que le taux de croissance.
 */
public class WealthTracker {

    private User user;
    private double totalWealth; // valeur actuelle totale (en USD par défaut)
    private double growthRate;
    private final List<Double> historicalValues; // historique en USD

    public WealthTracker(User user) {
        this.user = user;
        this.totalWealth = 0;
        this.growthRate = 0;
        this.historicalValues = new ArrayList<>();
    }

    /**
     * Met à jour la richesse totale en USD et recalcule le taux de croissance.
     */
    public void updateWealth() {
        double accountsTotalUsd = user.getTotalBalance(); // déjà en CHF
        double accountsTotalConverted = CurrencyConverter.chfToUsd(accountsTotalUsd);

        double portfolioUsd = 0;
        for (Asset a : user.getPortfolio().getAssets()) {
            double valueChf = a.getTotalValue();
            portfolioUsd += CurrencyConverter.chfToUsd(valueChf);
        }

        totalWealth = accountsTotalConverted + portfolioUsd;

        // Historique pour calcul du growthRate
        historicalValues.add(totalWealth);
        if (historicalValues.size() > 1) {
            double first = historicalValues.get(0);
            double last = totalWealth;
            growthRate = ((last - first) / first) * 100;
        }
    }

    /**
     * Retourne la valeur totale actuelle du patrimoine en USD.
     */
    public double getTotalWealth() {
        return totalWealth;
    }

    /**
     * Retourne la valeur totale actuelle du patrimoine en CHF.
     */
    public double getTotalWealthChf() {
        return CurrencyConverter.usdToChf(totalWealth);
    }

    public double getGrowthRate() {
        return growthRate;
    }

    /**
     * Affichage clair du suivi de patrimoine.
     */
    @Override
    public String toString() {
        double wealthChf = getTotalWealthChf();
        return String.format(
                "WealthTracker{user=%s, totalWealth=%.2f USD (≈ %.2f CHF), growthRate=%.2f%%}",
                user.getFirstName() + " " + user.getLastName(),
                totalWealth,
                wealthChf,
                growthRate
        );
    }
}