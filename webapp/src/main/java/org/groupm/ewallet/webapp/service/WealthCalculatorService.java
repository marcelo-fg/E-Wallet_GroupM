package org.groupm.ewallet.webapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import org.groupm.ewallet.webapp.model.LocalAccount;
import org.groupm.ewallet.webapp.model.LocalTransaction;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Service dedicated to calculating wealth history and evolution.
 * Extracts heavy calculation logic from the main WebAppService facade.
 */
@ApplicationScoped
public class WealthCalculatorService {

    @Inject
    private BackendApiService backendApi;

    // We can inject WebAppService if circular dep is avoided, or just use
    // BackendApi.
    // Ideally use BackendApi to be pure.
    // but getCashHistory used getAccounts() from WebAppService.
    // Let's use BackendApi directly or inject WebAppService carefully.
    // Better: Pure logic that accepts lists? Or Service that fetches?
    // Let's make it a Service that fetches its own data via BackendApi.

    /**
     * Calculates the historical evolution of the user's total portfolio value.
     */
    public Map<LocalDate, Double> calculatePortfolioHistory(String userId, int days, JsonObject wealthAnchor) {
        // 1. Anchor
        double currentTotalValue = 0.0;
        if (wealthAnchor != null) {
            double c = wealthAnchor.getJsonNumber("totalCrypto").doubleValue();
            double s = wealthAnchor.getJsonNumber("totalStocks").doubleValue();
            currentTotalValue = c + s;
        }

        // 2. Transactions
        List<LocalTransaction> transactions = backendApi.getTransactionsForUser(userId);
        List<LocalTransaction> portfolioTx = transactions.stream()
                .filter(t -> "Portfolio".equalsIgnoreCase(t.getType()) || "PORTFOLIO_TRADE".equals(t.getCategory()))
                .sorted(Comparator.comparing(LocalTransaction::getDateTime).reversed())
                .collect(Collectors.toList());

        // 3. Reconstruct
        Map<LocalDate, Double> history = new TreeMap<>();
        LocalDate today = LocalDate.now();
        LocalDate cutoffDate = today.minusDays(days);

        double runningValue = currentTotalValue;
        history.put(today, runningValue);

        Map<LocalDate, List<LocalTransaction>> txByDate = portfolioTx.stream()
                .collect(Collectors.groupingBy(t -> t.getDateTime().toLocalDate()));

        for (LocalDate date = today; !date.isBefore(cutoffDate); date = date.minusDays(1)) {
            history.put(date, runningValue);

            if (txByDate.containsKey(date)) {
                for (LocalTransaction tx : txByDate.get(date)) {
                    String desc = tx.getDescription().toUpperCase();
                    double amount = tx.getAmount();

                    if (desc.contains("BUY") || desc.contains("ACHAT")) {
                        runningValue -= amount;
                    } else if (desc.contains("SELL") || desc.contains("VENTE")) {
                        runningValue += amount;
                    }
                }
            }
            if (runningValue < 0)
                runningValue = 0;
        }
        return history;
    }

    /**
     * Calculates the historical evolution of the user's total cash balance.
     */
    public Map<LocalDate, Double> calculateCashHistory(String userId, int days) {
        // 1. Anchor
        List<LocalAccount> accounts = backendApi.getAccountsForUser(userId);
        double currentTotalCash = accounts.stream().mapToDouble(LocalAccount::getBalance).sum();

        // 2. Transactions - Create mutable copy before sorting
        List<LocalTransaction> transactions = new java.util.ArrayList<>(backendApi.getTransactionsForUser(userId));
        transactions.sort(Comparator.comparing(LocalTransaction::getDateTime).reversed());

        // 3. Reconstruct
        Map<LocalDate, Double> history = new TreeMap<>();
        LocalDate today = LocalDate.now();
        LocalDate cutoffDate = today.minusDays(days);

        double runningBalance = currentTotalCash;
        history.put(today, runningBalance);

        Map<LocalDate, List<LocalTransaction>> txByDate = transactions.stream()
                .collect(Collectors.groupingBy(t -> t.getDateTime().toLocalDate()));

        for (LocalDate date = today; !date.isBefore(cutoffDate); date = date.minusDays(1)) {
            history.put(date, runningBalance);

            if (txByDate.containsKey(date)) {
                for (LocalTransaction tx : txByDate.get(date)) {
                    double amount = tx.getAmount();
                    String type = tx.getType() != null ? tx.getType().toLowerCase() : "";

                    if (type.contains("deposit") || type.contains("receive") || type.contains("credit")) {
                        runningBalance -= amount;
                    } else if (type.contains("withdraw") || type.contains("send") || type.contains("debit")
                            || type.contains("payment")) {
                        runningBalance += amount;
                    }
                }
            }
            if (runningBalance < 0)
                runningBalance = 0;
        }

        return history;
    }
}
