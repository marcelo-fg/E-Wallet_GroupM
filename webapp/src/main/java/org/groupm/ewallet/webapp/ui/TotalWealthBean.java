package org.groupm.ewallet.webapp.ui;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
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
@SessionScoped
public class TotalWealthBean implements Serializable {

    private double totalCash;
    private double totalCrypto;
    private double totalStocks;
    private double totalEtf;
    private double totalNetWorth;

    private String cashHistoryJson;
    private String netWorthHistoryJson;
    private String portfolioHistoryJson;

    // Period selection for Total Wealth page
    private int selectedPeriod = 30; // Default 30 days

    // Dashboard always shows 7 days
    private String netWorthHistoryJson7Days;

    @Inject
    private WebAppService webAppService;

    @PostConstruct
    public void init() {
        loadData();
    }

    /**
     * Loads or reloads all wealth data for the current period.
     */
    public void loadData() {
        String userId = fetchRealData();

        if (userId == null) {
            this.cashHistoryJson = convertMapToJson(new TreeMap<>());
            this.netWorthHistoryJson = convertMapToJson(new TreeMap<>());
            this.portfolioHistoryJson = convertMapToJson(new TreeMap<>());
            this.netWorthHistoryJson7Days = convertMapToJson(new TreeMap<>());
            return;
        }

        // Generate chart data for selected period (Total Wealth page)
        generateChartData(userId, selectedPeriod);

        // Always generate 7-day data for dashboard
        Map<LocalDate, Double> cashMap7 = webAppService.getCashHistory(userId, 7);
        Map<LocalDate, Double> portfolioMap7 = webAppService.getPortfolioHistory(userId, 7);
        Map<LocalDate, Double> netWorthMap7 = new TreeMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate date = today.minusDays(i);
            double c = cashMap7.getOrDefault(date, 0.0);
            double p = portfolioMap7.getOrDefault(date, 0.0);
            netWorthMap7.put(date, c + p);
        }
        this.netWorthHistoryJson7Days = convertMapToJson(netWorthMap7);
    }

    /**
     * Generates chart data for a specific number of days.
     */
    private void generateChartData(String userId, int days) {
        Map<LocalDate, Double> cashMap = webAppService.getCashHistory(userId, days);
        Map<LocalDate, Double> portfolioMap = webAppService.getPortfolioHistory(userId, days);

        Map<LocalDate, Double> netWorthMap = new TreeMap<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < days; i++) {
            LocalDate date = today.minusDays(i);
            double c = cashMap.getOrDefault(date, 0.0);
            double p = portfolioMap.getOrDefault(date, 0.0);
            netWorthMap.put(date, c + p);
        }

        this.cashHistoryJson = convertMapToJson(cashMap);
        this.netWorthHistoryJson = convertMapToJson(netWorthMap);
        this.portfolioHistoryJson = convertMapToJson(portfolioMap);
    }

    /**
     * Changes the period and reloads data.
     */
    public void changePeriod(int days) {
        this.selectedPeriod = days;
        loadData();
    }

    public int getSelectedPeriod() {
        return selectedPeriod;
    }

    public void setSelectedPeriod(int selectedPeriod) {
        this.selectedPeriod = selectedPeriod;
    }

    /**
     * Returns the 7-day net worth history JSON for the dashboard.
     */
    public String getNetWorthHistoryJson7Days() {
        return netWorthHistoryJson7Days;
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
