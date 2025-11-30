package org.groupm.ewallet.service.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class DefaultMarketDataConnector implements MarketDataConnector {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String alphaKey = System.getenv("ALPHA_VANTAGE_API_KEY");

    // --- AJOUT : Mapping Symbole -> ID CoinGecko ---
    private static final Map<String, String> CRYPTO_ID_MAP = new HashMap<>();
    static {
        CRYPTO_ID_MAP.put("BTC", "bitcoin");
        CRYPTO_ID_MAP.put("ETH", "ethereum");
        CRYPTO_ID_MAP.put("SOL", "solana");
        CRYPTO_ID_MAP.put("XRP", "ripple");
        CRYPTO_ID_MAP.put("ADA", "cardano");
        CRYPTO_ID_MAP.put("DOGE", "dogecoin");
        CRYPTO_ID_MAP.put("DOT", "polkadot");
        // Ajoutez d'autres cryptos ici si nécessaire
    }

    // Cache local
    private static final Map<String, Double> priceCache = new HashMap<>();
    private static final Map<String, Long> timestampCache = new HashMap<>();
    private static final long CACHE_DURATION_MS = 120_000;

    public DefaultMarketDataConnector() {
        // Constructeur
    }

    @Override
    public double getCryptoPriceUsd(String symbolOrId) throws Exception {
        // 1. On traduit le symbole (ex: "BTC") en ID API (ex: "bitcoin")
        String apiId = CRYPTO_ID_MAP.getOrDefault(symbolOrId.toUpperCase(), symbolOrId.toLowerCase());

        // Vérifie si le PRIX est en cache (on utilise le symbole comme clé de cache pour simplifier)
        if (isCached(apiId)) return priceCache.get(apiId);

        String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + apiId + "&vs_currencies=usd";

        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(response.body());

            // On lit le noeud correspondant à l'apiId
            if (root.has(apiId)) {
                double price = root.path(apiId).path("usd").asDouble();
                if (price > 0) {
                    cache(apiId, price); // On cache avec l'ID API
                    return price;
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur CoinGecko pour " + apiId + ": " + e.getMessage());
        }

        return 0.0;
    }

    @Override
    public double getQuotePriceUsd(String symbol) throws Exception {
        // Vérifie le cache
        if (isCached(symbol)) return priceCache.get(symbol);

        if (alphaKey == null || alphaKey.isBlank()) {
            // Fallback pour éviter de planter si pas de clé : on retourne une valeur fictive ou 0
            System.err.println("ALPHA_VANTAGE_API_KEY manquant, impossible de récupérer le prix stock pour " + symbol);
            return 0.0;
        }

        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + alphaKey;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(response.body());
        JsonNode priceNode = root.path("Global Quote").path("05. price");

        if (priceNode != null && !priceNode.isMissingNode()) {
            double price = Double.parseDouble(priceNode.asText());
            cache(symbol, price);
            return price;
        }

        return 0.0;
    }

    private boolean isCached(String symbol) {
        if (!priceCache.containsKey(symbol)) return false;
        long lastUpdate = timestampCache.getOrDefault(symbol, 0L);
        return Instant.now().toEpochMilli() - lastUpdate < CACHE_DURATION_MS;
    }

    private void cache(String symbol, double price) {
        priceCache.put(symbol, price);
        timestampCache.put(symbol, Instant.now().toEpochMilli());
    }
}