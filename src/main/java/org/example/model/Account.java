package org.example.model;

/**
 * La classe Account représente un compte bancaire simple.
 * Elle contient des informations sur le compte comme son identifiant,
 * son type, et son solde, et propose des méthodes pour manipuler ce solde.
 */
public class Account {
    /**
     * Identifiant unique du compte.
     * C'est une chaîne de caractères qui permet d'identifier ce compte de manière unique.
     */
    private String accountID;

    /**
     * Type du compte (par exemple : "épargne", "courant", etc.).
     * Cela permet de différencier les différents types de comptes bancaires.
     */
    private String type;

    /**
     * Solde du compte.
     * Représente la quantité d'argent disponible sur ce compte.
     */
    private double balance;

    /**
     * Constructeur de la classe Account.
     * Il permet de créer un nouveau compte en initialisant l'identifiant,
     * le type et le solde initial du compte.
     *
     * @param accountID Identifiant unique du compte.
     * @param type Type du compte bancaire.
     * @param balance Solde initial du compte.
     */
    public Account(String accountID, String type, double balance) {
        this.accountID = accountID;
        this.type = type;
        this.balance = balance;
    }

    /**
     * Méthode pour obtenir le solde actuel du compte.
     * Cette méthode est utile pour connaître combien d'argent est disponible.
     *
     * @return Le solde actuel du compte.
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Méthode pour déposer de l'argent sur le compte.
     * Le montant doit être positif pour être ajouté au solde.
     *
     * @param amount Montant à déposer sur le compte.
     */
    public void deposit(double amount) {
        if (amount > 0) balance += amount;
    }

    /**
     * Méthode pour retirer de l'argent du compte.
     * Le montant doit être positif et ne pas dépasser le solde disponible.
     *
     * @param amount Montant à retirer du compte.
     */
    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) balance -= amount;
    }

    /**
     * Méthode qui retourne une représentation textuelle du compte.
     * Elle affiche l'identifiant, le type et le solde du compte.
     *
     * @return Une chaîne de caractères décrivant le compte.
     */
    @Override
    public String toString() {
        return "Account{" +
                "id='" + accountID + '\'' +
                ", type='" + type + '\'' +
                ", balance=" + balance +
                '}';
    }
}