package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.groupm.ewallet.webapp.service.WebAppService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Presentation bean for managing bank accounts.
 *
 * Main responsibilities:
 * - Expose the list of accounts to the JSF layer (read-only via DTO).
 * - Orchestrate account creation (type + name, service invocation).
 * - Manage the currently selected account for details and history display.
 * - Drive deposit / withdrawal / transfer operations.
 *
 * Session scope:
 * - The bean lives for the whole HTTP session of the user.
 * - Must remain lightweight (no heavy objects) and be serializable.
 */
@Named
@SessionScoped
public class AccountBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private WebAppService service;

    // UI state: display the type selector when creating a new account
    private boolean showTypeSelector = false;

    // Type of the account to create (e.g. "Checking", "Savings")
    private String selectedType;

    // Free name given to the account at creation time
    private String newAccountName;

    // Account currently selected on the UI (details, history)
    private AccountDTO selectedAccount;

    // Id of the account selected for DEPOSIT / WITHDRAWAL operations
    private String selectedAccountId;

    // List of accounts displayed in accounts.xhtml
    private List<AccountDTO> accounts = new ArrayList<>();

    // Fields used for operations (deposit / withdrawal / transfer)
    private double amount;                 // Amount entered by the user
    private String transactionDescription; // Free description of the operation
    private String expenseCategory;        // Category (expense / income / transfer)

    /**
     * Operation type chosen in the UI: "DEPOSIT", "WITHDRAWAL" or "TRANSFER".
     * By default, a deposit is proposed to simplify the usage.
     */
    private String operationType = "DEPOSIT";

    // Identifiers used only for transfer operations
    private String transferSourceAccountId;
    private String transferTargetAccountId;

    // Transaction selected for detailed display in the history
    private WebAppService.LocalTransaction selectedTransaction;

    /**
     * Simplified DTO used only by the web layer.
     * Avoid exposing domain entities directly.
     */
    public static class AccountDTO {

        private String id;
        private String type;
        private String number;
        private String name;
        private double balance;

        public AccountDTO() {
            // Default constructor required by some libs / tools
        }

        public AccountDTO(String id, String type, String number, String name, double balance) {
            this.id = id;
            this.type = type;
            this.number = number;
            this.name = name;
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }
    }

    /* ==========================================================
       Access to lists / base data
       ========================================================== */

    /**
     * Reload the list of accounts from the business service.
     * Internal method to centralize DTO mapping logic.
     */
    private void refreshAccounts() {
        List<AccountDTO> result = new ArrayList<>();
        service.getAccounts().forEach(acc ->
                result.add(new AccountDTO(
                        acc.getId(),
                        acc.getType(),
                        acc.getNumber(),
                        acc.getName(),
                        acc.getBalance()
                ))
        );
        this.accounts = result;
    }

    /**
     * Accessor used by the JSF page.
     * Reloads on each call to stay up to date.
     */
    public List<AccountDTO> getAccounts() {
        refreshAccounts();
        return accounts;
    }

    // Returns the total balance of all accounts
    public double getTotalBalance() {
        double total = 0.0;
        for (AccountDTO acc : getAccounts()) {
            total += acc.getBalance();
        }
        return total;
    }

    /* ==========================================================
       Account creation management
       ========================================================== */

    public boolean isShowTypeSelector() {
        return showTypeSelector;
    }

    public void startCreation() {
        showTypeSelector = true;
    }

    public void cancelCreation() {
        showTypeSelector = false;
        selectedType = null;
        newAccountName = null;
    }

    public void createAccount() {
        if (selectedType == null || selectedType.isBlank()) {
            addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    "Missing account type",
                    "Please select an account type before creating.");
            return;
        }

        service.createAccount(selectedType, newAccountName);

        selectedType = null;
        newAccountName = null;
        showTypeSelector = false;
        refreshAccounts();

        addGlobalMessage(FacesMessage.SEVERITY_INFO,
                "Account created",
                "The new account has been created successfully.");
    }

    public String getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(String selectedType) {
        this.selectedType = selectedType;
    }

    public String getNewAccountName() {
        return newAccountName;
    }

    public void setNewAccountName(String newAccountName) {
        this.newAccountName = newAccountName;
    }

    /* ==========================================================
       Account selection for details display
       ========================================================== */

    public void selectAccount(String id) {
        var acc = service.getAccountById(id);
        if (acc == null) {
            selectedAccount = null;
            selectedAccountId = null;
            addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    "Account not found",
                    "The requested account does not exist anymore.");
            return;
        }

        selectedAccountId = acc.getId();

        selectedAccount = new AccountDTO(
                acc.getId(),
                acc.getType(),
                acc.getNumber(),
                acc.getName(),
                acc.getBalance()
        );

        // Reset the operations form whenever an account is selected
        resetOperationForm();
    }

    public AccountDTO getSelectedAccount() {
        return selectedAccount;
    }

    public String getSelectedAccountId() {
        return selectedAccountId;
    }

    public void setSelectedAccountId(String selectedAccountId) {
        this.selectedAccountId = selectedAccountId;
    }

    /* ==========================================================
       Operations: deposit / withdrawal / transfer
       ========================================================== */

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public void setTransactionDescription(String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }

    public String getExpenseCategory() {
        return expenseCategory;
    }

    public void setExpenseCategory(String expenseCategory) {
        this.expenseCategory = expenseCategory;
    }

    private static final List<String> DEPOSIT_CATEGORIES = List.of(
            "Cash deposit",
            "Salary",
            "Dividends",
            "Refund",
            "Other"
    );

    private static final List<String> WITHDRAWAL_CATEGORIES = List.of(
            "Leisure",
            "Household",
            "Rent",
            "Insurance",
            "Subscriptions",
            "Other"
    );

    private static final List<String> TRANSFER_CATEGORIES = List.of(
            "Internal transfer",
            "Savings reallocation",
            "Other"
    );

    public List<String> getExpenseCategories() {
        if ("DEPOSIT".equalsIgnoreCase(operationType)) {
            return DEPOSIT_CATEGORIES;
        }
        if ("WITHDRAWAL".equalsIgnoreCase(operationType)) {
            return WITHDRAWAL_CATEGORIES;
        }
        if ("TRANSFER".equalsIgnoreCase(operationType)) {
            return TRANSFER_CATEGORIES;
        }
        return WITHDRAWAL_CATEGORIES;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getTransferSourceAccountId() {
        return transferSourceAccountId;
    }

    public void setTransferSourceAccountId(String transferSourceAccountId) {
        this.transferSourceAccountId = transferSourceAccountId;
    }

    public String getTransferTargetAccountId() {
        return transferTargetAccountId;
    }

    public void setTransferTargetAccountId(String transferTargetAccountId) {
        this.transferTargetAccountId = transferTargetAccountId;
    }

    public WebAppService.LocalTransaction getSelectedTransaction() {
        return selectedTransaction;
    }

    public void setSelectedTransaction(WebAppService.LocalTransaction selectedTransaction) {
        this.selectedTransaction = selectedTransaction;
    }

    /**
     * Performs the selected operation (deposit / withdrawal / transfer) then reloads balances and history.
     */
    public void confirmOperation() {
        if (amount <= 0) {
            addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    "Invalid amount",
                    "The amount must be strictly positive.");
            return;
        }

        boolean ok = false;

        if ("DEPOSIT".equalsIgnoreCase(operationType)) {
            if (selectedAccountId == null || selectedAccountId.isBlank()) {
                addGlobalMessage(FacesMessage.SEVERITY_WARN,
                        "No account selected",
                        "Please choose an account before making a deposit.");
                return;
            }
            ok = service.depositToAccount(selectedAccountId, amount);

        } else if ("WITHDRAWAL".equalsIgnoreCase(operationType)) {
            if (selectedAccountId == null || selectedAccountId.isBlank()) {
                addGlobalMessage(FacesMessage.SEVERITY_WARN,
                        "No account selected",
                        "Please choose an account before making a withdrawal.");
                return;
            }
            ok = service.withdrawFromAccount(
                    selectedAccountId,
                    amount,
                    expenseCategory,
                    transactionDescription
            );

        } else if ("TRANSFER".equalsIgnoreCase(operationType)) {
            if (transferSourceAccountId == null || transferTargetAccountId == null
                    || transferSourceAccountId.isBlank() || transferTargetAccountId.isBlank()) {
                addGlobalMessage(FacesMessage.SEVERITY_WARN,
                        "Incomplete transfer",
                        "Please select both a source and a target account.");
                return;
            }
            if (transferSourceAccountId.equals(transferTargetAccountId)) {
                addGlobalMessage(FacesMessage.SEVERITY_WARN,
                        "Invalid transfer",
                        "Source and target accounts must be different.");
                return;
            }

            ok = service.transferBetweenAccounts(
                    transferSourceAccountId,
                    transferTargetAccountId,
                    amount,
                    expenseCategory,
                    transactionDescription
            );
        }

        if (!ok) {
            addGlobalMessage(FacesMessage.SEVERITY_ERROR,
                    "Operation rejected",
                    "The operation could not be completed. Check the balance or parameters.");
            return;
        }

        if (selectedAccountId != null) {
            var acc = service.getAccountById(selectedAccountId);
            if (acc != null) {
                if (selectedAccount == null) {
                    selectedAccount = new AccountDTO(
                            acc.getId(), acc.getType(), acc.getNumber(), acc.getName(), acc.getBalance()
                    );
                } else {
                    selectedAccount.setBalance(acc.getBalance());
                }
            }
        }

        refreshAccounts();
        resetOperationForm();

        addGlobalMessage(FacesMessage.SEVERITY_INFO,
                "Operation recorded",
                "The transaction has been recorded successfully.");
    }

    public List<WebAppService.LocalTransaction> getSelectedAccountTransactions() {
        if (selectedAccountId == null || selectedAccountId.isBlank()) {
            return List.of();
        }
        return service.getTransactionsForAccount(selectedAccountId);
    }

    /* ==========================================================
       Internal utilities
       ========================================================== */

    /**
     * Find an account DTO by identifier.
     * Used mainly to display the account name in the history.
     */
    public AccountDTO findAccountById(String id) {
        for (AccountDTO dto : getAccounts()) {
            if (dto.getId().equals(id)) {
                return dto;
            }
        }
        return null;
    }

    private void updateSelectedAccountBalance() {
        if (selectedAccountId == null || selectedAccountId.isBlank()) {
            return;
        }
        var acc = service.getAccountById(selectedAccountId);
        if (acc != null && selectedAccount != null) {
            selectedAccount.setBalance(acc.getBalance());
        }
        refreshAccounts();
    }

    private void resetOperationForm() {
        this.amount = 0.0;
        this.transactionDescription = null;
        this.expenseCategory = null;
        this.operationType = "DEPOSIT";
        this.transferSourceAccountId = null;
        this.transferTargetAccountId = null;
        // Do not touch selectedAccountId nor selectedTransaction
    }

    private void addGlobalMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            return;
        }
        FacesMessage message = new FacesMessage(severity, summary, detail);
        context.addMessage(null, message);
    }
}
