package org.groupm.ewallet.model;

import org.groupm.ewallet.service.CurrencyConverter;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Suit la richesse totale d’un utilisateur (comptes + portefeuilles).
 * Calcule la valeur en USD, en CHF et le taux de croissance.
 */
@Entity
@Table(name = "wealth_trackers")
public class WealthTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Utilisateur suivi. */
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    /** Richesse totale en USD. */
    @Column(name = "total_wealth_usd")
    private double totalWealthUsd;

    /** Total Cash (Comptes bancaires) en USD. */
    @Column(name = "total_cash")
    private double totalCash;

    /** Total Crypto en USD. */
    @Column(name = "total_crypto")
    private double totalCrypto;

    /** Total Stocks en USD. */
    @Column(name = "total_stocks")
    private double totalStocks;

    /** Taux de croissance depuis la première mesure. */
    @Column(name = "growth_rate")
    private double growthRate;

    /** Historique des valeurs enregistrées (en USD). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "wealth_history", joinColumns = @JoinColumn(name = "tracker_id"))
    @Column(name = "value")
    private List<Double> historicalValues;

    // ============================================================
    // CONSTRUCTEURS
    // ============================================================

    public WealthTracker() {
        // Constructeur JPA requis
        this.historicalValues = new ArrayList<>();
    }

    /**
     * Constructeur normal : suivi direct d’un utilisateur.
     */
    public WealthTracker(User user) {
        this();
        this.user = user;
        this.totalWealthUsd = 0.0;
        this.totalCash = 0.0;
        this.totalCrypto = 0.0;
        this.totalStocks = 0.0;
        this.growthRate = 0.0;
    }

    // ============================================================
    // CALCUL PRINCIPAL DE LA RICHESSE
    // ============================================================

    /**
     * Met à jour la richesse totale :
     * - Total des comptes (CHF converti en USD) -> Cash
     * - Total de tous les portefeuilles (CHF converti en USD) -> Crypto/Stocks
     * - Met à jour : croissance, historique
     */
    public void updateWealth() {

        // =====================
        // 1. Comptes en CHF -> CASH
        // =====================
        double accountsChf = user.getTotalBalance();
        this.totalCash = CurrencyConverter.chfToUsd(accountsChf);

        // ================================================
        // 2. Sommation de TOUS les portefeuilles du user
        // ================================================
        this.totalCrypto = 0.0;
        this.totalStocks = 0.0;

        if (user.getPortfolios() != null) {
            for (Portfolio p : user.getPortfolios()) {
                if (p.getAssets() != null) {
                    for (Asset asset : p.getAssets()) {
                        double assetChf = asset.getTotalValue();
                        double assetUsd = CurrencyConverter.chfToUsd(assetChf);

                        String type = (asset.getType() != null) ? asset.getType().toUpperCase() : "UNKNOWN";

                        if ("CRYPTO".equals(type)) {
                            this.totalCrypto += assetUsd;
                        } else if ("STOCK".equals(type) || "SHARE".equals(type)) {
                            this.totalStocks += assetUsd;
                        } else {
                            // Par défaut, on considère le reste comme Stocks/Investissements
                            this.totalStocks += assetUsd;
                        }
                    }
                }
            }
        }

        // ====================
        // 3. Total Wealth
        // ====================
        this.totalWealthUsd = this.totalCash + this.totalCrypto + this.totalStocks;

        // ================================
        // 4. Mise à jour de l’historique
        // ================================
        historicalValues.add(totalWealthUsd);

        // ================================
        // 5. Calcul du taux de croissance
        // ================================
        if (historicalValues.size() > 1) {
            double initial = historicalValues.get(0);
            if (initial != 0) {
                growthRate = ((totalWealthUsd - initial) / initial) * 100.0;
            }
        }
    }

    // ============================================================
    // GETTERS CALCULÉS
    // ============================================================

    public double getTotalWealthUsd() {
        return totalWealthUsd;
    }

    public double getTotalCash() {
        return totalCash;
    }

    public double getTotalCrypto() {
        return totalCrypto;
    }

    public double getTotalStocks() {
        return totalStocks;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // ============================================================
    // TO STRING
    // ============================================================

    @Override
    public String toString() {
        return String.format(
                "WealthTracker{user=%s, total=%.2f, cash=%.2f, crypto=%.2f, stocks=%.2f}",
                user.getFirstName() + " " + user.getLastName(),
                totalWealthUsd,
                totalCash,
                totalCrypto,
                totalStocks);
    }
}
