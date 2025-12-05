package org.groupm.ewallet.webapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;
import org.groupm.ewallet.model.Account;
import org.groupm.ewallet.model.Transaction;
import org.groupm.ewallet.webapp.connector.ExternalAsset;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Central application service.
 * - Talks to the backend REST API (users, portfolios, assets, etc.).
 * - Now fully integrated with the backend for accounts and transactions.
 */
@ApplicationScoped
public class WebAppService {

    private static final String BASE_URL = "http://localhost:8080/webservice/api";

    // ============================================================
    // LOGIN
    // ============================================================

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
            e.printStackTrace();
            return null;
        }
    }

    // ============================================================
    // REGISTER
    // ============================================================

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
    // ACCOUNTS (BACKEND INTEGRATION)
    // ============================================================

    /**
     * Retrieves the list of accounts for a given user from the backend service.
     */
    public List<Account> getAccountsForUser(String userId) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(BASE_URL + "/accounts/user/" + userId); // Correct endpoint for accounts by
                                                                                     // user?
            // Wait, AccountResource doesn't have /accounts/user/{userId} GET endpoint?
            // Let's check AccountResource again. It has createAccountForUser but maybe not
            // getAccountsForUser?
            // AccountManager has getAccountsByUser.
            // Let's check if there is an endpoint.
            // If not, I might need to add it or use the User resource to get accounts.
            // UserResource usually returns the user with accounts.

            // Re-reading AccountResource...
            // It has:
            // GET /api/accounts (all)
            // POST /api/accounts
            // POST /api/accounts/user/{userId}
            // GET /api/accounts/{id}
            // DELETE /api/accounts/{id}
            // PUT /api/accounts/{id}

            // It seems AccountResource does NOT have GET /accounts/user/{userId}.
            // But UserResource likely returns the user with their accounts.
            // Let's use UserResource to get the user and then extract accounts.

            WebTarget userTarget = client.target(BASE_URL + "/users/" + userId);
            Response res = userTarget.request(MediaType.APPLICATION_JSON).get();

            if (res.getStatus() != 200) {
                return List.of();
            }

            // We can deserialize to User object if we have it, or parse JSON.
            // Since we have the model, let's try to use it, but we need to be careful with
            // JSON-B.
            // Let's parse JSON manually to be safe as before, or use GenericType.

            String json = res.readEntity(String.class);
            var obj = Json.createReader(new StringReader(json)).readObject();

            if (!obj.containsKey("accounts")) {
                return List.of();
            }

            var accountsJson = obj.getJsonArray("accounts");
            List<Account> accounts = new ArrayList<>();

            for (var a : accountsJson) {
                var accObj = a.asJsonObject();
                Account acc = new Account();
                acc.setAccountID(accObj.getString("accountID"));
                acc.setType(accObj.getString("type"));
                acc.setBalance(accObj.getJsonNumber("balance").doubleValue());
                acc.setUserID(userId);
                // Handle name if present
                if (accObj.containsKey("name") && !accObj.isNull("name")) {
                    acc.setName(accObj.getString("name"));
                }
                accounts.add(acc);
            }

            return accounts;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Creates a new account for the given user.
     */
    public boolean createAccount(String userId, String type, String name) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(BASE_URL + "/accounts/user/" + userId);

            Account account = new Account();
            account.setAccountID("A" + System.currentTimeMillis()); // Generate ID client-side or let backend handle it?
            // AccountResource expects accountID to be set in the body for validation: "Le
            // champ 'accountID' est obligatoire."
            // So we generate it here.
            account.setType(type);
            account.setName(name);
            account.setBalance(0.0);

            Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(account));

            return response.getStatus() == 201 || response.getStatus() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Account getAccountById(String id) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(BASE_URL + "/accounts/" + id);
            Response response = target.request(MediaType.APPLICATION_JSON).get();

            if (response.getStatus() == 200) {
                return response.readEntity(Account.class);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean depositToAccount(String accountId, double amount) {
        return createTransaction(accountId, "deposit", amount, "Deposit", null);
    }

    public boolean withdrawFromAccount(String accountId, double amount, String category, String description) {
        return createTransaction(accountId, "withdraw", amount, description, category); // Note: category not in
                                                                                        // Transaction model?
        // Transaction model has 'description', but 'category' is not explicitly there?
        // Wait, Transaction.java has: type, amount, timestamp, description, accountID.
        // It does NOT have 'category'.
        // The 'category' was in LocalTransaction.
        // We can append category to description or just ignore it.
        // Let's append it to description.
    }

    private boolean createTransaction(String accountId, String type, double amount, String description,
            String category) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(BASE_URL + "/transactions");

            Transaction tx = new Transaction();
            tx.setAccountID(accountId);
            tx.setType(type);
            tx.setAmount(amount);

            String desc = description;
            if (category != null && !category.isBlank()) {
                desc = (desc == null ? "" : desc) + " [" + category + "]";
            }
            tx.setDescription(desc);

            Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(tx));

            return response.getStatus() == 201 || response.getStatus() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean transferBetweenAccounts(String fromId, String toId, double amount, String category,
            String description) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(BASE_URL + "/transactions/transfer");

            String payload = """
                        {
                          "fromAccount":"%s",
                          "toAccount":"%s",
                          "amount":%s,
                          "category":"%s",
                          "description":"%s"
                        }
                    """.formatted(fromId, toId, amount, category, description);

            Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(payload));

            return response.getStatus() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getTransactionsForAccount(String accountId) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(BASE_URL + "/transactions/account/" + accountId);
            Response response = target.request(MediaType.APPLICATION_JSON).get();

            if (response.getStatus() == 200) {
                return response.readEntity(new GenericType<List<Transaction>>() {
                });
            }
            return List.of();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // ============================================================
    // PORTFOLIOS
    // ============================================================

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
                display.add("%s (%s) : %.2f × %.2f = %.2f".formatted(name, symbol, qty, unit, total));
            }
            return display;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public boolean addAssetToPortfolio(int portfolioId, String name, String type, double qty, double unitPrice,
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

    private List<ExternalAsset> loadCryptoAssets() {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd");
            String json = target.request(MediaType.APPLICATION_JSON).get(String.class);
            var arr = Json.createReader(new StringReader(json)).readArray();
            List<ExternalAsset> list = new ArrayList<>();
            for (var v : arr) {
                var o = v.asJsonObject();
                list.add(new ExternalAsset(o.getString("name", ""), o.getString("symbol", "").toUpperCase(),
                        o.getString("id", "")));
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private List<ExternalAsset> loadStockAssets() {
        try {
            if (FINNHUB_KEY == null || FINNHUB_KEY.isBlank())
                return List.of();
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

    private List<ExternalAsset> loadEtfAssets() {
        try {
            if (FINNHUB_KEY == null || FINNHUB_KEY.isBlank())
                return List.of();
            Client client = ClientBuilder.newClient();
            String url = "https://finnhub.io/api/v1/stock/symbol?exchange=US&token=" + FINNHUB_KEY;
            WebTarget target = client.target(url);
            String json = target.request(MediaType.APPLICATION_JSON).get(String.class);
            var arr = Json.createReader(new StringReader(json)).readArray();
            List<ExternalAsset> etfs = new ArrayList<>();
            for (var v : arr) {
                var o = v.asJsonObject();
                String symbol = o.getString("symbol", null);
                String name = o.getString("description", "");
                if (symbol == null || name.isBlank())
                    continue;
                String lower = name.toLowerCase();
                if (lower.contains("etf") || lower.contains("fund") || lower.contains("trust")
                        || lower.contains("index") || lower.contains("bond")) {
                    etfs.add(new ExternalAsset(name, symbol, symbol));
                }
            }
            return etfs;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private double getCryptoPrice(String apiId) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(
                    "https://api.coingecko.com/api/v3/simple/price?ids=" + apiId.toLowerCase() + "&vs_currencies=usd");
            String json = target.request(MediaType.APPLICATION_JSON).get(String.class);
            var obj = Json.createReader(new StringReader(json)).readObject();
            if (obj.containsKey(apiId.toLowerCase())) {
                return obj.getJsonObject(apiId.toLowerCase()).getJsonNumber("usd").doubleValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private double getStockEtfPrice(String symbol) {
        if (FINNHUB_KEY == null)
            return 0.0;
        try {
            String url = "https://finnhub.io/api/v1/quote?symbol=" + symbol + "&token=" + FINNHUB_KEY;
            Client client = ClientBuilder.newClient();
            String json = client.target(url).request(MediaType.APPLICATION_JSON).get(String.class);
            var obj = Json.createReader(new StringReader(json)).readObject();
            if (obj.containsKey("c")) {
                return obj.isNull("c") ? 0.0 : obj.getJsonNumber("c").doubleValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public List<ExternalAsset> loadAssetsFromApi(String type) {
        return switch (type.toLowerCase()) {
            case "crypto" -> loadCryptoAssets();
            case "stock" -> loadStockAssets();
            case "etf" -> loadEtfAssets();
            default -> List.of();
        };
    }

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

    public double getTotalWealthForUser(String userId) {
        return 423_817.00;
    }

    public double getWealthGrowthForUser(String userId) {
        return 1.93;
    }

    // ============================================================
    // PORTFOLIO TRADES (IN-MEMORY FOR PNL)
    // ============================================================

    // ============================================================
    // PORTFOLIO TRADES (IN-MEMORY FOR PNL)
    // ============================================================

    public static class PortfolioTrade {
        private final int portfolioId;
        private final String assetName;
        private final String symbol;
        private final String type;
        private final double quantity;
        private final double unitPrice;
        private final LocalDateTime dateTime;

        public PortfolioTrade(int portfolioId, String assetName, String symbol, String type, double quantity,
                double unitPrice, LocalDateTime dateTime) {
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

        public double getSignedNotional() {
            double sign = "SELL".equalsIgnoreCase(type) ? -1.0 : 1.0;
            return sign * quantity * unitPrice;
        }
    }

    private final List<PortfolioTrade> portfolioTrades = new ArrayList<>();

    public void recordPortfolioTrade(int portfolioId, String assetName, String symbol, String type, double quantity,
            double unitPrice) {
        portfolioTrades.add(
                new PortfolioTrade(portfolioId, assetName, symbol, type, quantity, unitPrice, LocalDateTime.now()));
    }

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

    public static class UnifiedTransaction {
        private final String source;
        private final String sourceId;
        private final String sourceLabel;
        private final LocalDateTime dateTime;
        private final double amount;
        private final String type;
        private final String category;
        private final String description;

        public UnifiedTransaction(String source, String sourceId, String sourceLabel, LocalDateTime dateTime,
                double amount, String type, String category, String description) {
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

    public List<UnifiedTransaction> getAllUnifiedTransactions(String userId) {
        List<UnifiedTransaction> list = new ArrayList<>();

        // 1) Map account transactions from Backend
        List<Account> accounts = getAccountsForUser(userId);
        for (Account acc : accounts) {
            List<Transaction> txs = getTransactionsForAccount(acc.getAccountID());
            for (Transaction tx : txs) {
                String label = (acc.getName() != null && !acc.getName().isBlank())
                        ? acc.getName()
                        : acc.getType() + " " + acc.getAccountID();

                list.add(new UnifiedTransaction(
                        "ACCOUNT",
                        acc.getAccountID(),
                        label,
                        tx.getTimestamp(),
                        tx.getAmount(),
                        tx.getType(),
                        "BANK_TX", // Category not in Transaction model, using placeholder
                        tx.getDescription()));
            }
        }

        // 2) Map portfolio trades (Local)
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
    // PORTFOLIO METADATA (NAMES)
    // ============================================================

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
