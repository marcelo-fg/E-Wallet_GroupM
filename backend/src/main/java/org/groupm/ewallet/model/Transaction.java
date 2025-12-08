package org.groupm.ewallet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Représente une transaction financière effectuée sur un compte.
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    /** Identifiant unique de la transaction (ex : "TXN001"). */
    @Id
    @Column(name = "transaction_id")
    private String transactionID;

    /** Type de transaction ("deposit", "withdraw", "transfer"). */
    private String type;

    /** Montant de la transaction. */
    private double amount;

    /** Date et heure exactes de la transaction. */
    private LocalDateTime timestamp;

    /** Description optionnelle. */
    private String description;

    /** Identifiant du compte associé. */
    @Column(name = "account_id", insertable = false, updatable = false)
    private String accountID;

    // ===================== Constructeurs =====================

    public Transaction() {
        this.timestamp = LocalDateTime.now();
    }

    public Transaction(String transactionID, String type, double amount, String description) {
        this.transactionID = transactionID;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

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

    // ===================== Setters =====================

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    // ===================== Méthodes utilitaires =====================

    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return timestamp != null ? timestamp.format(formatter) : "";
    }

    @Override
    public String toString() {
        return "[" + getFormattedTimestamp() + "] " + type.toUpperCase() + " | Montant: " + amount;
    }
}
