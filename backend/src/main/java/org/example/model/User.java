package org.example.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un utilisateur du système avec ses informations personnelles,
 * ses comptes bancaires et son portefeuille d’investissement.
 */
public class User {

    private double totalBalance;

    /** Identifiant unique de l’utilisateur. */
    private String userID;

    /** Adresse e-mail de l’utilisateur. */
    private String email;

    /** Mot de passe de l’utilisateur. */
    private String password;

    /** Prénom de l’utilisateur. */
    private String firstName;

    /** Nom de famille de l’utilisateur. */
    private String lastName;

    /** Liste des comptes bancaires liés à l’utilisateur. */
    private List<Account> accounts;

    /** Portefeuille d’investissement associé à l’utilisateur. */
    private Portfolio portfolio;

    /**
     * Constructeur par défaut requis pour la désérialisation JSON-B.
     * Initialise une liste vide de comptes et un nouveau portefeuille.
     */
    public User() {
        this.accounts = new ArrayList<>();
        this.portfolio = new Portfolio();
        //this.totalBalance = initiateTotalBalance();
    }

    /**
     * Constructeur complet pour initialiser un utilisateur avec ses informations personnelles.
     *
     * @param userID identifiant unique de l’utilisateur
     * @param email adresse e-mail de l’utilisateur
     * @param password mot de passe de l’utilisateur
     * @param firstName prénom de l’utilisateur
     * @param lastName nom de famille de l’utilisateur
     */
    public User(String userID, String email, String password, String firstName, String lastName) {
        this.userID = userID;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accounts = new ArrayList<>();
        this.portfolio = new Portfolio();
        this.totalBalance = initiateTotalBalance();
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

    public Portfolio getPortfolio() {
        return portfolio;
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
        this.accounts = accounts;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    // ===================== Méthodes principales =====================

    /**
     * Ajoute un compte à la liste des comptes de l’utilisateur.
     *
     * @param account compte à ajouter
     */
    public void addAccount(Account account) {
        accounts.add(account);
    }

    /**
     * Calcule le solde total de tous les comptes associés à l’utilisateur.
     *
     * @return solde total en CHF
     */
    public double initiateTotalBalance() {
        double total = 0;
        for (Account account : accounts) {
            total += account.getBalance();
        }
        return total;
    }
    public void setTotalBalance(double totalBalance) {
        this.totalBalance = totalBalance;
    }
    public double getTotalBalance(){
        return this.totalBalance;
    }
    /**
     * Retourne une représentation textuelle de l’utilisateur sous la forme :
     * "User: prénom nom (email)".
     *
     * @return description textuelle de l’utilisateur
     */
    @Override
    public String toString() {
        return "User: " + firstName + " " + lastName + " (" + email + ")";
    }
}