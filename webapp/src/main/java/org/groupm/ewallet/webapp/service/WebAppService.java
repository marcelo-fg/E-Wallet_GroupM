package org.groupm.ewallet.webapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import org.groupm.ewallet.webapp.connector.ExternalAsset;
import org.groupm.ewallet.webapp.model.LocalAccount;
import org.groupm.ewallet.webapp.model.LocalTransaction;
import org.groupm.ewallet.webapp.model.PortfolioTrade;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;

/**
 * Central application service facade.
 * Delegates to specialized services:
 * - BackendApiService: REST API calls
 * - MockDataService: In-memory mock data
 * - MarketDataService: External market APIs
 * 
 * This facade maintains backward compatibility with UI beans.
 */
@ApplicationScoped
public class WebAppService {

    @Inject
    private BackendApiService backendApi;

    @Inject
    private MockDataService mockData;

    @Inject
    private MarketDataService marketData;

    // ============================================================
    // AUTHENTICATION & USER MANAGEMENT
    // ============================================================

    public String login(String email, String password) {
        return backendApi.login(email, password);
    }

    public boolean registerUser(String firstname, String lastname, String email, String password) {
        return backendApi.registerUser(firstname, lastname, email, password);
    }

    public JsonObject getUserDetails(String userId) {
        return backendApi.getUserDetails(userId);
    }

    public boolean updateUser(String userId, String firstname, String lastname, String email, String password) {
        return backendApi.updateUser(userId, firstname, lastname, email, password);
    }

    public boolean deleteUser(String userId) {
        return backendApi.deleteUser(userId);
    }

    public JsonObject getWealthForUser(String userId) {
        return backendApi.getWealthForUser(userId);
    }

    @Inject
    private WealthCalculatorService wealthCalculator;

    // ...

    /**
     * Calculates the historical evolution of the user's total portfolio value.
     */
    public Map<LocalDate, Double> getPortfolioHistory(String userId, int days) {
        // Delegate to dedicated service
        return wealthCalculator.calculatePortfolioHistory(userId, days, getWealthForUser(userId));
    }

    /**
     * Calculates the historical evolution of the user's total cash balance.
     */
    public Map<LocalDate, Double> getCashHistory(String userId, int days) {
        // Delegate to dedicated service
        return wealthCalculator.calculateCashHistory(userId, days);
    }

    public boolean makeTransfer(String fromAccount, String toAccount, double amount) {
        return backendApi.makeTransfer(fromAccount, toAccount, amount);
    }

    // ============================================================
    // PORTFOLIO OPERATIONS
    // ============================================================

    public List<Integer> getPortfoliosForUser(String userId) {
        return backendApi.getPortfoliosForUser(userId);
    }

    public Integer createPortfolioForUser(String userId, String name) {
        return backendApi.createPortfolioForUser(userId, name);
    }

    public org.groupm.ewallet.webapp.model.Portfolio getPortfolioById(int portfolioId) {
        return backendApi.getPortfolioById(portfolioId);
    }

    public List<String> getAssetsForPortfolio(int portfolioId) {
        return backendApi.getAssetsForPortfolio(portfolioId);
    }

    public boolean addAssetToPortfolio(int portfolioId, String name, String type,
            double qty, double unitPrice, String symbol) {
        return backendApi.addAssetToPortfolio(portfolioId, name, type, qty, unitPrice, symbol);
    }

    public List<org.groupm.ewallet.webapp.model.PortfolioAsset> getAllUserAssets(String userId) {
        List<org.groupm.ewallet.webapp.model.PortfolioAsset> allAssets = new java.util.ArrayList<>();
        List<Integer> portfolios = getPortfoliosForUser(userId);
        for (Integer pid : portfolios) {
            allAssets.addAll(getPortfolioAssets(pid));
        }
        return allAssets;
    }

    // ============================================================
    // EXTERNAL MARKET DATA
    // ============================================================

    public List<ExternalAsset> loadAssetsFromApi(String type) {
        return marketData.loadAssetsFromApi(type);
    }

    public double getPriceForAsset(String idOrSymbol, String type) {
        return marketData.getPriceForAsset(idOrSymbol, type);
    }

    public List<Double> getHistoricalPrices(String idOrSymbol, String type, int days) {
        return marketData.getHistoricalPrices(idOrSymbol, type, days);
    }

    // ============================================================
    // ACCOUNTS (BACKEND CONNECTED)
    // ============================================================

    public List<LocalAccount> getAccounts(String userId) {
        if (userId == null)
            return List.of();
        return backendApi.getAccountsForUser(userId);
    }

    // Depecrated: Mock only
    public List<LocalAccount> getAccounts() {
        return mockData.getAccounts();
    }

    public LocalAccount createAccount(String userId, String type, String name) {
        boolean success = backendApi.createAccount(userId, type, name);
        if (success) {
            // Re-fetch to get the full object including generated ID
            List<LocalAccount> accounts = backendApi.getAccountsForUser(userId);
            return accounts.stream().filter(a -> a.getName().equals(name)).findFirst().orElse(null);
        }
        return null;
    }

    public LocalAccount getAccountById(String id) {
        return backendApi.getAccount(id);
    }

    public boolean deleteAccount(String id) {
        return backendApi.deleteAccount(id);
    }

    public boolean deletePortfolio(int portfolioId) {
        return backendApi.deletePortfolio(portfolioId);
    }

    public boolean deleteTransaction(String transactionId) {
        return backendApi.deleteTransaction(transactionId);
    }

