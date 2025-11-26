package org.groupm.ewallet.dto;

import java.util.List;

/**
 * DTO pour transporter les données utilisateur (sans mot de passe).
 * Version adaptée au mode multi-portefeuilles.
 */
public class UserDTO {

    private String userID;
    private String email;
    private String firstName;
    private String lastName;

    private List<AccountDTO> accounts;

    // Un utilisateur peut avoir plusieurs portefeuilles
    private List<PortfolioDTO> portfolios;

    // ======= Getters / Setters =======

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<AccountDTO> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountDTO> accounts) {
        this.accounts = accounts;
    }

    public List<PortfolioDTO> getPortfolios() {
        return portfolios;
    }

    public void setPortfolios(List<PortfolioDTO> portfolios) {
        this.portfolios = portfolios;
    }
}