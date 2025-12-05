package org.groupm.ewallet.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Modèle représentant un compte bancaire utilisateur.
 * Ce modèle ne contient aucune logique métier.
 * Toute la gestion de dépôts, retraits et transactions est effectuée dans
 * AccountManager.
 */
public class Account {

    /** Identifiant unique du compte. */
    private String accountID;

    /** Identifiant de l'utilisateur propriétaire du compte. */
    private String userID;

    /** Type du compte (ex : "épargne", "courant"). */
    private String type;

    /** Nom personnalisé du compte (ex: "Mes économies"). */
    private String name;

    /** Solde actuel du compte. */
    private double balance;

    /** Liste des transactions associées à ce compte. */
    private List<Transaction> transactions;

    /**
     * Constructeur par défaut requis pour la désérialisation JSON-B/Jackson.
     */
    public Account() {
        this.transactions = new ArrayList<>();
    }

    /**
     * Constructeur avec paramètres principaux (sans userID).
     */
    public Account(String accountID, String type, double balance) {
        this.accountID = accountID;
        this.type = type;
        this.balance = balance;
        this.transactions = new ArrayList<>();
    }

    /**
     * Constructeur avec userID.
     */
    public Account(String accountID, String userID, String type, double balance) {
        this.accountID = accountID;
        this.userID = userID;
        this.type = type;
        this.balance = balance;
        this.transactions = new ArrayList<>();
    }

    // ===================== Getters et Setters =====================

    public String getAccountID() {
        return accountID;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    /**
     * Ajoute une transaction au modèle.
     * Cette méthode est invoquée depuis AccountManager uniquement.
     */
    public void addTransaction(Transaction transaction) {
        if (this.transactions == null) {
            this.transactions = new ArrayList<>();
        }
        this.transactions.add(transaction);
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountID='" + accountID + '\'' +
                ", userID='" + userID + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", balance=" + balance +
                '}';
    }
}