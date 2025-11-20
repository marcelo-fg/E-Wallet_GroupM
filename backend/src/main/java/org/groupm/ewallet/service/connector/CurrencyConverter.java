package org.groupm.ewallet.service.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Service de conversion bidirectionnelle USD ⇄ CHF.
 * Utilise l'API publique Frankfurter.app avec un mécanisme de cache d'une durée d'une heure
 * afin de limiter les appels réseau et d'améliorer les performances.
 */
public class CurrencyConverter {

    /** Client HTTP utilisé pour effectuer les appels à l'API. */
    private static final HttpClient http = HttpClient.newHttpClient();

    /** Outil de parsing JSON pour lire la réponse de l'API. */
    private static final ObjectMapper mapper = new ObjectMapper();

    /** Taux de change USD → CHF mis en cache. */
    private static double cachedUsdToChf = -1.0;

    /** Taux de change CHF → USD mis en cache. */
    private static double cachedChfToUsd = -1.0;

    /** Heure du dernier rafraîchissement du cache (en millisecondes). */
    private static long lastFetchTime = 0;

    /** Durée de validité du cache (1 heure). */
    private static final long CACHE_DURATION_MS = 3600000;

    /**
     * Met à jour les taux de conversion USD→CHF et CHF→USD si le cache est expiré.
     * Les taux sont récupérés via l'API Frankfurter.app.
     */
    private static void updateRatesIfNeeded() {
        long now = System.currentTimeMillis();

        // Si le cache est encore valide, on évite un nouvel appel à l’API.
        if (cachedUsdToChf > 0 && cachedChfToUsd > 0 && (now - lastFetchTime) < CACHE_DURATION_MS) {
            return;
        }

        try {
            String url = "https://api.frankfurter.app/latest?from=USD&to=CHF";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());
            double rateUsdToChf = root.path("rates").path("CHF").asDouble();

            if (rateUsdToChf > 0) {
                cachedUsdToChf = rateUsdToChf;
                cachedChfToUsd = 1.0 / rateUsdToChf;
                lastFetchTime = now;
                System.out.println("Taux USD→CHF mis à jour via Frankfurter : " + cachedUsdToChf);
            } else {
                System.err.println("Taux USD→CHF introuvable. Utilisation du cache existant.");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du taux via Frankfurter : " + e.getMessage());
        }
    }

    /**
     * Convertit un montant en USD vers CHF.
     *
     * @param usdAmount montant en USD
     * @return montant converti en CHF
     */
    public static double usdToChf(double usdAmount) {
        updateRatesIfNeeded();
        return usdAmount * cachedUsdToChf;
    }

    /**
     * Convertit un montant en CHF vers USD.
     *
     * @param chfAmount montant en CHF
     * @return montant converti en USD
     */
    public static double chfToUsd(double chfAmount) {
        updateRatesIfNeeded();
        return chfAmount * cachedChfToUsd;
    }

    /**
     * Affiche les taux de conversion actuellement utilisés en mémoire.
     * Cette méthode peut être utilisée pour le débogage.
     */
    public static void printCurrentRates() {
        updateRatesIfNeeded();
        System.out.println("Taux actuels : 1 USD = " + cachedUsdToChf + " CHF | 1 CHF = " + cachedChfToUsd + " USD");
    }
}