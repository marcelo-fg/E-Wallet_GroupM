package org.groupm.ewallet.model;

import org.groupm.ewallet.service.CurrencyConverter;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Suit la richesse totale d'un utilisateur (comptes + portefeuilles).
 * Calcule la valeur en USD, en CHF et le taux de croissance.
 * Utilise BigDecimal pour précision financière.
 */
@Entity
@Table(name = "wealth_trackers")
public class WealthTracker implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** Utilisateur suivi. */
    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    /** Richesse totale en USD. */
    @Column(name = "total_wealth_usd", precision = 19, scale = 4)
    private BigDecimal totalWealthUsd = BigDecimal.ZERO;

    /** Total Cash (Comptes bancaires) en USD. */
    @Column(name = "total_cash", precision = 19, scale = 4)
    private BigDecimal totalCash = BigDecimal.ZERO;

    /** Total Crypto en USD. */
    @Column(name = "total_crypto", precision = 19, scale = 4)
    private BigDecimal totalCrypto = BigDecimal.ZERO;

    /** Total Stocks en USD. */
    @Column(name = "total_stocks", precision = 19, scale = 4)
    private BigDecimal totalStocks = BigDecimal.ZERO;

    /** Taux de croissance depuis la première mesure. */
    @Column(name = "growth_rate", precision = 10, scale = 4)
    private BigDecimal growthRate = BigDecimal.ZERO;

    /** Historique des valeurs enregistrées (en USD). */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "wealth_history", joinColumns = @JoinColumn(name = "tracker_id"))
    @Column(name = "value")
    private List<Double> historicalValues;

    /** Version pour optimistic locking - détection des conflits concurrents. */
    @Version
    private Long version;

    // ============================================================
    // CONSTRUCTEURS
    // ============================================================

    public WealthTracker() {
        this.historicalValues = new ArrayList<>();
        this.totalWealthUsd = BigDecimal.ZERO;
        this.totalCash = BigDecimal.ZERO;
        this.totalCrypto = BigDecimal.ZERO;
        this.totalStocks = BigDecimal.ZERO;
        this.growthRate = BigDecimal.ZERO;
    }

    /**
     * Constructeur normal : suivi direct d'un utilisateur.
     */
    public WealthTracker(User user) {
        this();
        this.user = user;
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
        this.totalCash = BigDecimal.valueOf(CurrencyConverter.chfToUsd(accountsChf));

        // ================================================
        // 2. Sommation de TOUS les portefeuilles du user
        // ================================================
        BigDecimal cryptoTotal = BigDecimal.ZERO;
        BigDecimal stocksTotal = BigDecimal.ZERO;

        if (user.getPortfolios() != null) {
            for (Portfolio p : user.getPortfolios()) {
                if (p.getAssets() != null) {
                    for (Asset asset : p.getAssets()) {
                        double assetChf = asset.getTotalValue();
                        BigDecimal assetUsd = BigDecimal.valueOf(CurrencyConverter.chfToUsd(assetChf));

                        String type = (asset.getType() != null) ? asset.getType().toUpperCase() : "UNKNOWN";

                        if ("CRYPTO".equals(type)) {
                            cryptoTotal = cryptoTotal.add(assetUsd);
                        } else if ("STOCK".equals(type) || "SHARE".equals(type)) {
                            stocksTotal = stocksTotal.add(assetUsd);
                        } else {
                            // Par défaut, on considère le reste comme Stocks/Investissements
                            stocksTotal = stocksTotal.add(assetUsd);
                        }
                    }
                }
            }
        }

        this.totalCrypto = cryptoTotal;
        this.totalStocks = stocksTotal;

        // ====================
        // 3. Total Wealth
        // ====================
        this.totalWealthUsd = this.totalCash.add(this.totalCrypto).add(this.totalStocks);

        // ================================
        // 4. Mise à jour de l'historique
        // ================================
        historicalValues.add(totalWealthUsd.doubleValue());

        // ================================
        // 5. Calcul du taux de croissance
        // ================================
        if (historicalValues.size() > 1) {
            double initial = historicalValues.get(0);
            if (initial != 0) {
                double growth = ((totalWealthUsd.doubleValue() - initial) / initial) * 100.0;
                this.growthRate = BigDecimal.valueOf(growth);
            }
        }
    }

    // ============================================================
    // GETTERS
    // ============================================================

    public BigDecimal getTotalWealthUsdAsBigDecimal() {
        return totalWealthUsd != null ? totalWealthUsd : BigDecimal.ZERO;
    }

    @Deprecated
    public double getTotalWealthUsd() {
        return totalWealthUsd != null ? totalWealthUsd.doubleValue() : 0.0;
    }

    public BigDecimal getTotalCashAsBigDecimal() {
        return totalCash != null ? totalCash : BigDecimal.ZERO;
    }

    @Deprecated
    public double getTotalCash() {
        return totalCash != null ? totalCash.doubleValue() : 0.0;
    }

    public BigDecimal getTotalCryptoAsBigDecimal() {
        return totalCrypto != null ? totalCrypto : BigDecimal.ZERO;
    }

    @Deprecated
    public double getTotalCrypto() {
        return totalCrypto != null ? totalCrypto.doubleValue() : 0.0;
    }

    public BigDecimal getTotalStocksAsBigDecimal() {
        return totalStocks != null ? totalStocks : BigDecimal.ZERO;
    }

    @Deprecated
    public double getTotalStocks() {
        return totalStocks != null ? totalStocks.doubleValue() : 0.0;
    }

    public double getTotalWealthChf() {
        return CurrencyConverter.usdToChf(getTotalWealthUsd());
    }

    public BigDecimal getGrowthRateAsBigDecimal() {
        return growthRate != null ? growthRate : BigDecimal.ZERO;
    }

    @Deprecated
    public double getGrowthRate() {
        return growthRate != null ? growthRate.doubleValue() : 0.0;
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
                "WealthTracker{user=%s, total=%s, cash=%s, crypto=%s, stocks=%s}",
                user.getFirstName() + " " + user.getLastName(),
                totalWealthUsd,
                totalCash,
                totalCrypto,
                totalStocks);
    }
}
