package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.groupm.ewallet.webapp.service.WebAppService;

@Named("accountBean")
@SessionScoped
public class AccountBean implements Serializable {

    @Inject
    private WebAppService service;

    // UI state
    private boolean showTypeSelector = false;
    private String selectedType = "";

    // Deposit
    private double depositAmount = 0.0;

    // The account currently viewed
    private AccountDTO selectedAccount;

    // ---------------------------------------------------------
    // DTO
    // ---------------------------------------------------------
    public static class AccountDTO {
        private String id;        // String !
        private String type;      // String
        private String number;    // String (same as id)
        private double balance;   // double

        public AccountDTO(String id, String type, String number, double balance) {
            this.id = id;
            this.type = type;
            this.number = number;
            this.balance = balance;
        }

        public String getId() { return id; }
        public String getType() { return type; }
        public String getNumber() { return number; }
        public double getBalance() { return balance; }

        public void setBalance(double balance) { this.balance = balance; }
    }

    // ---------------------------------------------------------
    // ACCOUNT CREATION
    // ---------------------------------------------------------

    public void startCreation() {
        showTypeSelector = true;
    }

    public String createAccount() {
        if (selectedType == null || selectedType.isBlank()) {
            return null;
        }

        service.createAccount(selectedType);

        showTypeSelector = false;
        selectedType = "";

        return "accounts.xhtml?faces-redirect=true";
    }

    // ---------------------------------------------------------
    // LOAD LIST
    // ---------------------------------------------------------

    private List<AccountDTO> convert(List<WebAppService.LocalAccount> list) {
        List<AccountDTO> res = new ArrayList<>();

        for (var acc : list) {
            res.add(
                    new AccountDTO(
                            acc.getId(),
                            acc.getType(),
                            acc.getNumber(),   // String
                            acc.getBalance()
                    )
            );
        }

        return res;
    }

    public List<AccountDTO> getAccounts() {
        return convert(service.getAccounts());
    }

    // ---------------------------------------------------------
    // OPEN DETAILS
    // ---------------------------------------------------------

    public String openDetails(String id) {

        WebAppService.LocalAccount acc = service.getAccountById(id);
        if (acc == null) return null;

        selectedAccount = new AccountDTO(
                acc.getId(),
                acc.getType(),
                acc.getNumber(),
                acc.getBalance()
        );

        return "accountDetails.xhtml?faces-redirect=true";
    }

    // ---------------------------------------------------------
    // DEPOSIT
    // ---------------------------------------------------------

    public String deposit() {

        if (selectedAccount == null) return null;

        boolean ok = service.depositToAccount(selectedAccount.getId(), depositAmount);

        if (ok) {
            selectedAccount.setBalance(selectedAccount.getBalance() + depositAmount);
        }

        depositAmount = 0.0;

        return "accountDetails.xhtml?faces-redirect=true";
    }

    // ---------------------------------------------------------
    // GETTERS / SETTERS
    // ---------------------------------------------------------

    public AccountDTO getSelectedAccount() {
        return selectedAccount;
    }

    public boolean isShowTypeSelector() {
        return showTypeSelector;
    }

    public void setShowTypeSelector(boolean showTypeSelector) {
        this.showTypeSelector = showTypeSelector;
    }

    public String getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(String selectedType) {
        this.selectedType = selectedType;
    }

    public double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(double depositAmount) {
        this.depositAmount = depositAmount;
    }
}