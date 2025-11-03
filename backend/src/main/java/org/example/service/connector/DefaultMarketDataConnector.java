package org.example.service.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Connecteur marché avec cache local pour limiter les appels Alpha Vantage.
 * - CoinGecko pour les cryptos
 * - Alpha Vantage pour actions/ETF
 */
public class DefaultMarketDataConnector implements MarketDataConnector {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String alphaKey = System.getenv("ALPHA_VANTAGE_API_KEY");

    // 🧠 Caches locaux
    private static final Map<String, Double> priceCache = new HashMap<>();
    private static final Map<String, Long> timestampCache = new HashMap<>();
    private static final long CACHE_DURATION_MS = 120_000; // 2 minutes

    @Override
    public double getCryptoPriceUsd(String coingeckoId) throws Exception {
        // Vérifie le cache
        if (isCached(coingeckoId)) return priceCache.get(coingeckoId);

        String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + coingeckoId + "&vs_currencies=usd";
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(res.body());
        double price = root.path(coingeckoId).path("usd").asDouble();

        if (price > 0) cache(coingeckoId, price);
        return price;
    }

    @Override
    public double getQuotePriceUsd(String symbol) throws Exception {
        // 🔍 Vérifie le cache
        if (isCached(symbol)) return priceCache.get(symbol);

        if (alphaKey == null || alphaKey.isBlank())
            throw new IllegalStateException("ALPHA_VANTAGE_API_KEY non défini");

        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + alphaKey;
        HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());

        JsonNode priceNode = mapper.readTree(res.body()).path("Global Quote").path("05. price");
        if (priceNode.isMissingNode() || priceNode.asText().isBlank())
            throw new IllegalStateException("Prix indisponible pour " + symbol + " (Alpha Vantage)");

        double price = Double.parseDouble(priceNode.asText());
        cache(symbol, price);
        return price;
    }

    // ----------------- Fonctions utilitaires du cache -----------------

    /** ✅ Vérifie si un symbole est en cache et toujours valide */
    private boolean isCached(String symbol) {
        if (!priceCache.containsKey(symbol)) return false;
        long last = timestampCache.getOrDefault(symbol, 0L);
        return Instant.now().toEpochMilli() - last < CACHE_DURATION_MS;
    }

    /** ✅ Ajoute/actualise un symbole dans le cache */
    private void cache(String symbol, double price) {
        priceCache.put(symbol, price);
        timestampCache.put(symbol, Instant.now().toEpochMilli());
        System.out.println("🗃️  Mise en cache de " + symbol + " : " + price + " USD");
    }
}