// src/main/java/org/example/Main.java
package org.example;

import org.example.model.*;
import org.example.service.*;
import org.example.service.connector.*;

import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) {

        // --- Création de l'utilisateur ---
        UserManager userManager = new UserManager();
        User marcelo = userManager.registerUser("U001", "marcelo@example.com", "1234", "Marcelo", "Gonçalves");

        // --- Import des comptes Revolut mockés ---
        AccountManager accountManager = new AccountManager();
        accountManager.importRevolutMockToUser(marcelo);
        accountManager.listUserAccounts(marcelo);

        // --- Transactions de test ---
        Account firstAccount = marcelo.getAccounts().get(0);
        firstAccount.deposit(500);
        firstAccount.withdraw(200);
        firstAccount.printTransactionHistory();

        // --- Portefeuille avec symboles ---
        Portfolio portfolio = marcelo.getPortfolio();
        portfolio.addAsset(new Asset("Apple", "stock", 5, 185.50, "AAPL"));       // AAPL coté USD
        portfolio.addAsset(new Asset("Bitcoin", "crypto", 0.05, 65000.00, "bitcoin")); // CoinGecko id
        portfolio.addAsset(new Asset("ETF S&P500", "etf", 10, 450.00, "SPY"));   // ETF SPY

        System.out.println("\n=== Portefeuille AVANT mise à jour marché ===");
        System.out.println(portfolio);

        // --- WealthTracker ---
        WealthTracker tracker = new WealthTracker(marcelo);
        tracker.updateWealth();
        System.out.println("\nWealth (initial): " + tracker.getTotalWealth() + " USD");

        // --- Connecteur marché + service ---
        MarketDataConnector connector = new DefaultMarketDataConnector();
        MarketDataService marketService = new MarketDataService(connector);

        // 🔄 Maj immédiate
        marketService.refreshPortfolioPricesUsd(portfolio);
        tracker.updateWealth();
        System.out.println("\n=== Portefeuille APRÈS mise à jour marché (1 tir) ===");
        System.out.println(portfolio);
        System.out.println("Wealth (après maj): " + tracker.getTotalWealth() + " USD");

        // ⏱️ Maj toutes les 60s (demo)
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override public void run() {
                marketService.refreshPortfolioPricesUsd(portfolio);
                tracker.updateWealth();
                System.out.println("\n⏱️ Mise à jour périodique — Wealth: " + tracker.getTotalWealth() + " USD");
            }
        }, 60_000, 60_000);

        // Garde l’appli vivante un peu pour voir 2–3 ticks (facultatif)
        try { Thread.sleep(190_000); } catch (InterruptedException ignored) {}
    }
}