package org.groupm.ewallet.webapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import org.groupm.ewallet.webapp.connector.ExternalAsset;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Central application service.
 * - Talks to the backend REST API (users, portfolios, assets, etc.).
 * - Provides in-memory mocks for bank accounts and portfolio trades.
 *
 * Note: for accounts and portfolio trades everything is currently in memory
 * to keep the UI prototype simple.
 */
@ApplicationScoped
public class WebAppService {

    private static final String BASE_URL = "http://localhost:8080/webservice/api";

    // ============================================================
    // LOGIN
    // ============================================================

    /**
     * Performs a login request against the backend and returns the technical user id.
     * The backend may return "userID" or "id" depending on the implementation.
     */
    public String login(String email, String password) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(BASE_URL + "/users/login");

            String payload = """
                {"email":"%s","password":"%s"}
            """.formatted(email, password);

            Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(payload));

            if (response.getStatus() != 200) {
                return null;
            }

            String json = response.readEntity(String.class);
            var obj = Json.createReader(new StringReader(json)).readObject();

            if (obj.containsKey("userID")) {
                return obj.getString("userID");
            }
            if (obj.containsKey("id")) {
                return obj.get("id").toString();
            }

            return null;

        } catch (Exception e) {
            // In a real project, use a structured logger instead of printStackTrace
            e.printStackTrace();
            return null;
        }
    }

    // ============================================================
    // REGISTER
    // ============================================================

    /**
     * Registers a new user in the backend.
     */
    public boolean registerUser(String firstname, String lastname, String email, String password) {
        try (Client client = ClientBuilder.newClient()) {

            WebTarget target = client.target(BASE_URL + "/users/register");

            String payload = """
                {"firstName":"%s","lastName":"%s","email":"%s","password":"%s"}
            """.formatted(firstname, lastname, email, password);

            Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(payload));

            return response.getStatus() == 200 || response.getStatus() == 201;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // ============================================================ 
    // GET USER DETAILS (RAW JSON) 
    // ============================================================ 

    /** 
     * Retrieves the full user details (including accounts and portfolios) as a JsonObject. 
     * Useful for calculations in the UI. 
     */ 
    public jakarta.json.JsonObject getUserDetails(String userId) { 
        try (Client client = ClientBuilder.newClient()) { 
            WebTarget target = client.target(BASE_URL + "/users/" + userId); 
            Response res = target.request(MediaType.APPLICATION_JSON_TYPE).get(); 

            if (res.getStatus() != 200) { 
                return null; 
            } 

            String json = res.readEntity(String.class); 
            return Json.createReader(new StringReader(json)).readObject(); 
        } catch (Exception e) { 
            e.printStackTrace(); 
            return null; 
        } 
    } 

    // ============================================================ 
    // GET WEALTH TRACKER (BACKEND) 
    // ============================================================ 

    /** 
     * Retrieves the WealthTracker object for a given user. 
     * Contains total wealth, cash, crypto, stocks, and growth rate. 
     */ 
    public jakarta.json.JsonObject getWealthForUser(String userId) { 
        try (Client client = ClientBuilder.newClient()) { 
            WebTarget target = client.target(BASE_URL + "/wealth/" + userId); 
            Response res = target.request(MediaType.APPLICATION_JSON_TYPE).get(); 

            if (res.getStatus() != 200) { 
                return null; 
            } 

            String json = res.readEntity(String.class); 
            return Json.createReader(new StringReader(json)).readObject(); 
        } catch (Exception e) { 
            e.printStackTrace(); 
            return null; 
        } 
    } 

    // GET USER ACCOUNTS (BACKEND)
    // ============================================================

    /**
     * Retrieves the list of accounts for a given user from the backend service.
     * The result is formatted as display strings (id + balance + currency).
     */
    public List<String> getAccountsForUser(String userId) {
        try (Client client = ClientBuilder.newClient()) {

            WebTarget target = client.target(BASE_URL + "/users/" + userId);

            Response res = target.request(MediaType.APPLICATION_JSON_TYPE).get();

            if (res.getStatus() != 200) {
                return List.of();
            }

            String json = res.readEntity(String.class);

            var obj = Json.createReader(new StringReader(json)).readObject();
            var accountsJson = obj.getJsonArray("accounts");

            List<String> out = new ArrayList<>();

            for (var a : accountsJson) {
                var acc = a.asJsonObject();
                out.add(acc.getString("accountID") + " - " +
                        acc.getJsonNumber("balance").doubleValue() + " CHF");
            }

            return out;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // ============================================================
    // TRANSFERS (BACKEND)
    // ============================================================

    /**
     * Performs a bank transfer between two backend accounts.
     */
    public boolean makeTransfer(String fromAccount, String toAccount, double amount) {
        try (Client client = ClientBuilder.newClient()) {

            WebTarget target = client.target(BASE_URL + "/transactions/transfer");

            String payload = """
                {
                  "fromAccount":"%s",
                  "toAccount":"%s",
                  "amount":%s
                }
            """.formatted(fromAccount, toAccount, amount);

            Response response = target.request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(payload));

            return response.getStatus() == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // PORTFOLIOS
    // ============================================================

    /**
     * Returns the list of portfolio ids for a given user.
     */
    public List<Integer> getPortfoliosForUser(String userId) {
        try (Client client = ClientBuilder.newClient()) {

            WebTarget target = client.target(BASE_URL + "/portfolios");
            Response res = target.request(MediaType.APPLICATION_JSON).get();

            if (res.getStatus() != 200) {
                return List.of();
            }

            String json = res.readEntity(String.class);
            var arr = Json.createReader(new StringReader(json)).readArray();

            List<Integer> list = new ArrayList<>();

            for (var v : arr) {
                var obj = v.asJsonObject();
                if (obj.containsKey("userID") && userId.equals(obj.getString("userID"))) {
                    list.add(obj.getInt("id"));
                }
            }

            return list;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Creates a new portfolio for the given user.
     */
    public boolean createPortfolioForUser(String userId) {
        try (Client client = ClientBuilder.newClient()) {

            WebTarget target = client.target(BASE_URL + "/portfolios");

            String payload = """
                {"userID":"%s"}
            """.formatted(userId);

            Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.json(payload));

            return res.getStatus() == 200 || res.getStatus() == 201;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // PORTFOLIO ASSETS (BACKEND)
    // ============================================================

    /**
     * Returns a display-ready list of assets for a given portfolio.
     * Values are read from the backend and formatted for the UI.
     */
    public List<String> getAssetsForPortfolio(int portfolioId) {
        try (Client client = ClientBuilder.newClient()) {

            WebTarget target = client.target(BASE_URL + "/assets/portfolio/" + portfolioId);
            Response res = target.request(MediaType.APPLICATION_JSON).get();

            if (res.getStatus() != 200) {
                return List.of();
            }

            String json = res.readEntity(String.class);
            var arr = Json.createReader(new StringReader(json)).readArray();

            List<String> display = new ArrayList<>();

            for (var v : arr) {
                var obj = v.asJsonObject();

                String name = obj.getString("assetName", "");
                String symbol = obj.getString("symbol", "");
                double qty = obj.getJsonNumber("quantity").doubleValue();
                double unit = obj.getJsonNumber("unitValue").doubleValue();
                double total = qty * unit;

                display.add("%s (%s) : %.2f × %.2f = %.2f"
                        .formatted(name, symbol, qty, unit, total));
            }

            return display;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Adds a new asset to the given portfolio in the backend.
     */
    public boolean addAssetToPortfolio(int portfolioId,
                                       String name,
                                       String type,
                                       double qty,
                                       double unitPrice,
                                       String symbol) {
        try (Client client = ClientBuilder.newClient()) {

            WebTarget target = client.target(BASE_URL + "/assets/portfolio/" + portfolioId);

            String payload = """
                {
                  "assetName":"%s",
                  "type":"%s",
                  "quantity":%s,
                  "unitValue":%s,
                  "symbol":"%s"
                }
            """.formatted(name, type, qty, unitPrice, symbol);

            Response res = target.request(MediaType.APPLICATION_JSON).post(Entity.json(payload));

            return res.getStatus() == 200 || res.getStatus() == 201;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // MARKET DATA
    // ============================================================

    private static final String FINNHUB_KEY = System.getenv("FINNHUB_API_KEY");

    /**
     * Loads a list of crypto assets from CoinGecko.
     */
    private List<ExternalAsset> loadCryptoAssets() {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(
                    "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd"
            );

            String json = target.request(MediaType.APPLICATION_JSON).get(String.class);
            var arr = Json.createReader(new StringReader(json)).readArray();

            List<ExternalAsset> list = new ArrayList<>();

            for (var v : arr) {
                var o = v.asJsonObject();
                list.add(new ExternalAsset(
                        o.getString("name", ""),
                        o.getString("symbol", "").toUpperCase(),
                        o.getString("id", "")
                ));
            }
            return list;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Loads a list of US stocks from Finnhub.
     */
    private List<ExternalAsset> loadStockAssets() {
        try {
            if (FINNHUB_KEY == null || FINNHUB_KEY.isBlank()) {
                System.err.println("FINNHUB_API_KEY missing!");
                return List.of();
            }

            Client client = ClientBuilder.newClient();
            String url = "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=" + FINNHUB_KEY;

            WebTarget target = client.target(url);
            String json = target.request(MediaType.APPLICATION_JSON).get(String.class);

            var arr = Json.createReader(new StringReader(json)).readArray();
            List<ExternalAsset> list = new ArrayList<>();

            for (var v : arr) {
                var o = v.asJsonObject();

                String symbol = o.getString("symbol", null);
                String name = o.getString("description", null);

                if (symbol != null && name != null) {
                    list.add(new ExternalAsset(name, symbol, symbol));
                }
            }

            return list;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Loads a list of ETFs and fund-like instruments from Finnhub.
     */
    private List<ExternalAsset> loadEtfAssets() {
        try {
            if (FINNHUB_KEY == null || FINNHUB_KEY.isBlank()) {
                System.err.println("FINNHUB_API_KEY missing!");
                return List.of();
            }

            Client client = ClientBuilder.newClient();

            String url =
                    "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=" + FINNHUB_KEY;
            WebTarget target = client.target(url);

            String json = target.request(MediaType.APPLICATION_JSON).get(String.class);
            var arr = Json.createReader(new StringReader(json)).readArray();

            List<ExternalAsset> etfs = new ArrayList<>();

            for (var v : arr) {

                var o = v.asJsonObject();

                String symbol = o.getString("symbol", null);
                String name = o.getString("description", "");
                if (symbol == null || name.isBlank()) {
                    continue;
                }

                String lower = name.toLowerCase();
                if (lower.contains("etf") ||
                        lower.contains("fund") ||
                        lower.contains("trust") ||
                        lower.contains("index") ||
                        lower.contains("bond")) {
                    etfs.add(new ExternalAsset(name, symbol, symbol));
                }
            }

            return etfs;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Requests the USD price of a crypto asset from CoinGecko.
     */
    private double getCryptoPrice(String apiId) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(
                    "https://api.coingecko.com/api/v3/simple/price?ids=" +
                            apiId.toLowerCase() + "&vs_currencies=usd"
            );

            String json = target.request(MediaType.APPLICATION_JSON).get(String.class);
            var obj = Json.createReader(new StringReader(json)).readObject();

            if (obj.containsKey(apiId.toLowerCase())) {
                return obj.getJsonObject(apiId.toLowerCase())
                        .getJsonNumber("usd").doubleValue();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Requests the current price of a stock or ETF from Finnhub.
     */
    private double getStockEtfPrice(String symbol) {

        if (FINNHUB_KEY == null) {
            return 0.0;
        }

        try {
            String url = "https://finnhub.io/api/v1/quote?symbol=" +
                    symbol + "&token=" + FINNHUB_KEY;

            Client client = ClientBuilder.newClient();
            String json = client.target(url)
                    .request(MediaType.APPLICATION_JSON)
                    .get(String.class);

            var obj = Json.createReader(new StringReader(json)).readObject();

            if (obj.containsKey("c")) {
                try {
                    return obj.isNull("c") ? 0.0 : obj.getJsonNumber("c").doubleValue();
                } catch (Exception ignore) {
                    // ignore parsing issues
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    /**
     * High-level API used by the UI to load external assets by type.
     */
    public List<ExternalAsset> loadAssetsFromApi(String type) {
        return switch (type.toLowerCase()) {
            case "crypto" -> loadCryptoAssets();
            case "stock" -> loadStockAssets();
            case "etf" -> loadEtfAssets();
            default -> List.of();
        };
    }

    /**
     * High-level lookup for a single asset price based on its id or symbol.
     */
    public double getPriceForAsset(String idOrSymbol, String type) {
        return switch (type.toLowerCase()) {
            case "crypto" -> getCryptoPrice(idOrSymbol);
            case "stock", "etf" -> getStockEtfPrice(idOrSymbol);
            default -> 0.0;
        };
    }

    // ============================================================
    // DASHBOARD MOCK
    // ============================================================

    /**
     * Mocked total wealth value, until a real aggregated calculation is available.
     */
    public double getTotalWealthForUser(String userId) {
        return 423_817.00;
    }

    /**
     * Mocked wealth growth percentage, until a real calculation is available.
     */
    public double getWealthGrowthForUser(String userId) {
        return 1.93;
    }

    // ============================================================
    // ACCOUNTS (LOCAL MOCK) + TRANSACTIONS
    // ============================================================

    /**
     * Simple in-memory bank account representation.
     * In a real project, this would be backed by a persistent backend.
     */
    public static class LocalAccount {
        private final String id;
        private final String type;
        private final String name;
        private double balance;

        public LocalAccount(String id, String type, String name, double balance) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.balance = balance;
        }

        public String getId() {
            return id;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public double getBalance() {
            return balance;
        }

        /**
         * For now we reuse the id as display number.
         */
        public String getNumber() {
            return id;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }
    }

    /**
     * Simple in-memory bank transaction, used to feed the account history UI.
     */
    public static class LocalTransaction {
        private final String accountId;
        private final LocalDateTime dateTime;
        private final double amount;
        private final String type;
        private final String category;
        private final String description;

        public LocalTransaction(String accountId,
                                LocalDateTime dateTime,
                                double amount,
                                String type,
                                String category,
                                String description) {
            this.accountId = accountId;
            this.dateTime = dateTime;
            this.amount = amount;
            this.type = type;
            this.category = category;
            this.description = description;
        }

        public String getAccountId() {
            return accountId;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
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
     * In-memory store for local accounts and their transactions.
     */
    private final List<LocalAccount> localAccounts = new ArrayList<>();
    private final List<LocalTransaction> localTransactions = new ArrayList<>();

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
                "Deposit"
        ));

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
                description
        ));

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
    // PORTFOLIO TRADES (IN-MEMORY FOR PNL)
    // ============================================================

    /**
     * In-memory portfolio trade used for PnL and global transaction view.
     */
    public static class PortfolioTrade {
        private final int portfolioId;
        private final String assetName;
        private final String symbol;
        private final String type;      // "BUY" or "SELL"
        private final double quantity;
        private final double unitPrice; // trade price in USD for now
        private final LocalDateTime dateTime;

        public PortfolioTrade(int portfolioId,
                              String assetName,
                              String symbol,
                              String type,
                              double quantity,
                              double unitPrice,
                              LocalDateTime dateTime) {
            this.portfolioId = portfolioId;
            this.assetName = assetName;
            this.symbol = symbol;
            this.type = type;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.dateTime = dateTime;
        }

        public int getPortfolioId() {
            return portfolioId;
        }

        public String getAssetName() {
            return assetName;
        }

        public String getSymbol() {
            return symbol;
        }

        public String getType() {
            return type;
        }

        public double getQuantity() {
            return quantity;
        }

        public double getUnitPrice() {
            return unitPrice;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        /**
         * Signed notional value of the trade.
         * BUY is positive, SELL is negative.
         */
        public double getSignedNotional() {
            double sign = "SELL".equalsIgnoreCase(type) ? -1.0 : 1.0;
            return sign * quantity * unitPrice;
        }
    }

    /**
     * In-memory list of portfolio trades used to build a simple PnL history.
     */
    private final List<PortfolioTrade> portfolioTrades = new ArrayList<>();

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
                LocalDateTime.now()
        ));
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

    // ============================================================
    // UNIFIED TRANSACTIONS FOR GLOBAL VIEW
    // ============================================================

    /**
     * DTO used by the global Transactions page to unify account and
     * portfolio transactions into a single table.
     */
    public static class UnifiedTransaction {
        private final String source;        // "ACCOUNT" or "PORTFOLIO"
        private final String sourceId;      // account id or portfolio id
        private final String sourceLabel;   // human readable label
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
     * Portfolio trades are currently valued in USD; currency conversion is out of scope.
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
                    tx.getDescription()
            ));
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
                            + " " + trade.getQuantity() + " @ " + trade.getUnitPrice()
            ));
        }

        return list;
    }

    // ============================================================
    // PORTFOLIO METADATA (NAMES)
    // ============================================================

    /**
     * Simple DTO to expose portfolio id and display name to the UI.
     * Names are currently managed at UI level (session) until the
     * backend exposes a dedicated field.
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
