package org.groupm.ewallet.webapp.ui;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import org.groupm.ewallet.webapp.service.WebAppService;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import org.groupm.ewallet.webapp.model.LocalAccount;

@Named
@RequestScoped
public class TotalWealthBean implements Serializable {

    private double totalCash;
    private double totalCrypto;
    private double totalStocks;
    private double totalEtf;
    private double totalNetWorth;

    private String cashHistoryJson;
    private String netWorthHistoryJson; // Replaces cryptoHistory for logic, but we'll add getter
    private String portfolioHistoryJson; // Replaces stocksHistory for logic

    @Inject
    private WebAppService webAppService;

    @PostConstruct
    public void init() {
        // 1. Fetch Real Data (Current Values)
        String userId = fetchRealData();

        if (userId == null) {
            // Fallback for unauthenticated or error state
            this.cashHistoryJson = convertMapToJson(new TreeMap<>()); // Empty map for no data
            this.netWorthHistoryJson = convertMapToJson(new TreeMap<>());
            this.portfolioHistoryJson = convertMapToJson(new TreeMap<>());
            return;
        }

        // 2. Get Histories (30 Days)
        int days = 30;

        // A. Real Cash History (from DB transactions)
        Map<LocalDate, Double> cashMap = webAppService.getCashHistory(userId, days);

        // B. Real Portfolio History (from DB Transactions)
        Map<LocalDate, Double> portfolioMap = webAppService.getPortfolioHistory(userId, days);

        // Simulating crypto/stocks breakdown is no longer accurate if we use aggregated
        // history.
        // We will just feed the aggregated map to the charts or stick to one main
        // chart.
        // The user wants: Net Worth, Cash, Portfolio.
        // We have Cash Map. We have Portfolio Map.
        // Net Worth Map = Cash + Portfolio.

        // C. Calculate Total Net Worth History (Cash + Portfolio)
        Map<LocalDate, Double> netWorthMap = new TreeMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            LocalDate date = today.minusDays(i);
            double c = cashMap.getOrDefault(date, 0.0);
            double p = portfolioMap.getOrDefault(date, 0.0);
            netWorthMap.put(date, c + p);
        }

        // 3. Convert to JSON for Charts
        this.cashHistoryJson = convertMapToJson(cashMap);
        this.netWorthHistoryJson = convertMapToJson(netWorthMap);
        this.portfolioHistoryJson = convertMapToJson(portfolioMap);
    }

    // Helper: Fetch real data and return userId
    private String fetchRealData() {
        var context = jakarta.faces.context.FacesContext.getCurrentInstance();
        if (context == null)
            return null;

        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        if (session == null)
            return null;

        String userId = (String) session.getAttribute("userId");
        if (userId == null)
            return null;

        // 1. Calculate Total Cash from Raw Accounts
        List<LocalAccount> accounts = webAppService.getAccounts(userId);
        this.totalCash = accounts.stream().mapToDouble(LocalAccount::getBalance).sum();

        // 2. Calculate Portfolio Assets (Granular Breakdown)
        List<org.groupm.ewallet.webapp.model.PortfolioAsset> assets = webAppService.getAllUserAssets(userId);

        this.totalStocks = 0;
        this.totalCrypto = 0;
        this.totalEtf = 0;

        for (org.groupm.ewallet.webapp.model.PortfolioAsset asset : assets) {
            String type = asset.getType() != null ? asset.getType().toLowerCase() : "";
            double val = asset.getTotalValue();

            if (type.equals("crypto")) {
                this.totalCrypto += val;
            } else if (type.equals("etf")) {
                this.totalEtf += val;
            } else {
                // Default to stock for "stock" or unknown
                this.totalStocks += val;
            }
        }

        // 3. Recalculate Net Worth
        this.totalNetWorth = this.totalCash + this.totalCrypto + this.totalStocks + this.totalEtf;

        return userId;
    }

    // Helper for US formatting
    private String formatUs(double val) {
        return String.format(java.util.Locale.US, "%.2f", val);
    }

    private String convertMapToJson(Map<LocalDate, Double> map) {
        StringBuilder labels = new StringBuilder("[");
        StringBuilder data = new StringBuilder("[");

        // Sort keys (dates) ascending
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");
        boolean first = true;
        for (Map.Entry<LocalDate, Double> entry : map.entrySet()) {
            if (!first) {
                labels.append(",");
                data.append(",");
            }
            labels.append("\"").append(entry.getKey().format(formatter)).append("\"");
            data.append(formatUs(entry.getValue())); // Use formatUs, no replace needed
            first = false;
        }
        labels.append("]");
        data.append("]");
        return String.format("{\"labels\": %s, \"data\": %s}", labels.toString(), data.toString());
    }

    // Getters
    public double getTotalCash() {
        return totalCash;
    }

    public double getTotalCrypto() {
        return totalCrypto;
    }

    public double getTotalStocks() {
        return totalStocks;
    }

    public double getTotalEtf() {
        return totalEtf;
    }

    public double getTotalNetWorth() {
        return totalNetWorth;
    }

    public double getTotalPortfolioBalance() {
        return totalCrypto + totalStocks + totalEtf;
    }

    /**
     * Returns a JSON array of [Cash, Stocks, Crypto, ETFs] for the Donut Chart.
     */
    public String getWealthDistributionJson() {
        // Safe formatting using Locale.US, maintaining commas as separators
        return String.format(java.util.Locale.US, "[%.2f, %.2f, %.2f, %.2f]",
                totalCash,
                totalStocks,
                totalCrypto,
                totalEtf);
    }

    public String getCashHistoryJson() {
        return cashHistoryJson;
    }

    // Maintaining interface with UI but changing content semantics
    public String getCryptoHistoryJson() {
        return netWorthHistoryJson;
    } // Mapped to 2nd chart

    public String getStocksHistoryJson() {
        return portfolioHistoryJson;
    } // Mapped to 3rd chart

    // Proper getters if UI updates
    public String getNetWorthHistoryJson() {
        return netWorthHistoryJson;
    }

    public String getPortfolioHistoryJson() {
        return portfolioHistoryJson;
    }
}
