package org.example.model;

import jakarta.json.bind.annotation.JsonbTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un compte bancaire avec ses informations essentielles.
 * Contient l'identifiant, le type, le solde, l'utilisateur associé et les transactions.
 */
public class Account {

    /** Identifiant unique du compte. */
    private String accountID;

    /** Type du compte (exemple : "épargne", "courant"). */
    private String type;

    /** Solde actuel du compte. */
    private double balance;

    /** Utilisateur propriétaire du compte. */
    private User user;

    /** Liste des transactions liées à ce compte. */
    private List<Transaction> transactions;

    /**
     * Constructeur par défaut nécessaire pour la désérialisation JSON.
     * Initialise la liste des transactions.
     */
    public Account() {
        this.transactions = new ArrayList<>();
    }

    /**
     * Constructeur avec paramètres.
     *
     * @param accountID Identifiant unique du compte.
     * @param type Type du compte bancaire.
     * @param balance Solde initial du compte.
     */
    public Account(String accountID, String type, double balance) {
        this.accountID = accountID;
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

    @JsonbTransient
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    // ===================== Méthodes principales =====================

    /**
     * Effectue un dépôt sur le compte.
     *
     * @param amount Montant à déposer, doit être strictement positif.
     */
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            addTransaction("deposit", amount, "Dépôt sur le compte");
        } else {
            System.out.println("Le montant du dépôt doit être positif.");
        }
    }

    /**
     * Effectue un retrait depuis le compte.
     *
     * @param amount Montant à retirer, doit être positif et inférieur ou égal au solde.
     */
    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            addTransaction("withdraw", amount, "Retrait du compte");
        } else {
            System.out.println("Montant invalide ou solde insuffisant.");
        }
    }

    /**
     * Ajoute une transaction à la liste des transactions du compte.
     *
     * @param type Type de la transaction (exemple : "deposit", "withdraw").
     * @param amount Montant impliqué dans la transaction.
     * @param description Description de la transaction.
     */
    private void addTransaction(String type, double amount, String description) {
        String id = "TXN-" + (transactions.size() + 1);
        Transaction txn = new Transaction(id, type, amount, description);
        transactions.add(txn);
    }

    /**
     * Affiche l'historique complet des transactions du compte.
     * Si aucune transaction n'est enregistrée, affiche un message approprié.
     */
    public void printTransactionHistory() {
        if (transactions.isEmpty()) {
            System.out.println("Aucune transaction enregistrée pour ce compte.");
            return;
        }
        for (Transaction transaction : transactions) {
            System.out.println(transaction);
        }
    }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + accountID + '\'' +
                ", type='" + type + '\'' +
                ", balance=" + balance +
                ", owner=" + (user != null ? user.getFirstName() + " " + user.getLastName() : "Aucun") +
                '}';
    }
}