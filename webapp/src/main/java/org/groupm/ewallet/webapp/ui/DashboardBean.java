package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import jakarta.annotation.PostConstruct;
import org.groupm.ewallet.webapp.model.LocalAccount;
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

    private List<String> accounts; // comptes bancaires
    private double totalWealth; // richesse totale
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

        public String getDate() {
            return date;
        }

        public String getType() {
            return type;
        }

        public double getAmount() {
            return amount;
        }
    }

    @Inject
    private WebAppService webAppService;

    @PostConstruct
    public void init() {
        loadDashboard();
    }

    public void loadDashboard() {
        var context = jakarta.faces.context.FacesContext.getCurrentInstance();
        if (context == null)
            return;

        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        if (session != null) {
            this.userId = (String) session.getAttribute("userId");
            this.userEmail = (String) session.getAttribute("userEmail");
        }

        if (userEmail != null) {
            this.username = userEmail;
        }

        if (userId != null) {
            List<LocalAccount> userAccounts = webAppService.getAccounts(userId);
            this.accounts = new java.util.ArrayList<>();
            for (LocalAccount acc : userAccounts) {
                this.accounts.add(acc.getId() + " - " + acc.getBalance() + " CHF");
            }

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
}
