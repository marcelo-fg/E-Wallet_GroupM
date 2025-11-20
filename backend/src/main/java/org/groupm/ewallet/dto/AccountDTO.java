package org.groupm.ewallet.dto;

import java.util.List;

/**
 * DTO pour transporter les données d’un compte bancaire entre le backend et l’API.
 */
public class AccountDTO {
    private String accountID;
    private String type;
    private double balance;
    private List<TransactionDTO> transactions;

    // Getters et setters

    public String getAccountID() { return accountID; }
    public void setAccountID(String accountID) { this.accountID = accountID; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public List<TransactionDTO> getTransactions() { return transactions; }
    public void setTransactions(List<TransactionDTO> transactions) { this.transactions = transactions; }
}
