package org.example.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * La classe Transaction représente une opération financière effectuée sur un compte.
 * Elle peut être de type dépôt, retrait ou transfert et contient des informations
 * sur le montant, la date, et une description optionnelle.
 */
public class Transaction {

    /** Identifiant unique de la transaction (par ex : "TXN001"). */
    private String transactionID;

    /** Type de transaction (par ex : "deposit", "withdraw", "transfer"). */
    private String type;

    /** Montant impliqué dans la transaction. */
    private double amount;

    /** Date et heure exactes de la transaction. */
    private LocalDateTime timestamp;

    /** Description optionnelle de la transaction (ex : "Virement salaire", "Achat crypto"). */
    private String description;

    // ✅ Nouveau champ pour identifier le compte concerné (optionnel mais utile)
    private String accountID;

    /**
     * Constructeur de la classe Transaction.
     *
     * @param transactionID Identifiant unique
     * @param type Type de transaction ("deposit", "withdraw", "transfer")
     * @param amount Montant concerné
     * @param description Description optionnelle
     */
    public Transaction(String transactionID, String type, double amount, String description) {
        this.transactionID = transactionID;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.description = description;
    }

    // 🔹 Surcharge utile pour associer un compte à la transaction
    public Transaction(String transactionID, String type, double amount, String description, String accountID) {
        this(transactionID, type, amount, description);
        this.accountID = accountID;
    }

    // ------------------------- Getters -------------------------
    public String getTransactionID() { return transactionID; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDescription() { return description; }
    public String getAccountID() { return accountID; }

    // ------------------------- Méthodes utilitaires -------------------------

    /** Retourne la date/heure de la transaction sous forme lisible. */
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    /** Fournit une description textuelle complète de la transaction. */
    @Override
    public String toString() {
        return "[" + getFormattedTimestamp() + "] "
                + type.toUpperCase()
                + " | Montant: " + amount + " CHF"
                + (accountID != null ? " | Compte: " + accountID : "")
                + (description != null && !description.isEmpty() ? " | " + description : "");
    }
}