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
 * - Provides in-memory mocks for bank accounts and transactions.
 *
 * Note: for accounts and transactions everything is currently in memory
 * to keep the UI prototype simple.
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
    // GET USER ACCOUNTS (BACKEND)
    // ============================================================

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
    // PORTFOLIO ASSETS
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

                display.add("%s (%s) : %.2f × %.2f = %.2f"
                        .formatted(name, symbol, qty, unit, total));
            }

            return display;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

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
    // ACCOUNTS (LOCAL MOCK) + TRANSACTIONS
    // ============================================================

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

        public String getNumber() {
            return id;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }
    }

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

    private final List<LocalAccount> localAccounts = new ArrayList<>();
    private final List<LocalTransaction> localTransactions = new ArrayList<>();

    public List<LocalAccount> getAccounts() {
        return localAccounts;
    }

    public LocalAccount createAccount(String type, String name) {
        String nextId = String.valueOf(localAccounts.size() + 1);
        LocalAccount acc = new LocalAccount(nextId, type, name, 0.0);
        localAccounts.add(acc);
        return acc;
    }

    public LocalAccount createAccount(String type) {
        return createAccount(type, null);
    }

    public LocalAccount getAccountById(String id) {
        return localAccounts.stream()
                .filter(a -> a.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

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

    public List<LocalTransaction> getTransactionsForAccount(String accountId) {
        List<LocalTransaction> result = new ArrayList<>();
        for (LocalTransaction tx : localTransactions) {
            if (tx.getAccountId().equals(accountId)) {
                result.add(tx);
            }
        }
        return result;
    }

    public List<LocalTransaction> getTransactionsForPortfolio(int portfolioId) {
        return List.of();
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

    public List<UnifiedTransaction> getAllUnifiedTransactions() {
        List<UnifiedTransaction> list = new ArrayList<>();
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
        return list;
    }
}
