package org.example.service.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Service de conversion bidirectionnelle USD ⇄ CHF
 * Utilise l'API publique Frankfurter.app, avec cache de 1h.
 */
public class CurrencyConverter {

    private static final HttpClient http = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static double cachedUsdToChf = -1.0;
    private static double cachedChfToUsd = -1.0;
    private static long lastFetchTime = 0;
    private static final long CACHE_DURATION_MS = 3600000; // 1 heure

    /** 🔄 Récupère et met à jour les taux USD→CHF et CHF→USD (cache 1h) */
    private static void updateRatesIfNeeded() {
        long now = System.currentTimeMillis();
        if (cachedUsdToChf > 0 && cachedChfToUsd > 0 && (now - lastFetchTime) < CACHE_DURATION_MS) return;

        try {
            String url = "https://api.frankfurter.app/latest?from=USD&to=CHF";
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(res.body());
            double rateUsdToChf = root.path("rates").path("CHF").asDouble();

            if (rateUsdToChf > 0) {
                cachedUsdToChf = rateUsdToChf;
                cachedChfToUsd = 1.0 / rateUsdToChf;
                lastFetchTime = now;
                System.out.println("💱 Taux USD→CHF mis à jour (Frankfurter) : " + cachedUsdToChf);
            } else {
                System.err.println("⚠️ Taux USD→CHF introuvable, utilisation du cache.");
            }

        } catch (Exception e) {
            System.err.println("⚠️ Erreur API Frankfurter : " + e.getMessage());
        }
    }

    /** 💰 Conversion USD → CHF */
    public static double usdToChf(double usdAmount) {
        updateRatesIfNeeded();
        return usdAmount * cachedUsdToChf;
    }

    /** 💵 Conversion CHF → USD */
    public static double chfToUsd(double chfAmount) {
        updateRatesIfNeeded();
        return chfAmount * cachedChfToUsd;
    }

    /** 💡 Pour debug : affiche le taux actuel */
    public static void printCurrentRates() {
        updateRatesIfNeeded();
        System.out.println("📊 Taux actuels : 1 USD = " + cachedUsdToChf + " CHF | 1 CHF = " + cachedChfToUsd + " USD");
    }
}