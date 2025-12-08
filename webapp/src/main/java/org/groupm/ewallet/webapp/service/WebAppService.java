package org.groupm.ewallet.webapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import org.groupm.ewallet.webapp.connector.ExternalAsset;
import org.groupm.ewallet.webapp.model.LocalAccount;
import org.groupm.ewallet.webapp.model.LocalTransaction;
import org.groupm.ewallet.webapp.model.PortfolioTrade;

import java.util.List;

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

    @Inject
    private PriceCacheService priceCache;

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

    public JsonObject getWealthForUser(String userId) {
        return backendApi.getWealthForUser(userId);
    }

    // ============================================================
    // TRANSACTIONS & TRANSFERS
    // ============================================================

    public boolean makeTransfer(String fromAccount, String toAccount, double amount) {
        return backendApi.makeTransfer(fromAccount, toAccount, amount);
    }

    // ============================================================
    // PORTFOLIO OPERATIONS
    // ============================================================

    public List<Integer> getPortfoliosForUser(String userId) {
        return backendApi.getPortfoliosForUser(userId);
    }

    public Integer createPortfolioForUser(String userId) {
        return backendApi.createPortfolioForUser(userId);
    }

    public List<String> getAssetsForPortfolio(int portfolioId) {
        return backendApi.getAssetsForPortfolio(portfolioId);
    }

    public boolean addAssetToPortfolio(int portfolioId, String name, String type,
            double qty, double unitPrice, String symbol) {
        return backendApi.addAssetToPortfolio(portfolioId, name, type, qty, unitPrice, symbol);
    }

    // ============================================================
    // EXTERNAL MARKET DATA
    // ============================================================

    public List<ExternalAsset> loadAssetsFromApi(String type) {
        return marketData.loadAssetsFromApi(type);
    }

    public double getPriceForAsset(String idOrSymbol, String type) {
        // Use cached price first to avoid hitting API rate limits
        return priceCache.getCachedPrice(idOrSymbol, type);
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
    // PORTFOLIO TRADES (IN-MEMORY)
    // ============================================================

    public void recordPortfolioTrade(int portfolioId, String assetName, String symbol,
            String type, double quantity, double unitPrice) {
        mockData.recordPortfolioTrade(portfolioId, assetName, symbol, type, quantity, unitPrice);
    }

    public List<PortfolioTrade> getTradesForPortfolio(int portfolioId) {
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
        return mockData.getPortfolioAssets(portfolioId);
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
        }

        // 2. Add mock portfolio transactions (keep existing mock behavior for
        // portfolios for now)
        List<MockDataService.UnifiedTransaction> mocks = mockData.getAllUnifiedTransactions();
        for (MockDataService.UnifiedTransaction ut : mocks) {
            if ("PORTFOLIO".equals(ut.getSource())) {
                list.add(ut);
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

    // ============================================================
    // PORTFOLIO METADATA
    // ============================================================

    /**
     * Simple DTO to expose portfolio id and display name to the UI.
     */
    public static class PortfolioInfo {
        private final int id;
        private final String name;

        public PortfolioInfo(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
