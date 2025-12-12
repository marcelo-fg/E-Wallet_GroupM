package org.groupm.ewallet.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un utilisateur du système, incluant ses informations
 * personnelles, ses comptes bancaires et ses portefeuilles d'investissement.
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identifiant unique de l'utilisateur. */
    @Id
    @Column(name = "user_id")
    private String userID;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    /** Version pour optimistic locking - détection des conflits concurrents. */
    @Version
    private Long version;

    /** Solde total cumulé de tous les comptes de l'utilisateur. */
    @Transient // Calculé, pas stocké
    private BigDecimal totalBalance = BigDecimal.ZERO;

    /** Liste des comptes bancaires associés à l'utilisateur. */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Account> accounts;

    /** Liste des portefeuilles d'investissement associés à l'utilisateur. */
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "user_id") // Clé étrangère dans la table portfolio
    private List<Portfolio> portfolios;

    // ===================== Constructeurs =====================

    public User() {
        this.accounts = new ArrayList<>();
        this.portfolios = new ArrayList<>();
        this.totalBalance = BigDecimal.ZERO;
    }

    public User(String userID, String email, String password,
            String firstName, String lastName) {
        this.userID = userID;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accounts = new ArrayList<>();
        this.portfolios = new ArrayList<>();
        this.totalBalance = BigDecimal.ZERO;
    }

    // ===================== Getters =====================

    public String getUserID() {
        return userID;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Long getVersion() {
        return version;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Portfolio> getPortfolios() {
        return portfolios;
    }

    /**
     * Retourne le solde total en BigDecimal.
     */
    public BigDecimal getTotalBalanceAsBigDecimal() {
        return initiateTotalBalanceAsBigDecimal();
    }

    /**
     * Retourne le solde total en double pour rétrocompatibilité.
     * 
     * @deprecated Utiliser getTotalBalanceAsBigDecimal() pour précision financière.
     */
    @Deprecated
    public double getTotalBalance() {
        return getTotalBalanceAsBigDecimal().doubleValue();
    }

    // ===================== Setters =====================

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = (accounts != null) ? accounts : new ArrayList<>();
        this.totalBalance = initiateTotalBalanceAsBigDecimal();
    }

    public void setPortfolios(List<Portfolio> portfolios) {
        this.portfolios = (portfolios != null) ? portfolios : new ArrayList<>();
    }

    public void setPortfolio(Portfolio portfolio) {
        if (this.portfolios == null) {
            this.portfolios = new ArrayList<>();
        }
        this.portfolios.clear();
        if (portfolio != null) {
            this.portfolios.add(portfolio);
        }
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance != null ? totalBalance : BigDecimal.ZERO;
    }

    @Deprecated
    public void setTotalBalance(double totalBalance) {
        this.totalBalance = BigDecimal.valueOf(totalBalance);
    }

    // ===================== Méthodes principales =====================

    public void addAccount(Account account) {
        if (account != null) {
            accounts.add(account);
            this.totalBalance = initiateTotalBalanceAsBigDecimal();
        }
    }

    public void addPortfolio(Portfolio portfolio) {
        if (portfolio != null) {
            portfolios.add(portfolio);
        }
    }

    public BigDecimal initiateTotalBalanceAsBigDecimal() {
        BigDecimal total = BigDecimal.ZERO;
        if (accounts != null) {
            for (Account account : accounts) {
                total = total.add(account.getBalanceAsBigDecimal());
            }
        }
        return total;
    }

    @Deprecated
    public double initiateTotalBalance() {
        return initiateTotalBalanceAsBigDecimal().doubleValue();
    }

    @Override
    public String toString() {
        return "User: " + firstName + " " + lastName + " (" + email + ")";
    }
}
