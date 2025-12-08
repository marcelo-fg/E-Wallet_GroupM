package org.groupm.ewallet.webapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.groupm.ewallet.webapp.model.LocalAccount;
import org.groupm.ewallet.webapp.model.LocalTransaction;
import org.groupm.ewallet.webapp.model.PortfolioTrade;
import org.groupm.ewallet.webapp.model.PortfolioAsset;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mock data service for in-memory accounts, transactions, and portfolio trades.
 * In a real project, this would be backed by a persistent backend.
 */
@ApplicationScoped
public class MockDataService {

    // ============================================================
    // IN-MEMORY STORAGE
    // ============================================================

    private final List<LocalAccount> localAccounts = new ArrayList<>();
    private final List<LocalTransaction> localTransactions = new ArrayList<>();
    private final List<PortfolioTrade> portfolioTrades = new ArrayList<>();
    private final List<PortfolioAsset> portfolioAssets = new ArrayList<>();

    // ============================================================
    // ACCOUNT OPERATIONS
    // ============================================================

    /**
     * Returns all local accounts.
     */
    public List<LocalAccount> getAccounts() {
        return localAccounts;
    }

    /**
     * Creates a new local account with a zero initial balance.
     */
    public LocalAccount createAccount(String type, String name) {
        String nextId = String.valueOf(localAccounts.size() + 1);
        LocalAccount acc = new LocalAccount(nextId, type, name, 0.0);
        localAccounts.add(acc);
        return acc;
    }

    /**
     * Compatibility overload for the older signature without a name.
     */
    public LocalAccount createAccount(String type) {
        return createAccount(type, null);
    }

