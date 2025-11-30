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
 * Bean de présentation pour la gestion des comptes bancaires.
 *
 * Responsabilités principales :
 * - Exposer la liste des comptes à la couche JSF (lecture seule via DTO).
 * - Orchestrer la création de comptes (type + nom, appel service).
 * - Gérer la sélection d’un compte courant pour l’affichage des détails.
 * - Piloter les opérations de dépôt / retrait / transfert.
 *
 * Portée session :
 * - Le bean vit pendant toute la session HTTP de l’utilisateur.
 * - Doit rester léger (pas d’objets trop volumineux) et être sérialisable.
 */
@Named
@SessionScoped
public class AccountBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private WebAppService service;

    // Etat UI : affichage du sélecteur de type lors de la création d'un compte
    private boolean showTypeSelector = false;

    // Type du compte à créer (par ex. "Courant", "Épargne")
    private String selectedType;

    // Nom libre donné au compte lors de la création
    private String newAccountName;

    // Compte actuellement sélectionné côté UI (détail, historique)
    private AccountDTO selectedAccount;

    // Id du compte sélectionné pour les opérations DEPOSIT / WITHDRAWAL
    private String selectedAccountId;

    // Liste des comptes affichés dans accounts.xhtml
    private List<AccountDTO> accounts = new ArrayList<>();

    // Champs pour les opérations (dépôt / retrait / transfert)
    private double amount;                 // Montant saisi par l'utilisateur
    private String transactionDescription; // Description libre de l'opération
    private String expenseCategory;        // Catégorie (dépense / entrée / transfert)

    /**
     * Type d'opération choisie dans l'UI : "DEPOSIT", "WITHDRAWAL" ou "TRANSFER".
     * Par défaut, on propose un dépôt pour simplifier l’usage.
     */
    private String operationType = "DEPOSIT";

    // Identifiants utilisés uniquement pour l'activité de transfert
    private String transferSourceAccountId;
    private String transferTargetAccountId;

    // Transaction sélectionnée pour l’affichage détaillé dans l’historique
    private WebAppService.LocalTransaction selectedTransaction;

    /**
     * DTO simplifié utilisé uniquement par la couche web.
     * On évite d'exposer directement les entités du domaine.
     */
    public static class AccountDTO {

        private String id;
        private String type;
        private String number;
        private String name;
        private double balance;

        public AccountDTO() {
            // Constructeur par défaut requis par certaines libs / outils
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
       Accès aux listes / données de base
       ========================================================== */

    /**
     * Recharge la liste des comptes depuis le service métier.
     * Méthode interne pour centraliser la logique de mapping DTO.
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
     * Accesseur utilisé par la page JSF.
     * Recharge à chaque appel pour rester à jour.
     */
    public List<AccountDTO> getAccounts() {
        refreshAccounts();
        return accounts;
    }

    // Retourne le solde total de tous les comptes
    public double getTotalBalance() {
        double total = 0.0;
        for (AccountDTO acc : getAccounts()) {
            total += acc.getBalance();
        }
        return total;
    }

    /* ==========================================================
       Gestion de la création de compte
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
                    "Type de compte manquant",
                    "Veuillez sélectionner un type de compte avant de créer.");
            return;
        }

        service.createAccount(selectedType, newAccountName);

        selectedType = null;
        newAccountName = null;
        showTypeSelector = false;
        refreshAccounts();

        addGlobalMessage(FacesMessage.SEVERITY_INFO,
                "Compte créé",
                "Le nouveau compte a été créé avec succès.");
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
       Sélection d'un compte pour affichage des détails
       ========================================================== */

    public void selectAccount(String id) {
        var acc = service.getAccountById(id);
        if (acc == null) {
            selectedAccount = null;
            selectedAccountId = null;
            addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    "Compte introuvable",
                    "Le compte demandé n'existe pas ou plus.");
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

        // On réinitialise le formulaire d'opérations à chaque sélection
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
       Opérations : dépôt / retrait / transfert
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
            "Dépôt cash",
            "Salaire",
            "Dividendes",
            "Remboursement",
            "Autre"
    );

    private static final List<String> WITHDRAWAL_CATEGORIES = List.of(
            "Loisirs",
            "Ménage",
            "Loyer",
            "Assurance",
            "Abonnements",
            "Autre"
    );

    private static final List<String> TRANSFER_CATEGORIES = List.of(
            "Virement interne",
            "Réallocation épargne",
            "Autre"
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
     * Effectue l'opération choisie (dépôt / retrait / transfert) puis recharge soldes + historique.
     */
    public void confirmOperation() {
        if (amount <= 0) {
            addGlobalMessage(FacesMessage.SEVERITY_WARN,
                    "Montant invalide",
                    "Le montant doit être strictement positif.");
            return;
        }

        boolean ok = false;

        if ("DEPOSIT".equalsIgnoreCase(operationType)) {
            if (selectedAccountId == null || selectedAccountId.isBlank()) {
                addGlobalMessage(FacesMessage.SEVERITY_WARN,
                        "Aucun compte sélectionné",
                        "Veuillez choisir un compte avant d'effectuer un dépôt.");
                return;
            }
            ok = service.depositToAccount(selectedAccountId, amount);

        } else if ("WITHDRAWAL".equalsIgnoreCase(operationType)) {
            if (selectedAccountId == null || selectedAccountId.isBlank()) {
                addGlobalMessage(FacesMessage.SEVERITY_WARN,
                        "Aucun compte sélectionné",
                        "Veuillez choisir un compte avant d'effectuer un retrait.");
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
                        "Transfert incomplet",
                        "Veuillez sélectionner un compte source et un compte destinataire.");
                return;
            }
            if (transferSourceAccountId.equals(transferTargetAccountId)) {
                addGlobalMessage(FacesMessage.SEVERITY_WARN,
                        "Transfert invalide",
                        "Les comptes source et destinataire doivent être différents.");
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
                    "Opération refusée",
                    "L'opération n'a pas pu être effectuée. Vérifiez le solde ou les paramètres.");
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
                "Opération enregistrée",
                "La transaction a été enregistrée avec succès.");
    }

    public List<WebAppService.LocalTransaction> getSelectedAccountTransactions() {
        if (selectedAccountId == null || selectedAccountId.isBlank()) {
            return List.of();
        }
        return service.getTransactionsForAccount(selectedAccountId);
    }

    /* ==========================================================
       Utilitaires internes
       ========================================================== */

    /**
     * Recherche un DTO de compte par identifiant.
     * Utilisé notamment pour afficher le nom de compte dans l’historique.
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
        // On ne touche pas à selectedAccountId ni selectedTransaction
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
