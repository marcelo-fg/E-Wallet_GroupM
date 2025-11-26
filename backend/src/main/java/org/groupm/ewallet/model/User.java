package org.groupm.ewallet.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un utilisateur du système, incluant ses informations
 * personnelles, ses comptes bancaires et ses portefeuilles d’investissement.
 */
public class User {

    /** Solde total cumulé de tous les comptes de l’utilisateur. */
    private double totalBalance;

    /** Identifiant unique de l'utilisateur. */
    private String userID;

    private String email;
    private String password;
    private String firstName;
    private String lastName;

    /** Liste des comptes bancaires associés à l'utilisateur. */
    private List<Account> accounts;

    /** Liste des portefeuilles d'investissement associés à l'utilisateur. */
    private List<Portfolio> portfolios;

    // ===================== Constructeurs =====================

    public User() {
        this.accounts = new ArrayList<>();
        this.portfolios = new ArrayList<>();
        this.totalBalance = 0.0;
    }

    public User(String userID, String email, String password,
                String firstName, String lastName) {
        this.userID = userID;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accounts = new ArrayList<>();
        this.portfolios = new ArrayList<>();
        this.totalBalance = 0.0;
    }

    // ===================== Getters =====================

    public String getUserID() { return userID; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public List<Account> getAccounts() { return accounts; }
    public List<Portfolio> getPortfolios() { return portfolios; }
    public double getTotalBalance() { return totalBalance; }

    // ===================== Setters =====================

    public void setUserID(String userID) { this.userID = userID; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    /**
     * Remplace la liste complète des comptes.
     * Recalcule automatiquement le solde total.
     */
    public void setAccounts(List<Account> accounts) {
        this.accounts = (accounts != null) ? accounts : new ArrayList<>();
        this.totalBalance = initiateTotalBalance();
    }

    /**
     * Remplace la liste complète des portefeuilles.
     */
    public void setPortfolios(List<Portfolio> portfolios) {
        this.portfolios = (portfolios != null) ? portfolios : new ArrayList<>();
    }

    /**
     * Définit un unique portefeuille pour l'utilisateur.
     * Utile si le système impose une relation 1-to-1.
     */
    public void setPortfolio(Portfolio portfolio) {
        if (this.portfolios == null) {
            this.portfolios = new ArrayList<>();
        }
        this.portfolios.clear();
        if (portfolio != null) {
            this.portfolios.add(portfolio);
        }
    }

    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }

    // ===================== Méthodes principales =====================

    /**
     * Ajoute un compte bancaire à l'utilisateur
     * et met à jour le solde total.
     */
    public void addAccount(Account account) {
        if (account != null) {
            accounts.add(account);
            this.totalBalance = initiateTotalBalance();
        }
    }

    /**
     * Ajoute un portefeuille à la liste de l'utilisateur.
     */
    public void addPortfolio(Portfolio portfolio) {
        if (portfolio != null) {
            portfolios.add(portfolio);
        }
    }

    /**
     * Recalcule le total des soldes de tous les comptes de l’utilisateur.
     */
    public double initiateTotalBalance() {
        double total = 0.0;
        for (Account account : accounts) {
            total += account.getBalance();
        }
        return total;
    }

    @Override
    public String toString() {
        return "User: " + firstName + " " + lastName + " (" + email + ")";
    }
}