    /**
     * Looks up a local account by its identifier.
     */
    public LocalAccount getAccountById(String id) {
        return localAccounts.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Deletes an account by ID.
     */
    public boolean deleteAccount(String id) {
        return localAccounts.removeIf(a -> a.getId().equals(id));
    }

    // ============================================================
    // TRANSACTION OPERATIONS
    // ============================================================

    /**
     * Simple deposit operation and positive transaction recording.
     */
    public boolean depositToAccount(String id, double amount) {
        LocalAccount acc = getAccountById(id);
        if (acc == null || amount <= 0) {
            return false;
        }

        acc.setBalance(acc.getBalance() + amount);

        localTransactions.add(new LocalTransaction(
                id,
                LocalDateTime.now(),
                amount,
                "DEPOSIT",
                null,
                "Deposit"));

        return true;
    }

    /**
     * Withdrawal operation with basic balance check and negative transaction.
     */
    public boolean withdrawFromAccount(String id,
            double amount,
            String category,
            String description) {

        LocalAccount acc = getAccountById(id);
        if (acc == null || amount <= 0) {
            return false;
        }

        double newBalance = acc.getBalance() - amount;
        if (newBalance < 0) {
            // In a real service, a business exception could be thrown here
            return false;
        }

        acc.setBalance(newBalance);

        localTransactions.add(new LocalTransaction(
                id,
                LocalDateTime.now(),
                -amount,
                "WITHDRAWAL",
                category,
                description));

        return true;
    }

    /**
     * Local transfer between two accounts:
     * - withdrawal from source
     * - deposit to target
     * No compensation is performed on partial failure (simplified behavior).
     */
    public boolean transferBetweenAccounts(String fromAccountId,
            String toAccountId,
            double amount,
            String category,
            String description) {

        if (fromAccountId == null || toAccountId == null ||
                fromAccountId.equals(toAccountId) || amount <= 0) {
            return false;
        }

        boolean withdrawalOk = withdrawFromAccount(fromAccountId, amount, category, description);
        if (!withdrawalOk) {
            return false;
        }

        boolean depositOk = depositToAccount(toAccountId, amount);
        return depositOk;
    }

    /**
     * Returns all local transactions for a given account.
     */
    public List<LocalTransaction> getTransactionsForAccount(String accountId) {
        List<LocalTransaction> result = new ArrayList<>();
        for (LocalTransaction tx : localTransactions) {
            if (tx.getAccountId().equals(accountId)) {
                result.add(tx);
            }
        }
        return result;
    }

    /**
     * Placeholder for portfolio-related account-style transactions.
     * Currently returns an empty list until a real backend exists.
     */
    public List<LocalTransaction> getTransactionsForPortfolio(int portfolioId) {
        return List.of();
    }

    // ============================================================
    // PORTFOLIO TRADE OPERATIONS
    // ============================================================

    /**
     * Records a portfolio trade (BUY or SELL) in the in-memory list.
     */
    public void recordPortfolioTrade(int portfolioId,
            String assetName,
            String symbol,
            String type,
            double quantity,
            double unitPrice) {
        portfolioTrades.add(new PortfolioTrade(
                portfolioId,
                assetName,
                symbol,
                type,
                quantity,
                unitPrice,
                LocalDateTime.now()));
    }

    /**
     * Returns all trades for a given portfolio id.
     */
    public List<PortfolioTrade> getTradesForPortfolio(int portfolioId) {
        List<PortfolioTrade> result = new ArrayList<>();
        for (PortfolioTrade trade : portfolioTrades) {
            if (trade.getPortfolioId() == portfolioId) {
                result.add(trade);
            }
        }
        return result;
    }

    // ==============================================================
    // UNIFIED TRANSACTIONS
    // ============================================================

    /**
     * DTO used by the global Transactions page to unify account and
     * portfolio transactions into a single table.
     */
    public static class UnifiedTransaction {
        private final String source; // "ACCOUNT" or "PORTFOLIO"
        private final String sourceId; // account id or portfolio id
        private final String sourceLabel; // human readable label
        private final LocalDateTime dateTime;
        private final double amount;
        private final String type;
        private final String category;
        private final String description;

        public UnifiedTransaction(String source,
                String sourceId,
                String sourceLabel,
                LocalDateTime dateTime,
                double amount,
                String type,
                String category,
                String description) {
            this.source = source;
            this.sourceId = sourceId;
            this.sourceLabel = sourceLabel;
            this.dateTime = dateTime;
            this.amount = amount;
            this.type = type;
            this.category = category;
            this.description = description;
        }

        public String getSource() {
            return source;
        }

        public String getSourceId() {
            return sourceId;
        }

        public String getSourceLabel() {
            return sourceLabel;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        /**
         * Returns a user-friendly formatted date without time.
         */
        public String getFormattedDateTime() {
            if (dateTime == null) {
                return "";
            }
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    .ofPattern("yyyy-MM-dd");
            return dateTime.format(formatter);
        }

        public double getAmount() {
            return amount;
        }

        public String getType() {
            return type;
        }

        public String getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Returns all local bank and portfolio trades mapped as unified transactions.
     * Portfolio trades are currently valued in USD; currency conversion is out of
     * scope.
     */
    public List<UnifiedTransaction> getAllUnifiedTransactions() {
        List<UnifiedTransaction> list = new ArrayList<>();

        // 1) Map account transactions
        for (LocalTransaction tx : localTransactions) {
            LocalAccount acc = getAccountById(tx.getAccountId());
            String label = (acc != null && acc.getName() != null && !acc.getName().isBlank())
                    ? acc.getName()
                    : (acc != null ? acc.getType() + " " + acc.getId() : tx.getAccountId());

            list.add(new UnifiedTransaction(
                    "ACCOUNT",
                    tx.getAccountId(),
                    label,
                    tx.getDateTime(),
                    tx.getAmount(),
                    tx.getType(),
                    tx.getCategory(),
                    tx.getDescription()));
        }

        // 2) Map portfolio trades
        for (PortfolioTrade trade : portfolioTrades) {
            String label = trade.getAssetName() + " (" + trade.getSymbol() + ")";
            double signedNotional = trade.getSignedNotional();

            list.add(new UnifiedTransaction(
                    "PORTFOLIO",
                    String.valueOf(trade.getPortfolioId()),
                    label,
                    trade.getDateTime(),
                    signedNotional,
                    trade.getType(),
                    "PORTFOLIO_TRADE",
                    "Portfolio " + trade.getPortfolioId() + " " + trade.getType()
                            + " " + trade.getQuantity() + " @ " + trade.getUnitPrice()));
        }

        return list;
    }

    // ============================================================
    // PORTFOLIO ASSET OPERATIONS (IN-MEMORY)
    // ============================================================

    /**
     * Adds an asset to a portfolio (in-memory storage).
     */
    public void addPortfolioAsset(int portfolioId, String assetName, String symbol,
            String type, double quantity, double unitPrice) {
        portfolioAssets.add(new PortfolioAsset(
                portfolioId,
                assetName,
                symbol,
                type,
                quantity,
                unitPrice,
                LocalDateTime.now()));
    }

    /**
     * Returns all assets for a given portfolio.
     */
    public List<PortfolioAsset> getPortfolioAssets(int portfolioId) {
        return portfolioAssets.stream()
                .filter(asset -> asset.getPortfolioId() == portfolioId)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing asset quantity (for buy/sell operations).
     */
    public boolean updateAssetQuantity(int portfolioId, String symbol, double newQuantity) {
        for (PortfolioAsset asset : portfolioAssets) {
            if (asset.getPortfolioId() == portfolioId && asset.getSymbol().equals(symbol)) {
                if (newQuantity <= 0) {
                    portfolioAssets.remove(asset);
                } else {
                    asset.setQuantity(newQuantity);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Finds an asset in a portfolio by symbol.
     */
    public PortfolioAsset getPortfolioAsset(int portfolioId, String symbol) {
        return portfolioAssets.stream()
                .filter(asset -> asset.getPortfolioId() == portfolioId &&
                        asset.getSymbol().equals(symbol))
                .findFirst()
                .orElse(null);
    }
}