    public boolean depositToAccount(String id, double amount) {
        return backendApi.createTransaction(id, amount, "deposit", "Dépôt via WebApp");
    }

    public boolean withdrawFromAccount(String id, double amount, String category, String description) {
        return backendApi.createTransaction(id, amount, "withdraw",
                description != null ? description : "Retrait via WebApp");
    }

    public boolean transferBetweenAccounts(String fromAccountId, String toAccountId,
            double amount, String category, String description) {
        return backendApi.makeTransfer(fromAccountId, toAccountId, amount);
    }

    public List<LocalTransaction> getTransactionsForAccount(String accountId) {
        return backendApi.getTransactionsForAccount(accountId);
    }

    public List<LocalTransaction> getTransactionsForPortfolio(int portfolioId) {
        return mockData.getTransactionsForPortfolio(portfolioId);
    }

    // ============================================================
    // PORTFOLIO TRADES (PERSISTED TO BACKEND + IN-MEMORY CACHE)
    // ============================================================

    public void recordPortfolioTrade(int portfolioId, String assetName, String symbol,
            String type, double quantity, double unitPrice) {
        // Store in memory for immediate display (cache)
        mockData.recordPortfolioTrade(portfolioId, assetName, symbol, type, quantity, unitPrice);

        // ALSO persist to backend database
        backendApi.recordPortfolioTransaction(portfolioId, assetName, symbol, type, quantity, unitPrice);
    }

    public List<PortfolioTrade> getTradesForPortfolio(int portfolioId) {
        // Try to get from backend first
        List<PortfolioTrade> backendTrades = backendApi.getPortfolioTransactions(portfolioId);

        if (backendTrades != null && !backendTrades.isEmpty()) {
            return backendTrades;
        }

        // Fallback to in-memory mock data
        return mockData.getTradesForPortfolio(portfolioId);
    }

    // ============================================================
    // PORTFOLIO ASSETS (IN-MEMORY)
    // ============================================================

    public void addPortfolioAsset(int portfolioId, String assetName, String symbol,
            String type, double quantity, double unitPrice) {
        mockData.addPortfolioAsset(portfolioId, assetName, symbol, type, quantity, unitPrice);
    }

    public List<org.groupm.ewallet.webapp.model.PortfolioAsset> getPortfolioAssets(int portfolioId) {
        // Switch from mock data to real backend data
        return backendApi.getPortfolioAssetsFromBackend(portfolioId);
    }

    // ============================================================
    // UNIFIED TRANSACTIONS
    // ============================================================

    public List<MockDataService.UnifiedTransaction> getAllUnifiedTransactions(String userId) {
        List<MockDataService.UnifiedTransaction> list = new java.util.ArrayList<>();

        // 1. Get real bank transactions from backend
        if (userId != null) {
            List<LocalTransaction> realTx = backendApi.getTransactionsForUser(userId);
            for (LocalTransaction tx : realTx) {
                LocalAccount acc = getAccountById(tx.getAccountId());
                String label = (acc != null && acc.getName() != null && !acc.getName().isBlank())
                        ? acc.getName()
                        : (acc != null ? acc.getType() + " " + acc.getId() : tx.getAccountId());

                list.add(new MockDataService.UnifiedTransaction(
                        "ACCOUNT",
                        tx.getAccountId(),
                        label,
                        tx.getDateTime(),
                        tx.getAmount(),
                        tx.getType(),
                        tx.getCategory(),
                        tx.getDescription()));
            }

            // 2. Get real portfolio transactions from backend
            List<Integer> portfolioIds = getPortfoliosForUser(userId);
            for (Integer portfolioId : portfolioIds) {
                List<org.groupm.ewallet.webapp.model.PortfolioTrade> trades = backendApi
                        .getPortfolioTransactions(portfolioId);

                for (org.groupm.ewallet.webapp.model.PortfolioTrade trade : trades) {
                    // Format: "AssetName (SYMBOL)" as source label
                    String assetLabel = trade.getAssetName() + " (" + trade.getSymbol() + ")";

                    // Description includes portfolio ID, type, quantity, and price
                    String description = String.format("Portfolio %d %s %.2f @ %.2f",
                            trade.getPortfolioId(),
                            trade.getType(),
                            trade.getQuantity(),
                            trade.getUnitPrice());

                    // Use getSignedNotional() which already handles BUY (+) / SELL (-)
                    double signedAmount = trade.getSignedNotional();

                    list.add(new MockDataService.UnifiedTransaction(
                            "PORTFOLIO",
                            String.valueOf(trade.getPortfolioId()),
                            assetLabel,
                            trade.getDateTime(),
                            signedAmount,
                            trade.getType(),
                            "PORTFOLIO_TRADE",
                            description));
                }
            }
        }

        // Sort by date descending
        list.sort((a, b) -> b.getDateTime().compareTo(a.getDateTime()));

        return list;
    }

    // ============================================================
    // MOCK VALUES (TEMPORARY)
    // ============================================================

    /**
     * Mocked total wealth value, until a real aggregated calculation is available.
     */
    public double getTotalWealthForUser(String userId) {
        return 50000.0;
    }

    /**
     * Mocked wealth growth percentage, until a real calculation is available.
     */
    public double getWealthGrowthForUser(String userId) {
        return 12.5;
    }

    /**
     * Simple DTO to expose portfolio id, display name and total value to the UI.
     */
    public static class PortfolioInfo {
        private final int id;
        private final String name;
        private final double value;

        public PortfolioInfo(int id, String name, double value) {
            this.id = id;
            this.name = name;
            this.value = value;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getValue() {
            return value;
        }
    }
}
