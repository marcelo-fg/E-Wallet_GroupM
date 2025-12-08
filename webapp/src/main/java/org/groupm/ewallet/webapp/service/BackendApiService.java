package org.groupm.ewallet.webapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for backend REST API calls (users, portfolios, assets, transactions).
 */
@ApplicationScoped
public class BackendApiService {

    private static final String BASE_URL = "http://localhost:8080/webservice/api";

    // ============================================================
    // USER OPERATIONS
    // ============================================================

    /**
     * Performs a login request against the backend and returns the technical user
     * id.
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
            e.printStackTrace();
            return null;
        }
    }

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

    /**
     * Retrieves the full user details (including accounts and portfolios) as a
     * JsonObject.
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

    /**
     * Retrieves the WealthTracker object for a given user.
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

    /**
     * Retrieves the list of accounts for a given user from the backend service.
     */
    public List<org.groupm.ewallet.webapp.model.LocalAccount> getAccountsForUser(String userId) {
        try (Client client = ClientBuilder.newClient()) {

            WebTarget target = client.target(BASE_URL + "/users/" + userId);

            Response res = target.request(MediaType.APPLICATION_JSON_TYPE).get();

            if (res.getStatus() != 200) {
                return List.of();
            }

            String json = res.readEntity(String.class);

            var obj = Json.createReader(new StringReader(json)).readObject();
            if (!obj.containsKey("accounts") || obj.isNull("accounts")) {
                return List.of();
            }
            var accountsJson = obj.getJsonArray("accounts");

            List<org.groupm.ewallet.webapp.model.LocalAccount> out = new ArrayList<>();

            for (var a : accountsJson) {
                var acc = a.asJsonObject();
                String id = acc.getString("accountID");
                // Fallback name if missing
                String name = acc.containsKey("name") && !acc.isNull("name") ? acc.getString("name") : "Compte " + id;
                String type = acc.containsKey("type") && !acc.isNull("type") ? acc.getString("type") : "Standard";
                double bal = acc.getJsonNumber("balance").doubleValue();

                out.add(new org.groupm.ewallet.webapp.model.LocalAccount(id, type, name, bal));
            }

            return out;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public boolean createAccount(String userId, String type, String name) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(BASE_URL + "/accounts");
            String accountId = java.util.UUID.randomUUID().toString();
            String payload = """
                    {"accountID":"%s","userID":"%s","type":"%s","name":"%s","balance":0.0}
                    """.formatted(accountId, userId, type, name);
            Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(payload));
            return response.getStatus() == 200 || response.getStatus() == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteAccount(String accountId) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(BASE_URL + "/accounts/" + accountId);
            Response response = target.request().delete();
            return response.getStatus() == 200 || response.getStatus() == 204;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<org.groupm.ewallet.webapp.model.LocalTransaction> getTransactionsForAccount(String accountId) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(BASE_URL + "/transactions/account/" + accountId);
            Response res = target.request(MediaType.APPLICATION_JSON_TYPE).get();
            if (res.getStatus() != 200) {
                return List.of();
            }
            String json = res.readEntity(String.class);
            var array = Json.createReader(new StringReader(json)).readArray();
            List<org.groupm.ewallet.webapp.model.LocalTransaction> out = new ArrayList<>();

            for (var tVal : array) {
                var t = tVal.asJsonObject();
                // Backend returns timestamp as string (ISO format) in JSON
                java.time.LocalDateTime dt;
                try {
                    if (t.get("timestamp") instanceof jakarta.json.JsonString) {
                        String tsStr = t.getString("timestamp");
                        dt = java.time.LocalDateTime.parse(tsStr); // Assumes ISO-8601
                    } else {
                        // Fallback for numeric timestamp
                        long ts = t.getJsonNumber("timestamp").longValue();
                        dt = java.time.LocalDateTime.ofInstant(
                                java.time.Instant.ofEpochMilli(ts),
                                java.time.ZoneId.systemDefault());
                    }
                } catch (Exception e) {
                    dt = java.time.LocalDateTime.now();
                }

                String type = t.getString("type", "UNKNOWN");
                double amount = t.getJsonNumber("amount").doubleValue();
                String desc = "Transaction " + t.getString("transactionID");

                out.add(new org.groupm.ewallet.webapp.model.LocalTransaction(
                        accountId, dt, amount, type, "General", desc));
            }
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<org.groupm.ewallet.webapp.model.LocalTransaction> getTransactionsForUser(String userId) {
        try (Client client = ClientBuilder.newClient()) {
            // GET /api/transactions/user/{userId}
            WebTarget target = client.target(BASE_URL + "/transactions/user/" + userId);
            Response res = target.request(MediaType.APPLICATION_JSON_TYPE).get();

            if (res.getStatus() != 200) {
                return List.of();
            }

            String json = res.readEntity(String.class);
            var array = Json.createReader(new StringReader(json)).readArray();
            List<org.groupm.ewallet.webapp.model.LocalTransaction> out = new ArrayList<>();

            for (var tVal : array) {
                var t = tVal.asJsonObject();
                // Check if accountID is present in the transaction JSON
                String accountId = t.containsKey("accountID") && !t.isNull("accountID")
                        ? t.getString("accountID")
                        : "UNKNOWN";

                // Backend returns timestamp as string (ISO format) or number
                java.time.LocalDateTime dt;
                try {
                    if (t.containsKey("timestamp") && t.get("timestamp") instanceof jakarta.json.JsonString) {
                        String tsStr = t.getString("timestamp");
                        // Handles potential space separator "yyyy-MM-dd HH:mm:ss" vs "T"
                        tsStr = tsStr.replace(" ", "T");
                        dt = java.time.LocalDateTime.parse(tsStr);
                    } else if (t.containsKey("timestamp")) {
                        long ts = t.getJsonNumber("timestamp").longValue();
                        dt = java.time.LocalDateTime.ofInstant(
                                java.time.Instant.ofEpochMilli(ts),
                                java.time.ZoneId.systemDefault());
                    } else {
                        dt = java.time.LocalDateTime.now();
                    }
                } catch (Exception e) {
                    dt = java.time.LocalDateTime.now();
                }

                String type = t.getString("type", "UNKNOWN");
                double amount = t.getJsonNumber("amount").doubleValue();
                String tId = t.getString("transactionID", "N/A");
                String desc = t.getString("description", "Transaction " + tId);

                out.add(new org.groupm.ewallet.webapp.model.LocalTransaction(
                        accountId, dt, amount, type, "General", desc));
            }
            return out;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // ============================================================
    // TRANSACTION OPERATIONS
    // ============================================================

    /**
     * Performs a bank transfer between two backend accounts.
     */
    public boolean makeTransfer(String fromAccount, String toAccount, double amount) {
        try (Client client = ClientBuilder.newClient()) {

            WebTarget target = client.target(BASE_URL + "/transactions/transfer");

            String payload = """
                        {"fromAccount":"%s","toAccount":"%s","amount":%f}
                    """.formatted(fromAccount, toAccount, amount);

            Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(payload));

            return response.getStatus() == 200 || response.getStatus() == 201;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean createTransaction(String accountId, double amount, String type, String description) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(BASE_URL + "/transactions");
            String txnId = java.util.UUID.randomUUID().toString();

            // Note: 'usage' field often mapped to 'type' in backend or handled logic
            // Assuming Transaction entity matches JSON.
            // The JSON fields must match Transaction class setters or public fields.
            // Transaction has: transactionID, type, amount, description, accountID

            String json = """
                        {"transactionID":"%s","type":"%s","amount":%f,"description":"%s","accountID":"%s"}
                    """.formatted(txnId, type, amount, description, accountId);

            Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(json));
            return response.getStatus() == 200 || response.getStatus() == 201;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ============================================================
    // PORTFOLIO OPERATIONS
    // ============================================================

    /**
     * Returns the list of portfolio ids for a given user.
     */
    public List<Integer> getPortfoliosForUser(String userId) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(BASE_URL + "/users/" + userId);
            Response res = target.request(MediaType.APPLICATION_JSON_TYPE).get();

            if (res.getStatus() != 200) {
                return List.of();
            }

            String json = res.readEntity(String.class);
            var obj = Json.createReader(new StringReader(json)).readObject();

            if (!obj.containsKey("portfolios")) {
                return List.of();
            }

            var portfoliosJson = obj.getJsonArray("portfolios");
            List<Integer> out = new ArrayList<>();

            for (var p : portfoliosJson) {
                var pObj = p.asJsonObject();
                out.add(pObj.getInt("id"));
            }

            return out;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Creates a new portfolio for the given user.
     */
    public Integer createPortfolioForUser(String userId) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(BASE_URL + "/portfolios");

            String payload = """
                        {"userID":"%s"}
                    """.formatted(userId);

            Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.json(payload));

            if (response.getStatus() != 200 && response.getStatus() != 201) {
                return null;
            }

            String json = response.readEntity(String.class);
            var obj = Json.createReader(new StringReader(json)).readObject();
            return obj.getInt("id");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public org.groupm.ewallet.webapp.model.LocalAccount getAccount(String accountId) {
        try (Client client = ClientBuilder.newClient()) {
            WebTarget target = client.target(BASE_URL + "/accounts/" + accountId);
            Response res = target.request(MediaType.APPLICATION_JSON_TYPE).get();
            if (res.getStatus() != 200) {
                return null;
            }
            String json = res.readEntity(String.class);
            var acc = Json.createReader(new StringReader(json)).readObject();

            String id = acc.getString("accountID");
            String name = acc.containsKey("name") && !acc.isNull("name") ? acc.getString("name") : "Compte " + id;
            String type = acc.containsKey("type") && !acc.isNull("type") ? acc.getString("type") : "Standard";
            double bal = acc.getJsonNumber("balance").doubleValue();

            return new org.groupm.ewallet.webapp.model.LocalAccount(id, type, name, bal);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ============================================================
    // ASSET OPERATIONS
    // ============================================================

    /**
     * Returns a display-ready list of assets for a given portfolio.
     */
    public List<String> getAssetsForPortfolio(int portfolioId) {
        try (Client client = ClientBuilder.newClient()) {

            WebTarget target = client.target(BASE_URL + "/portfolios/" + portfolioId + "/assets");
            Response res = target.request(MediaType.APPLICATION_JSON_TYPE).get();

            if (res.getStatus() != 200) {
                System.err.println("[BackendApiService] Failed to get assets. Status: " + res.getStatus());
                return List.of();
            }

            String json = res.readEntity(String.class);
            System.out.println("[BackendApiService] Assets JSON: " + json);
            var array = Json.createReader(new StringReader(json)).readArray();

            List<String> out = new ArrayList<>();

            for (var obj : array) {
                var asset = obj.asJsonObject();
                String symbol = asset.getString("symbol", "N/A");
                String type = asset.getString("type", "");
                double qty = asset.getJsonNumber("quantity").doubleValue();
                double unitValue = asset.containsKey("unitValue") ? asset.getJsonNumber("unitValue").doubleValue()
                        : 0.0;

                out.add(String.format("%s (%s) - Qty: %.2f @ %.2f USD", symbol, type, qty, unitValue));
            }

            return out;

        } catch (Exception e) {
            System.err.println("[BackendApiService] Exception getting assets:");
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Retrieves the list of assets for a given portfolio as rich objects.
     */
    public List<org.groupm.ewallet.webapp.model.PortfolioAsset> getPortfolioAssetsFromBackend(int portfolioId) {
        try (Client client = ClientBuilder.newClient()) {

            WebTarget target = client.target(BASE_URL + "/portfolios/" + portfolioId + "/assets");
            Response res = target.request(MediaType.APPLICATION_JSON_TYPE).get();

            if (res.getStatus() != 200) {
                return List.of();
            }

            String json = res.readEntity(String.class);
            var array = Json.createReader(new StringReader(json)).readArray();

            List<org.groupm.ewallet.webapp.model.PortfolioAsset> out = new ArrayList<>();

            for (var obj : array) {
                var asset = obj.asJsonObject();
                String symbol = asset.getString("symbol", "N/A");
                String name = asset.getString("name", symbol);
                String type = asset.getString("type", "");
                double qty = asset.getJsonNumber("quantity").doubleValue();
                double unitValue = asset.containsKey("unitValue") ? asset.getJsonNumber("unitValue").doubleValue()
                        : 0.0;

                // If unitValue is 0 (backend default), try to use unitPrice if available or
                // fetch current price?
                // For now, let's treat unitValue as the current market price or cost basis.
                // The PortfolioAsset model expects: symbol, name, type, quantity, unitPrice
                // (current market price ideally)

                out.add(new org.groupm.ewallet.webapp.model.PortfolioAsset(
                        portfolioId, // portfolioId
                        name, // assetName
                        symbol, // symbol
                        type, // type
                        qty, // quantity
                        unitValue, // unitPrice
                        java.time.LocalDateTime.now() // addedAt (fallback)
                ));
            }

            return out;

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

            String url = BASE_URL + "/portfolios/" + portfolioId + "/assets";
            System.out.println("[BackendApiService] Adding asset to portfolio " + portfolioId);
            System.out.println("  URL: " + url);
            System.out.println("  Name: " + name);
            System.out.println("  Type: " + type);
            System.out.println("  Quantity: " + qty);
            System.out.println("  UnitPrice: " + unitPrice);
            System.out.println("  Symbol: " + symbol);

            WebTarget target = client.target(url);

            String payload = """
                        {"name":"%s","type":"%s","quantity":%f,"unitPrice":%f,"symbol":"%s"}
                    """.formatted(name, type, qty, unitPrice, symbol != null ? symbol : "");

            System.out.println("  Payload: " + payload);

            Response response = target.request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(payload, MediaType.APPLICATION_JSON));

            int status = response.getStatus();
            System.out.println("  Response status: " + status);

            if (status != 200 && status != 201) {
                String responseBody = response.readEntity(String.class);
                System.err.println("  Backend rejected asset addition. Response: " + responseBody);
            }

            return status == 200 || status == 201;

        } catch (Exception e) {
            System.err.println("[BackendApiService] Exception adding asset: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
