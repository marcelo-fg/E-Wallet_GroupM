package org.groupm.ewallet.webapp.ui;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import jakarta.json.JsonObject;
import org.groupm.ewallet.webapp.service.WebAppService;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Named
@RequestScoped
public class TotalWealthBean implements Serializable {

    private double totalCash;
    private double totalCrypto;
    private double totalStocks;
    private double totalNetWorth;

    private String cashHistoryJson;
    private String cryptoHistoryJson;
    private String stocksHistoryJson;

    @Inject
    private WebAppService webAppService;

    @PostConstruct
    public void init() {
        // 1. Fetch Real Data from Backend (Refactored)
        fetchRealData();

        // 2. Generate History ending at Real Data
        this.cashHistoryJson = generateConsistentHistory(totalCash, 200);
        this.cryptoHistoryJson = generateConsistentHistory(totalCrypto, 2000);
        this.stocksHistoryJson = generateConsistentHistory(totalStocks, 1000);
    }

    private void fetchRealData() {
        var context = jakarta.faces.context.FacesContext.getCurrentInstance();
        if (context == null) return;
        
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        if (session == null) return;

        String userId = (String) session.getAttribute("userId");
        if (userId == null) return;

        // Call the new service method that returns the WealthTracker model
        JsonObject wealthJson = webAppService.getWealthForUser(userId);
        
        if (wealthJson != null) {
            // Direct mapping from backend model
            this.totalCash = wealthJson.getJsonNumber("totalCash").doubleValue();
            this.totalCrypto = wealthJson.getJsonNumber("totalCrypto").doubleValue();
            this.totalStocks = wealthJson.getJsonNumber("totalStocks").doubleValue();
            this.totalNetWorth = wealthJson.getJsonNumber("totalWealthUsd").doubleValue();
        }
    }

    /**
     * Generates a random walk history that ENDS exactly at currentRealValue.
     */
    private String generateConsistentHistory(double currentRealValue, double volatility) {
        // We generate backwards from today
        double[] values = new double[30];
        values[29] = currentRealValue; // Today is the last point

        Random random = new Random();
        
        // Walk backwards
        for (int i = 28; i >= 0; i--) {
            double change = (random.nextDouble() - 0.5) * volatility;
            values[i] = values[i+1] - change; // Reverse the change
            
            // Prevent negative values
            if (values[i] < 0) values[i] = 0;
        }

        // Build JSON
        StringBuilder labels = new StringBuilder("[");
        StringBuilder data = new StringBuilder("[");
        
        LocalDate date = LocalDate.now().minusDays(29);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM");

        for (int i = 0; i < 30; i++) {
            labels.append("\"").append(date.format(formatter)).append("\"");
            data.append(String.format("%.2f", values[i]).replace(",", "."));

            if (i < 29) {
                labels.append(",");
                data.append(",");
            }
            date = date.plusDays(1);
        }

        labels.append("]");
        data.append("]");

        return String.format("{\"labels\": %s, \"data\": %s}", labels.toString(), data.toString());
    }

    // Getters
    public double getTotalCash() { return totalCash; }
    public double getTotalCrypto() { return totalCrypto; }
    public double getTotalStocks() { return totalStocks; }
    public double getTotalNetWorth() { return totalNetWorth; }
    
    public String getCashHistoryJson() { return cashHistoryJson; }
    public String getCryptoHistoryJson() { return cryptoHistoryJson; }
    public String getStocksHistoryJson() { return stocksHistoryJson; }
}
