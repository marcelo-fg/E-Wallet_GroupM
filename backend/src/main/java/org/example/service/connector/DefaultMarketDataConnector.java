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
 * Connecteur de données de marché avec gestion d’un cache local
 * pour limiter les appels aux APIs externes.
 *
 * - CoinGecko est utilisé pour les cryptomonnaies.
 * - Alpha Vantage est utilisé pour les actions et les ETF.
 */
public class DefaultMarketDataConnector implements MarketDataConnector {

    /** Client HTTP utilisé pour effectuer les requêtes externes. */
    private final HttpClient http = HttpClient.newHttpClient();

    /** Outil de conversion JSON pour le traitement des réponses API. */
    private final ObjectMapper mapper = new ObjectMapper();

    /** Clé API d’Alpha Vantage récupérée depuis les variables d’environnement. */
    private final String alphaKey = System.getenv("ALPHA_VANTAGE_API_KEY");

    /** Cache local pour stocker les prix des actifs. */
    private static final Map<String, Double> priceCache = new HashMap<>();

    /** Cache local pour stocker les horodatages des valeurs mises en cache. */
    private static final Map<String, Long> timestampCache = new HashMap<>();

    /** Durée de validité du cache (2 minutes). */
    private static final long CACHE_DURATION_MS = 120_000;

    /**
     * Récupère le prix d'une cryptomonnaie en USD via l’API CoinGecko.
     * Si la valeur est déjà présente et valide dans le cache, celle-ci est utilisée.
     *
     * @param coingeckoId identifiant de la cryptomonnaie sur CoinGecko
     * @return prix actuel en USD
     * @throws Exception en cas d’erreur réseau ou de parsing JSON
     */
    @Override
    public double getCryptoPriceUsd(String coingeckoId) throws Exception {
        // Vérifie si la donnée est en cache
        if (isCached(coingeckoId)) return priceCache.get(coingeckoId);

        String url = "https://api.coingecko.com/api/v3/simple/price?ids=" + coingeckoId + "&vs_currencies=usd";
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode root = mapper.readTree(response.body());
        double price = root.path(coingeckoId).path("usd").asDouble();

        if (price > 0) cache(coingeckoId, price);
        return price;
    }

    /**
     * Récupère le prix d’une action ou d’un ETF en USD via l’API Alpha Vantage.
     * Si la valeur est déjà présente et valide dans le cache, celle-ci est utilisée.
     *
     * @param symbol symbole boursier (exemple : "AAPL", "SPY")
     * @return prix actuel en USD
     * @throws Exception en cas d’erreur réseau ou de clé API manquante
     */
    @Override
    public double getQuotePriceUsd(String symbol) throws Exception {
        // Vérifie si la donnée est en cache
        if (isCached(symbol)) return priceCache.get(symbol);

        if (alphaKey == null || alphaKey.isBlank()) {
            throw new IllegalStateException("La clé API ALPHA_VANTAGE_API_KEY n’est pas définie.");
        }

        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + alphaKey;
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        JsonNode priceNode = mapper.readTree(response.body()).path("Global Quote").path("05. price");

        if (priceNode.isMissingNode() || priceNode.asText().isBlank()) {
            throw new IllegalStateException("Prix indisponible pour le symbole : " + symbol + " (Alpha Vantage)");
        }

        double price = Double.parseDouble(priceNode.asText());
        cache(symbol, price);
        return price;
    }

    // ===================== Méthodes utilitaires du cache =====================

    /**
     * Vérifie si un symbole est présent dans le cache et si sa valeur est encore valide.
     *
     * @param symbol symbole à vérifier
     * @return true si le symbole est en cache et valide, sinon false
     */
    private boolean isCached(String symbol) {
        if (!priceCache.containsKey(symbol)) return false;
        long lastUpdate = timestampCache.getOrDefault(symbol, 0L);
        return Instant.now().toEpochMilli() - lastUpdate < CACHE_DURATION_MS;
    }

    /**
     * Ajoute ou met à jour un symbole dans le cache.
     *
     * @param symbol symbole de l’actif
     * @param price valeur à stocker en USD
     */
    private void cache(String symbol, double price) {
        priceCache.put(symbol, price);
        timestampCache.put(symbol, Instant.now().toEpochMilli());
        System.out.println("Mise en cache : " + symbol + " = " + price + " USD");
    }
}