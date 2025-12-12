package org.groupm.ewallet.model;

import jakarta.persistence.*;
import jakarta.json.bind.annotation.JsonbTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Représente une transaction financière effectuée sur un compte.
 * Utilise BigDecimal pour les montants afin de garantir la précision
 * financière.
 */
@Entity
@Table(name = "transactions")
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identifiant unique de la transaction (ex : "TXN001"). */
    @Id
    @Column(name = "transaction_id")
    private String transactionID;

    /** Type de transaction ("deposit", "withdraw", "transfer"). */
    @Column(nullable = false)
    private String type;

    /** Montant de la transaction - BigDecimal pour précision financière. */
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount = BigDecimal.ZERO;

    /** Date et heure exactes de la transaction. */
    private LocalDateTime timestamp;

    /** Description optionnelle. */
    private String description;

    /** Relation vers le compte associé. */
    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    /** Version pour optimistic locking - détection des conflits concurrents. */
    @Version
    private Long version;

    // ===================== Constructeurs =====================

    public Transaction() {
        this.timestamp = LocalDateTime.now();
        this.amount = BigDecimal.ZERO;
    }

    public Transaction(String transactionID, String type, double amount, String description) {
        this.transactionID = transactionID;
        this.type = type;
        this.amount = BigDecimal.valueOf(amount);
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public Transaction(String transactionID, String type, BigDecimal amount, String description) {
        this.transactionID = transactionID;
        this.type = type;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * @deprecated Use the constructor without accountID and call
     *             setAccount(Account) instead.
     */
    @Deprecated
    public Transaction(String transactionID, String type, double amount, String description, String accountID) {
        this(transactionID, type, amount, description);
        // accountID parameter ignored - use setAccount(Account) instead
    }

    /**
     * @deprecated Use the constructor without accountID and call
     *             setAccount(Account) instead.
     */
    @Deprecated
    public Transaction(String transactionID, String type, BigDecimal amount, String description, String accountID) {
        this(transactionID, type, amount, description);
        // accountID parameter ignored - use setAccount(Account) instead
    }

    // ===================== Lifecycle Callbacks =====================

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
        if (this.amount == null) {
            this.amount = BigDecimal.ZERO;
        }
    }

    // ===================== Getters =====================

    public String getTransactionID() {
        return transactionID;
    }

    public String getType() {
        return type;
    }

    /**
     * Retourne le montant en BigDecimal (méthode principale).
     */
    public BigDecimal getAmountAsBigDecimal() {
        return amount != null ? amount : BigDecimal.ZERO;
    }

    /**
     * Retourne le montant en double pour rétrocompatibilité.
     * 
     * @deprecated Utiliser getAmountAsBigDecimal() pour précision financière.
     */
    @Deprecated
    public double getAmount() {
        return amount != null ? amount.doubleValue() : 0.0;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public Account getAccount() {
        return account;
    }

    public String getAccountID() {
        return account != null ? account.getAccountID() : null;
    }

    public Long getVersion() {
        return version;
    }

    // ===================== Setters =====================

    public void setTransactionID(String transactionID) {
        this.transactionID = transactionID;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * Définit le montant avec un BigDecimal (méthode recommandée).
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount != null ? amount : BigDecimal.ZERO;
    }

    /**
     * Définit le montant avec un double pour rétrocompatibilité.
     * 
     * @deprecated Utiliser setAmount(BigDecimal) pour précision financière.
     */
    @Deprecated
    public void setAmount(double amount) {
        this.amount = BigDecimal.valueOf(amount);
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * @deprecated Use setAccount(Account) for proper JPA relationship.
     */
    @Deprecated
    public void setAccountID(String accountID) {
        // Kept for backward compatibility - prefer setAccount()
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
