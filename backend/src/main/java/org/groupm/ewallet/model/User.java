package org.groupm.ewallet.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un utilisateur du système, incluant ses informations
 * personnelles, ses comptes bancaires et ses portefeuilles d’investissement.
 */
@Entity
@Table(name = "users")
public class User {

    /** Identifiant unique de l'utilisateur. */
    @Id
    @Column(name = "user_id")
    private String userID;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    /** Solde total cumulé de tous les comptes de l’utilisateur. */
    @Transient // Calculé, pas stocké
    private double totalBalance;

    /** Liste des comptes bancaires associés à l'utilisateur. */
    /** Liste des comptes bancaires associés à l'utilisateur. */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Account> accounts;

    /** Liste des portefeuilles d'investissement associés à l'utilisateur. */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id") // Clé étrangère dans la table portfolio
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

    public String getUserID() {
        return userID;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Portfolio> getPortfolios() {
        return portfolios;
    }

    public double getTotalBalance() {
        // Recalculer si nécessaire car @Transient
        return initiateTotalBalance();
    }

    // ===================== Setters =====================

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = (accounts != null) ? accounts : new ArrayList<>();
        this.totalBalance = initiateTotalBalance();
    }

    public void setPortfolios(List<Portfolio> portfolios) {
        this.portfolios = (portfolios != null) ? portfolios : new ArrayList<>();
    }

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

    public void addAccount(Account account) {
        if (account != null) {
            accounts.add(account);
            this.totalBalance = initiateTotalBalance();
        }
    }

    public void addPortfolio(Portfolio portfolio) {
        if (portfolio != null) {
            portfolios.add(portfolio);
        }
    }

    public double initiateTotalBalance() {
        double total = 0.0;
        if (accounts != null) {
            for (Account account : accounts) {
                total += account.getBalance();
            }
        }
        return total;
    }

    @Override
    public String toString() {
        return "User: " + firstName + " " + lastName + " (" + email + ")";
    }
}
