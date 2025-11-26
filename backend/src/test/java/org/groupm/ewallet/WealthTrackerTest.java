package org.groupm.ewallet;

import org.groupm.ewallet.model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WealthTrackerTest {

    @Test
    void emptyPortfolioWealthIsZero() {

        User user = new User("2", "empty@example.com", "1234", "Bob", "Test");

        WealthTracker tracker = new WealthTracker(user);
        tracker.updateWealth();

        System.out.println("=== Test richesse vide ===");
        System.out.println("Utilisateur : " + user.getFirstName());
        System.out.println("Richesse totale calculée : " + tracker.getTotalWealthUsd() + " USD");
        System.out.println("Taux de croissance : " + tracker.getGrowthRate() + " %");

        assertEquals(0.0, tracker.getTotalWealthUsd(), 1e-6);
    }

    @Test
    void testWealthCalculationWithPortfolioAndAccounts() {

        User user = new User("3", "investor@example.com", "pwd", "Alice", "Test");

        // === Comptes ===
        user.addAccount(new Account("A001", "courant", 2000.0));
        user.addAccount(new Account("A002", "épargne", 3000.0));

        // === Portefeuille ===
        Portfolio portfolio = new Portfolio(user.getUserID());
        portfolio.addAsset(new Asset("AAPL", "stock", "Apple", 200.0));
        portfolio.getAssets().get(0).setQuantity(10);

        portfolio.addAsset(new Asset("BTC", "crypto", "Bitcoin", 60000.0));
        portfolio.getAssets().get(1).setQuantity(0.1);

        user.addPortfolio(portfolio);

        WealthTracker tracker = new WealthTracker(user);
        tracker.updateWealth();

        System.out.println("=== Test richesse complète ===");
        System.out.println("Richesse USD : " + tracker.getTotalWealthUsd());
        System.out.println("Richesse CHF : " + tracker.getTotalWealthChf());
        System.out.println("Croissance : " + tracker.getGrowthRate() + "%");

        // Pas de valeur fixe attendue, on vérifie juste que le calcul ne renvoie pas zéro
        assertEquals(false, tracker.getTotalWealthUsd() == 0);
    }
}