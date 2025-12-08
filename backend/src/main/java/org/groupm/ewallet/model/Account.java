package org.groupm.ewallet.model;

import jakarta.persistence.*;
import jakarta.json.bind.annotation.JsonbTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Modèle représentant un compte bancaire utilisateur.
 */
@Entity
@Table(name = "accounts")
public class Account {

    /** Identifiant unique du compte. */
    @Id
    @Column(name = "account_id")
    private String accountID;

    /** Identifiant de l'utilisateur propriétaire du compte. */
    /** Identifiant de l'utilisateur propriétaire du compte. */
    @Column(name = "user_id")
    private String userID;

    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /** Type du compte (ex : "épargne", "courant"). */
    private String type;

    /** Solde actuel du compte. */
    private double balance;

    /** Nom personnalisé du compte. */
    private String name;

    /** Liste des transactions associées à ce compte. */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private List<Transaction> transactions;

    // ===================== Constructeurs =====================

    public Account() {
        this.transactions = new ArrayList<>();
    }

    public Account(String accountID, String type, double balance) {
        this.accountID = accountID;
        this.type = type;
        this.balance = balance;
        this.transactions = new ArrayList<>();
    }

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void addTransaction(Transaction transaction) {
        if (this.transactions == null) {
            this.transactions = new ArrayList<>();
        }
        this.transactions.add(transaction);
    }

    @Override
    public String toString() {
        return "Account{" + "accountID='" + accountID + '\'' + ", balance=" + balance + '}';
    }
}
