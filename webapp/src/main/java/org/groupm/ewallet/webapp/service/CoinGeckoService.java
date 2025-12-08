package org.groupm.ewallet.webapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import org.groupm.ewallet.webapp.connector.ExternalAsset;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for CoinGecko API integration (cryptocurrency data).
 */
@ApplicationScoped
public class CoinGeckoService {

    private static final String API_URL = "https://api.coingecko.com/api/v3";

    /**
     * Loads a list of crypto assets from CoinGecko.
     */
    public List<ExternalAsset> loadCryptoAssets() {
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(API_URL + "/coins/markets?vs_currency=usd");

            String json = target.request(MediaType.APPLICATION_JSON).get(String.class);
            var arr = Json.createReader(new StringReader(json)).readArray();

            List<ExternalAsset> list = new ArrayList<>();

            for (var v : arr) {
                var o = v.asJsonObject();
                list.add(new ExternalAsset(
                        o.getString("name", ""),
                        o.getString("symbol", "").toUpperCase(),
                        o.getString("id", "")));
            }
            return list;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Requests the USD price of a crypto asset from CoinGecko.
     */
    public double getCryptoPrice(String apiId) {
        try {
            Client client = ClientBuilder.newClient();
            String url = API_URL + "/simple/price?ids=" + apiId + "&vs_currencies=usd";

            WebTarget target = client.target(url);
            String json = target.request(MediaType.APPLICATION_JSON).get(String.class);

            var obj = Json.createReader(new StringReader(json)).readObject();

            if (obj.containsKey(apiId)) {
                var priceObj = obj.getJsonObject(apiId);
                if (priceObj.containsKey("usd")) {
                    return priceObj.getJsonNumber("usd").doubleValue();
                }
            }

            return 0.0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}
