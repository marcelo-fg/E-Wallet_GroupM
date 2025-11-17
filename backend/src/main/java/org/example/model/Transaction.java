package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Représente une transaction financière effectuée sur un compte.
 * Une transaction peut correspondre à un dépôt, un retrait ou un transfert,
 * et contient des informations sur le montant, la date et une description facultative.
 */
public class Transaction {

    /** Identifiant unique de la transaction (exemple : "TXN001"). */
    private String transactionID;

    /** Type de transaction (exemple : "deposit", "withdraw", "transfer"). */
    private String type;

    /** Montant concerné par la transaction. */
    private double amount;

    /** Date et heure exactes de la transaction. */
    private LocalDateTime timestamp;

    /** Description optionnelle de la transaction (exemple : "Virement salaire", "Achat crypto"). */
    private String description;

    /** Identifiant du compte associé à la transaction (facultatif, mais utile pour le suivi). */
    private String accountID;

    /**
     * Constructeur principal de la classe Transaction.
     *
     * @param transactionID identifiant unique de la transaction
     * @param type type de transaction ("deposit", "withdraw", "transfer")
     * @param amount montant concerné
     * @param description description optionnelle de la transaction
     */
    public Transaction(String transactionID, String type, double amount, String description) {
        this.transactionID = transactionID;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    /**
     * Constructeur alternatif permettant d’associer un compte à la transaction.
     *
     * @param transactionID identifiant unique de la transaction
     * @param type type de transaction ("deposit", "withdraw", "transfer")
     * @param amount montant concerné
     * @param description description optionnelle
     * @param accountID identifiant du compte concerné
     */
    public Transaction(String transactionID, String type, double amount, String description, String accountID) {
        this(transactionID, type, amount, description);
        this.accountID = accountID;
    }

    // ===================== Getters =====================

    public String getTransactionID() {
        return transactionID;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public String getAccountID() {
        return accountID;
    }

    // ===================== Méthodes utilitaires =====================

    /**
     * Retourne la date et l’heure de la transaction au format lisible.
     *
     * @return date et heure de la transaction formatées
     */
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    /**
     * Retourne une représentation textuelle complète de la transaction.
     *
     * @return une chaîne de caractères décrivant la transaction
     */
    @Override
    public String toString() {
        return "[" + getFormattedTimestamp() + "] "
                + type.toUpperCase()
                + " | Montant: " + amount + " CHF"
                + (accountID != null ? " | Compte: " + accountID : "")
                + (description != null && !description.isEmpty() ? " | " + description : "");
    }
}