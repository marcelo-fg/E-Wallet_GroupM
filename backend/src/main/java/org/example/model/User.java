package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class User {
    // Attributs représentant les informations personnelles et les comptes associés à l'utilisateur
    private String userID;               // Identifiant unique de l'utilisateur
    private String email;                // Adresse email de l'utilisateur
    private String password;             // Mot de passe de l'utilisateur
    private String firstName;            // Prénom de l'utilisateur
    private String lastName;             // Nom de famille de l'utilisateur
    private List<Account> accounts;      // Liste des comptes bancaires liés à cet utilisateur
    private Portfolio portfolio;        // Portefeuille d’investissement de l’utilisateur

    /**
     * Default constructor required for JSON-B deserialization.
     * Initializes accounts as empty list and portfolio as new Portfolio.
     */
    public User() {
        this.accounts = new ArrayList<>();
        this.portfolio = new Portfolio();
    }

    /**
     * Constructeur de la classe User.
     * Initialise un nouvel utilisateur avec ses informations personnelles et crée une liste vide de comptes.
     *
     * @param userID Identifiant unique de l'utilisateur
     * @param email Adresse email de l'utilisateur
     * @param password Mot de passe de l'utilisateur
     * @param firstName Prénom de l'utilisateur
     * @param lastName Nom de famille de l'utilisateur
     */
    public User(String userID, String email, String password, String firstName, String lastName) {
        this.userID = userID;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accounts = new ArrayList<>();
        this.portfolio = new Portfolio();
    }

    // Getters pour accéder aux informations privées de l'utilisateur

    /**
     * Retourne l'identifiant unique de l'utilisateur.
     * @return userID
     */
    public String getUserID() { return userID; }

    /**
     * Retourne l'adresse email de l'utilisateur.
     * @return email
     */
    public String getEmail() { return email; }

    /**
     * Retourne le mot de passe de l'utilisateur.
     * @return password
     */
    public String getPassword() { return password; }

    /**
     * Retourne le prénom de l'utilisateur.
     * @return firstName
     */
    public String getFirstName() { return firstName; }

    /**
     * Retourne le nom de famille de l'utilisateur.
     * @return lastName
     */
    public String getLastName() { return lastName; }

    /**
     * Retourne la liste des comptes associés à l'utilisateur.
     * @return accounts
     */
    public List<Account> getAccounts() { return accounts; }

    // Méthodes principales permettant la gestion des comptes et des informations utilisateur

    /**
     * Ajoute un compte à la liste des comptes de l'utilisateur.
     * @param account Le compte à ajouter
     */
    public void addAccount(Account account) {
        accounts.add(account);
    }

    /**
     * Calcule le solde total en additionnant les soldes de tous les comptes de l'utilisateur.
     * @return la somme des soldes de tous les comptes
     */
    public double getTotalBalance() {
        double total = 0;
        for (Account acc : accounts) {
            total += acc.getBalance();
        }
        return total;
    }

    public void setPortfolio(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public void  setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }



    public Portfolio getPortfolio() {
        return portfolio;
    }
    /**
     * Retourne une représentation textuelle de l'utilisateur sous la forme:
     * "User: prénom nom (email)"
     * @return chaîne descriptive de l'utilisateur
     */
    @Override
    public String toString() {
        return "User: " + firstName + " " + lastName + " (" + email + ")";
    }
}