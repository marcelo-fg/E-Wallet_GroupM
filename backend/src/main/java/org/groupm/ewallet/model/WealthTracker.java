package org.groupm.ewallet.model;

import org.groupm.ewallet.service.connector.CurrencyConverter;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente le suivi de la richesse totale d’un utilisateur (comptes + portefeuille).
 * Cette classe permet de calculer et d’afficher la valeur du patrimoine
 * en USD et en CHF, ainsi que son taux de croissance dans le temps.
 */
public class WealthTracker {

    /** Utilisateur dont la richesse est suivie. */
    private User user;

    /** Valeur totale actuelle du patrimoine en USD. */
    private double totalWealth;

    /** Taux de croissance du patrimoine depuis le premier enregistrement. */
    private double growthRate;

    /** Historique des valeurs du patrimoine (en USD). */
    private final List<Double> historicalValues;

    /**
     * Constructeur principal.
     * Initialise un WealthTracker lié à un utilisateur spécifique.
     *
     * @param user utilisateur concerné
     */
    public WealthTracker(User user) {
        this.user = user;
        this.totalWealth = 0;
        this.growthRate = 0;
        this.historicalValues = new ArrayList<>();
    }

    /**
     * Constructeur alternatif utilisé pour initialiser un WealthTracker
     * avec un identifiant d’utilisateur et une valeur initiale.
     *
     * @param userId identifiant de l’utilisateur
     * @param totalWealth valeur totale initiale du patrimoine en USD
     */

    /**
     * Renvoie l'utilisateur suivi par le WealthTracker.
     * @return l'utilisateur associé
     */
    public User getUser() {
        return user;
    }

    public WealthTracker(int userId, double totalWealth) {
        this.user = new User(String.valueOf(userId), "", "", "", "");
        this.totalWealth = totalWealth;
        this.growthRate = 0;
        this.historicalValues = new ArrayList<>();
        this.historicalValues.add(totalWealth);
    }

    /**
     * Met à jour la richesse totale de l’utilisateur en USD
     * et recalcule le taux de croissance depuis la première mesure.
     */
    public void updateWealth() {
        double accountsTotalUsd = user.getTotalBalance(); // Valeur en CHF
        double accountsTotalConverted = CurrencyConverter.chfToUsd(accountsTotalUsd);

        double portfolioUsd = 0;
        if (user.getPortfolio() != null && user.getPortfolio().getAssets() != null) {
            for (Asset asset : user.getPortfolio().getAssets()) {
                double valueChf = asset.getTotalValue();
                portfolioUsd += CurrencyConverter.chfToUsd(valueChf);
            }
        }

        totalWealth = accountsTotalConverted + portfolioUsd;

        // Mise à jour de l'historique et calcul du taux de croissance
        historicalValues.add(totalWealth);
        if (historicalValues.size() > 1) {
            double initialValue = historicalValues.get(0);
            if (initialValue != 0) {
                growthRate = ((totalWealth - initialValue) / initialValue) * 100;
            }
        }
    }

    /**
     * Retourne la valeur totale actuelle du patrimoine en USD.
     *
     * @return richesse totale en USD
     */
    public double getTotalWealth() {
        return totalWealth;
    }

    /**
     * Retourne la valeur totale actuelle du patrimoine en CHF.
     *
     * @return richesse totale convertie en CHF
     */
    public double getTotalWealthChf() {
        return CurrencyConverter.usdToChf(totalWealth);
    }

    /**
     * Retourne le taux de croissance actuel du patrimoine.
     *
     * @return taux de croissance en pourcentage
     */
    public double getGrowthRate() {
        return growthRate;
    }

    /**
     * Retourne une représentation textuelle claire du suivi de patrimoine.
     *
     * @return description complète du WealthTracker
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