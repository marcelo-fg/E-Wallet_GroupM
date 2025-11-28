package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.groupm.ewallet.webapp.service.WebAppService;

import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class DashboardBean implements Serializable {

    private String userId;
    private String userEmail;
    private String username = "Utilisateur";

    public String getUsername() {
        return username;
    }

    private List<String> accounts;   // comptes bancaires
    private double totalWealth;      // richesse totale
    private double percentageGrowth; // croissance du patrimoine

    private List<Transaction> recentTransactions;

    public static class Transaction {
        private String date;
        private String type;
        private double amount;

        public Transaction(String date, String type, double amount) {
            this.date = date;
            this.type = type;
            this.amount = amount;
        }

        public String getDate() { return date; }
        public String getType() { return type; }
        public double getAmount() { return amount; }
    }

    @Inject
    private WebAppService webAppService;

    public void loadDashboard() {
        var context = jakarta.faces.context.FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        if (session != null) {
            this.userId = (String) session.getAttribute("userId");
            this.userEmail = (String) session.getAttribute("userEmail");
        }

        if (userEmail != null) {
            this.username = userEmail;
        }

        if (userId != null) {
            this.accounts = webAppService.getAccountsForUser(userId);

            // Exemple de valeurs si pas encore connecté à une vraie logique métier
            this.totalWealth = webAppService.getTotalWealthForUser(userId);
            this.percentageGrowth = webAppService.getWealthGrowthForUser(userId);
        }
    }

    public String getWelcomeMessage() {
        return "Bienvenue, " + username + " !";
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public double getTotalWealth() {
        return totalWealth;
    }

    public double getPercentageGrowth() {
        return percentageGrowth;
    }

    // === Computed properties for dashboard.xhtml ===
    public double getTotalBalance() {
        return this.totalWealth;
    }

    public int getAccountCount() {
        return (accounts != null) ? accounts.size() : 0;
    }

    public double getPortfolioValue() {
        // Placeholder: portfolio value equals totalWealth for now
        return this.totalWealth;
    }

    public List<Transaction> getRecentTransactions() {
        if (recentTransactions == null) {
            recentTransactions = List.of(
                    new Transaction("2025-01-01", "Achat café", 4.50),
                    new Transaction("2025-01-02", "Transfert épargne", 200.00),
                    new Transaction("2025-01-03", "Retrait ATM", 100.00)
            );
        }
        return recentTransactions;
    }
}