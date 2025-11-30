package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.groupm.ewallet.webapp.service.WebAppService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class AccountBean implements Serializable {

    @Inject
    private WebAppService service;

    // UI state
    private boolean showTypeSelector = false;
    private String selectedType;

    // Selected account for details page
    private AccountDTO selectedAccount;

    // List of accounts displayed in accounts.xhtml
    private List<AccountDTO> accounts = new ArrayList<>();

    // DTO class used by the WebApp
    public static class AccountDTO {

        private String id;
        private String type;
        private String number;
        private double balance;

        public AccountDTO() {}

        public AccountDTO(String id, String type, String number, double balance) {
            this.id = id;
            this.type = type;
            this.number = number;
            this.balance = balance;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }
    }

    // Load accounts from service
    private void refreshAccounts() {
        accounts = new ArrayList<>();
        for (var acc : service.getAccounts()) {
            accounts.add(
                    new AccountDTO(
                            acc.getId(),
                            acc.getType(),
                            acc.getNumber(),
                            acc.getBalance()
                    )
            );
        }
    }

    public List<AccountDTO> getAccounts() {
        refreshAccounts();
        return accounts;
    }

    // UI logic
    public boolean isShowTypeSelector() {
        return showTypeSelector;
    }

    public void startCreation() {
        showTypeSelector = true;
    }

    public void cancelCreation() {
        showTypeSelector = false;
        selectedType = null;
    }

    public void createAccount() {
        if (selectedType == null) return;

        service.createAccount(selectedType);

        selectedType = null;
        showTypeSelector = false;
        refreshAccounts();
    }

    public String openDetails(String id) {
        var acc = service.getAccountById(id);
        if (acc == null)
            return "accounts.xhtml?faces-redirect=true";

        selectedAccount = new AccountDTO(
                acc.getId(),
                acc.getType(),
                acc.getNumber(),
                acc.getBalance()
        );

        return "accountDetails.xhtml?faces-redirect=true";
    }

    public AccountDTO getSelectedAccount() {
        return selectedAccount;
    }

    // Deposit logic
    private double depositAmount;

    public double getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(double depositAmount) {
        this.depositAmount = depositAmount;
    }

    public void deposit() {
        if (selectedAccount == null) return;

        service.depositToAccount(selectedAccount.getId(), depositAmount);

        // reload updated balance
        var acc = service.getAccountById(selectedAccount.getId());
        selectedAccount.setBalance(acc.getBalance());

        depositAmount = 0.0;
    }

    // Getter & setter for selectedType
    public String getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(String selectedType) {
        this.selectedType = selectedType;
    }
}