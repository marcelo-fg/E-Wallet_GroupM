package org.groupm.ewallet.model;

import jakarta.persistence.*;
import jakarta.json.bind.annotation.JsonbTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Modèle représentant un compte bancaire utilisateur.
 * Utilise BigDecimal pour les montants financiers afin de garantir la
 * précision.
 */
@Entity
@Table(name = "accounts")
public class Account implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identifiant unique du compte. */
    @Id
    @Column(name = "account_id")
    private String accountID;

    /** Identifiant de l'utilisateur propriétaire du compte. */
    @Column(name = "user_id")
    private String userID;

    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /** Type du compte (ex : "épargne", "courant"). */
    @Column(nullable = false)
    private String type;

    /** Solde actuel du compte - BigDecimal pour précision financière. */
    @Column(precision = 19, scale = 4)
    private BigDecimal balance = BigDecimal.ZERO;

    /** Nom personnalisé du compte. */
    private String name;

    /** Version pour optimistic locking - détection des conflits concurrents. */
    @Version
    private Long version;

    /** Liste des transactions associées à ce compte. */
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Transaction> transactions;

    // ===================== Constructeurs =====================

    public Account() {
        this.transactions = new ArrayList<>();
        this.balance = BigDecimal.ZERO;
    }

    public Account(String accountID, String type, double balance) {
        this.accountID = accountID;
        this.type = type;
        this.balance = BigDecimal.valueOf(balance);
        this.transactions = new ArrayList<>();
    }

    public Account(String accountID, String type, BigDecimal balance) {
        this.accountID = accountID;
        this.type = type;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
        this.transactions = new ArrayList<>();
    }

    public Account(String accountID, String userID, String type, double balance) {
        this.accountID = accountID;
        this.userID = userID;
        this.type = type;
        this.balance = BigDecimal.valueOf(balance);
        this.transactions = new ArrayList<>();
    }

    public Account(String accountID, String userID, String type, BigDecimal balance) {
        this.accountID = accountID;
        this.userID = userID;
        this.type = type;
        this.balance = balance != null ? balance : BigDecimal.ZERO;
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

    /**
     * Retourne le solde en BigDecimal (méthode principale).
     */
    public BigDecimal getBalanceAsBigDecimal() {
        return balance != null ? balance : BigDecimal.ZERO;
    }

    /**
     * Retourne le solde en double pour rétrocompatibilité.
     * 
     * @deprecated Utiliser getBalanceAsBigDecimal() pour précision financière.
     */
    @Deprecated
    public double getBalance() {
        return balance != null ? balance.doubleValue() : 0.0;
    }

    /**
     * Définit le solde avec un BigDecimal (méthode recommandée).
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance != null ? balance : BigDecimal.ZERO;
    }

    /**
     * Définit le solde avec un double pour rétrocompatibilité.
     * 
     * @deprecated Utiliser setBalance(BigDecimal) pour précision financière.
     */
    @Deprecated
    public void setBalance(double balance) {
        this.balance = BigDecimal.valueOf(balance);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getVersion() {
        return version;
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
        transaction.setAccount(this); // Maintain bidirectional relationship
    }

    @Override
    public String toString() {
        return "Account{" + "accountID='" + accountID + '\'' + ", balance=" + balance + '}';
    }
}
