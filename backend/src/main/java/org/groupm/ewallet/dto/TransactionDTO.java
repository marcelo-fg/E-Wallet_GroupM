package org.groupm.ewallet.dto;

import java.time.LocalDateTime;

/**
 * DTO pour transporter les transactions.
 */
public class TransactionDTO {
    private String transactionID;
    private String type;
    private double amount;
    private LocalDateTime timestamp;
    private String description;
    private String accountID;

    // Getters/setters

    public String getTransactionID() { return transactionID; }
    public void setTransactionID(String transactionID) { this.transactionID = transactionID; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAccountID() { return accountID; }
    public void setAccountID(String accountID) { this.accountID = accountID; }
}
