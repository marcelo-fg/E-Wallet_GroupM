package org.groupm.ewallet.webapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.Json;

/**
 * Service central côté WebApp pour communiquer avec le backend.
 * Pour l'instant, la méthode login(...) est simulée.
 * Tu pourras plus tard remplacer la logique par un appel REST réel.
 */
@ApplicationScoped
public class WebAppService {

    // TODO: quand ton endpoint REST de login sera prêt,
    // tu pourras mettre ici l'URL de ton webservice, par ex. :
    // private static final String BASE_URL = "http://localhost:8080/E-Wallet_WebService/api";

    public String login(String email, String password) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("http://localhost:8080/webservice/api/users/login");

            String payload = """
            {
                "email": "%s",
                "password": "%s"
            }
            """.formatted(email, password);

            Response response = target
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.json(payload));

            if (response.getStatus() != 200) {
                return null;
            }

            String json = response.readEntity(String.class);
            var obj = Json.createReader(new java.io.StringReader(json)).readObject();

            // Try different possible field names to avoid NullPointerException
            String userId = obj.getString("userId", null);
            if (userId == null) userId = obj.getString("userID", null);
            if (userId == null && obj.containsKey("id")) {
                userId = obj.get("id").toString();
            }

            return userId;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean registerUser(String firstname, String lastname, String email, String password) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("http://localhost:8080/webservice/api/users/register");

            String payload = """
            {
                "firstName": "%s",
                "lastName": "%s",
                "email": "%s",
                "password": "%s"
            }
            """.formatted(firstname, lastname, email, password);

            Response response = target
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.json(payload));

            return response.getStatus() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<String> getAccountsForUser(String userId) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("http://localhost:8080/webservice/api/users/" + userId);

            Response res = target.request(MediaType.APPLICATION_JSON_TYPE).get();

            if (res.getStatus() != 200) {
                return java.util.List.of();
            }

            String json = res.readEntity(String.class);
            var obj = Json.createReader(new java.io.StringReader(json)).readObject();
            var accountsJson = obj.getJsonArray("accounts");

            java.util.List<String> out = new java.util.ArrayList<>();

            for (var a : accountsJson) {
                var acc = a.asJsonObject();
                out.add(acc.getString("accountID") + " - " + acc.getJsonNumber("balance").doubleValue() + " CHF");
            }

            return out;

        } catch (Exception e) {
            e.printStackTrace();
            return java.util.List.of();
        }
    }

    public boolean makeTransfer(String fromAccount, String toAccount, double amount) {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target("http://localhost:8080/webservice/api/transactions/transfer");

            String payload = """
            {
                "fromAccount": "%s",
                "toAccount": "%s",
                "amount": %s
            }
            """.formatted(fromAccount, toAccount, amount);

            Response response = target
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .post(Entity.json(payload));

            return response.getStatus() == 200;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
