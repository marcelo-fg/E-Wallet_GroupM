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
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class WebAppService {

    private static final String BASE_URL = "http://localhost:8080/webservice/api";

    // ============================================================
    // =============== LOGIN ======================================
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
    // =============== REGISTER ===================================
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
    // =============== GET USER ACCOUNTS ==========================
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
    // =============== MAKE TRANSFER ===============================
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
    // =============== PORTFOLIOS =================================
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

    /**
     * Creation d'un portefeuille sans nom explicite (comportement historique).
     */
    public boolean createPortfolioForUser(String userId) {
        return createPortfolioForUser(userId, "Nouveau portefeuille");
    }

    /**
     * Creation d'un portefeuille avec nom fourni par l'utilisateur.
     * Payload JSON : { "userID": "...", "name": "..." }
     */
    public boolean createPortfolioForUser(String userId, String portfolioName) {
        try (Client client = ClientBuilder.newClient()) {

            WebTarget target = client.target(BASE_URL + "/portfolios");

            String payload = """
                {
                  "userID":"%s",
                  "name":"%s"
                }
            """.formatted(userId, portfolioName);

            Response res = target.request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(payload));

            return res.getStatus() == 200 || res.getStatus() == 201;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // =============== PORTFOLIO ASSETS ===========================
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
    // ===============  EXTERNAL MARKET DATA ======================
    // ============================================================

    private static final String FINNHUB_KEY = System.getenv("FINNHUB_API_KEY");

    // -------- LISTER LES CRYPTO --------
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

    // -------- LISTER LES ACTIONS (US MARKET) --------
    private List<ExternalAsset> loadStockAssets() {
        try {
            if (FINNHUB_KEY == null || FINNHUB_KEY.isBlank()) {
                System.err.println("FINNHUB_API_KEY manquant !");
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

    // -------- LISTER DES ETF (US) --------
    private List<ExternalAsset> loadEtfAssets() {
        try {
            if (FINNHUB_KEY == null || FINNHUB_KEY.isBlank()) {
                System.err.println("FINNHUB_API_KEY manquant !");
                return List.of();
            }

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
                if (symbol == null || name.isBlank()) continue;

                String lower = name.toLowerCase();
                if (lower.contains("etf") ||
                        lower.contains("fund") ||
                        lower.contains("trust") ||
                        lower.contains("index") ||
                        lower.contains("bond")) {

                    etfs.add(new ExternalAsset(name, symbol, symbol));
                }
            }

            System.out.println("ETF trouvés = " + etfs.size());
            return etfs;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // -------- PRIX CRYPTO --------
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

    // -------- PRIX ACTION / ETF --------
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
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    // -------- ROUTEUR --------
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
    // ===============  DASHBOARD METRICS (TEMP MOCK) =============
    // ============================================================

    public double getTotalWealthForUser(String userId) {
        // TODO: Replace with real logic (sum of all accounts + total portfolio value)
        return 423817.00;
    }

    public double getWealthGrowthForUser(String userId) {
        // TODO: Replace with real logic (calculate real variation)
        return 1.93;
    }
}
