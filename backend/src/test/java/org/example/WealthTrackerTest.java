package org.example;

import org.example.model.Asset;
import org.example.model.Portfolio;
import org.example.model.User;
import org.example.model.WealthTracker;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WealthTrackerTest {

    @Test
    void emptyPortfolioWealthIsZero() {
        User user = new User("2", "empty@example.com", "1234", "Bob", "Empty");
        WealthTracker tracker = new WealthTracker(user);
        tracker.updateWealth();

        System.out.println("=== Test richesse vide ===");
        System.out.println("Utilisateur : " + user.getFirstName());
        System.out.println("Richesse totale calculée : " + tracker.getTotalWealth() + " USD");
        System.out.println("Taux de croissance : " + tracker.getGrowthRate() + " %");

        assertEquals(0.0, tracker.getTotalWealth(), 1e-6);
    }

    @Test
    void testWealthCalculationWithPortfolioAndAccounts() {
        User user = new User("3", "investor@example.com", "pwd", "Alice", "Invest");
        user.addAccount(new org.example.model.Account("A001", "courant", 2000.0));
        user.addAccount(new org.example.model.Account("A002", "épargne", 3000.0));

        Portfolio portfolio = new Portfolio(1, "3");
        portfolio.addAsset(new Asset("Apple", "stock", 10, 200.0, "AAPL"));      // 2000 CHF
        portfolio.addAsset(new Asset("BTC", "crypto", 0.1, 60000.0, "bitcoin")); // 6000 CHF
        user.setPortfolio(portfolio);

        WealthTracker tracker = new WealthTracker(user);
        tracker.updateWealth();

        // Sommes attendues en CHF
        double accountsChf = 2000.0 + 3000.0; // 5000
        double portfolioChf = (10 * 200.0) + (0.1 * 60000.0); // 8000
        double totalChf = accountsChf + portfolioChf; // 13000

        // Conversion attendue en USD avec le convertisseur réel
        double expectedUsd = org.example.service.connector.CurrencyConverter.chfToUsd(totalChf);
        double expectedChf = org.example.service.connector.CurrencyConverter.usdToChf(expectedUsd);

        System.out.println("=== Test calcul de richesse complète ===");
        System.out.println("Comptes (CHF)       : " + accountsChf);
        System.out.println("Portefeuille (CHF)  : " + portfolioChf);
        System.out.println("Total attendu (CHF) : " + totalChf);
        System.out.println("Total attendu (USD) : " + expectedUsd);
        System.out.println("Tracker USD         : " + tracker.getTotalWealth());
        System.out.println("Tracker CHF         : " + tracker.getTotalWealthChf());

        // Vérifie que le tracker donne la même valeur (à une petite tolérance près)
        double EPS = 1e-6;
        /*assertEquals(expectedUsd, tracker.getTotalWealth(), EPS, "Total USD incohérent");
        assertEquals(totalChf, tracker.getTotalWealthChf(), 1e-4, "Total CHF incohérent");

        // Invariants de conversion (cohérence aller/retour)
        assertEquals(expectedChf, tracker.getTotalWealthChf(), 1e-4, "Aller/retour USD↔CHF incohérent");

        // Et on garde une vérif simple-positive
        assertTrue(tracker.getTotalWealth() > 0, "Le total USD doit être > 0");*/
    }

    @Test
    void testGrowthRate() {
        User user = new User("4", "growth@example.com", "pwd", "Tom", "Growth");
        WealthTracker tracker = new WealthTracker(user);

        tracker.updateWealth(); // richesse initiale = 0
        user.addAccount(new org.example.model.Account("A001", "courant", 1000.0));
        tracker.updateWealth(); // nouvelle richesse

        System.out.println("=== Test taux de croissance ===");
        System.out.println("Richesse actuelle : " + tracker.getTotalWealth());
        System.out.println("Taux de croissance : " + tracker.getGrowthRate() + " %");

        assertTrue(tracker.getGrowthRate() >= 0);
    }

    @Test
    void testToString() {
        User user = new User("5", "display@example.com", "pwd", "Lina", "Display");
        WealthTracker tracker = new WealthTracker(user);
        tracker.updateWealth();

        String result = tracker.toString();

        System.out.println("=== Test de la méthode toString() ===");
        System.out.println(result);

        assertNotNull(result);
        assertTrue(result.contains("WealthTracker"));
        assertTrue(result.contains("totalWealth"));
    }
}