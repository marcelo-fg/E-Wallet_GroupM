package org.groupm.ewallet.service.business;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.groupm.ewallet.model.Account;
import org.groupm.ewallet.model.User;
import org.groupm.ewallet.model.Transaction;
import org.groupm.ewallet.repository.impl.JpaAccountRepository;
import org.groupm.ewallet.repository.impl.JpaTransactionRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service métier pour la gestion des comptes et des transactions.
 * Cette classe encapsule la logique de création, modification, suppression
 * et récupération des comptes/transactions via abstraction repository.
 * 
 * Toutes les méthodes de modification utilisent @Transactional pour garantir
 * ACID.
 */
@ApplicationScoped
public class AccountManager {

    @Inject
    private JpaAccountRepository accountRepository;

    @Inject
    private JpaTransactionRepository transactionRepository;

    @Inject
    private EntityManager em;

    /**
     * Récupère tous les comptes.
     * 
     * @return liste complète des comptes
     */
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Ajoute un nouveau compte.
     * 
     * @param account compte à persister
     * @return compte ajouté
     */
    @Transactional
    public Account addAccount(Account account) {
        accountRepository.save(account);
        return account;
    }

    /**
     * Recherche un compte par son identifiant (String pour cohérence).
     * 
     * @param id identifiant du compte
     * @return compte ou null
     */
    public Account getAccountById(String id) {
        return accountRepository.findById(id);
    }

    /**
     * Supprime un compte existant selon son identifiant.
     * 
     * @param id identifiant du compte à supprimer
     * @return true si suppression réussie, false sinon
     */
    @Transactional
    public boolean deleteAccount(String id) {
        Account account = accountRepository.findById(id);
        if (account != null) {
            accountRepository.delete(id);
            return true;
        }
        return false;
    }

    /**
     * Met à jour les informations d'un compte existant.
     * Les champs non nuls du nouvel objet remplacent ceux du compte existant.
     *
     * @param id         identifiant du compte
     * @param newAccount données à mettre à jour
     * @return true si mise à jour réussie, false sinon
     */
    @Transactional
    public boolean updateAccount(String id, Account newAccount) {
        Account account = accountRepository.findById(id);
        if (account != null) {
            if (newAccount.getType() != null) {
                account.setType(newAccount.getType());
            }
            if (newAccount.getBalance() != 0) {
                account.setBalance(newAccount.getBalanceAsBigDecimal());
            }
            if (newAccount.getName() != null) {
                account.setName(newAccount.getName());
            }
            accountRepository.save(account);
            return true;
        }
        return false;
    }

    /**
     * Liste tous les comptes d'un utilisateur donné.
     * 
     * @param user utilisateur concerné
     * @return liste de comptes
     */
    public List<Account> getAccountsByUser(User user) {
        return accountRepository.findByUserId(user.getUserID());
    }

    // ===================== Gestion des transactions =====================

    /**
     * Génère un identifiant unique de transaction si absent.
     * Utilise UUID pour garantir l'unicité même sous forte charge.
     */
    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString();
    }

    /**
     * Ajoute une transaction en appliquant la logique métier (dépôt/retrait).
     * Utilise @Transactional pour garantir l'atomicité.
     * 
     * @param transaction modèle reçu du webservice
     * @return transaction persistée
     */
    @Transactional
    public Transaction addTransaction(Transaction transaction) {

        // Validation complète (centralisée dans Manager)
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null.");
        }
        if (transaction.getAccountID() == null || transaction.getAccountID().isEmpty()) {
            throw new IllegalArgumentException("No accountID provided for the transaction.");
        }
        if (transaction.getType() == null || transaction.getType().isEmpty()) {
            throw new IllegalArgumentException("Transaction type is required.");
        }
        if (transaction.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        Account account = accountRepository.findById(transaction.getAccountID());

        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + transaction.getAccountID());
        }

        // Génération automatique de l'ID si absent
        if (transaction.getTransactionID() == null || transaction.getTransactionID().isEmpty()) {
            transaction.setTransactionID(generateTransactionId());
        }

        String type = transaction.getType();
        BigDecimal amount = transaction.getAmountAsBigDecimal();
        BigDecimal currentBalance = account.getBalanceAsBigDecimal();

        switch (type.toLowerCase()) {

            case "deposit":
                account.setBalance(currentBalance.add(amount));
                break;

            case "withdraw":
                if (currentBalance.compareTo(amount) < 0) {
                    throw new IllegalArgumentException("Insufficient balance for withdrawal.");
                }
                account.setBalance(currentBalance.subtract(amount));
                break;

            default:
                throw new IllegalArgumentException("Unsupported transaction type: " + type);
        }

        // Ajout de la transaction au modèle du compte
        account.addTransaction(transaction);

        // Mise à jour du compte
        accountRepository.save(account);

        // Persistance de la transaction
        transactionRepository.save(transaction);

        return transaction;
    }

    /**
     * Recherche une transaction par ID.
     */
    public Transaction getTransactionById(String transactionID) {
        return transactionRepository.findById(transactionID);
    }

    /**
     * Récupère toutes les transactions.
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Récupère les transactions d'un compte.
     */
    public List<Transaction> getTransactionsByAccountId(String accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    /**
     * Supprime une transaction.
     */
    @Transactional
    public boolean deleteTransaction(String transactionID) {
        Transaction tx = transactionRepository.findById(transactionID);
        if (tx != null) {
            transactionRepository.delete(transactionID);
            return true;
        }
        return false;
    }

    /**
     * Effectue un virement ATOMIQUE entre deux comptes.
     * Utilise @Transactional pour garantir que SOIT tout réussit, SOIT tout échoue.
     * Évite les pertes de données financières.
     * 
     * @param fromId      compte source
     * @param toId        compte destination
     * @param amount      montant
     * @param category    catégorie (optionnel)
     * @param description description (optionnel)
     * @return true si succès, false sinon
     * @throws IllegalArgumentException si validation échoue (provoque rollback
     *                                  automatique)
     */
    @Transactional
    public boolean transfer(String fromId, String toId, double amount, String category, String description) {
        return transfer(fromId, toId, BigDecimal.valueOf(amount), category, description);
    }

    /**
     * Effectue un virement ATOMIQUE entre deux comptes avec BigDecimal.
     * Utilise un verrouillage pessimiste (PESSIMISTIC_WRITE) pour éviter
     * les problèmes de concurrence et les race conditions.
     */
    @Transactional
    public boolean transfer(String fromId, String toId, BigDecimal amount, String category, String description) {
        // Validations préliminaires
        if (fromId == null || toId == null) {
            throw new IllegalArgumentException("Account IDs cannot be null.");
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Cannot transfer to the same account.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero.");
        }

        // Load accounts with pessimistic locking
        // Lock acquisition order based on ID to prevent deadlocks
        Account from, to;
        if (fromId.compareTo(toId) < 0) {
            from = em.find(Account.class, fromId, LockModeType.PESSIMISTIC_WRITE);
            to = em.find(Account.class, toId, LockModeType.PESSIMISTIC_WRITE);
        } else {
            to = em.find(Account.class, toId, LockModeType.PESSIMISTIC_WRITE);
            from = em.find(Account.class, fromId, LockModeType.PESSIMISTIC_WRITE);
        }

        if (from == null) {
            throw new IllegalArgumentException("Source account not found: " + fromId);
        }
        if (to == null) {
            throw new IllegalArgumentException("Destination account not found: " + toId);
        }

        BigDecimal fromBalance = from.getBalanceAsBigDecimal();

        // Validate balance BEFORE any modification
        if (fromBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient balance. Available: " + fromBalance + " CHF, Requested: " + amount + " CHF");
        }

        // === TRANSACTION ATOMIQUE : Tout ou rien ===

        // 1. Modifier les balances
        from.setBalance(fromBalance.subtract(amount));
        to.setBalance(to.getBalanceAsBigDecimal().add(amount));

        // 2. Créer les transactions (historique) avec relation JPA correcte
        Transaction withdrawal = new Transaction();
        withdrawal.setTransactionID(generateTransactionId());
        withdrawal.setAccount(from); // Utiliser la relation JPA
        withdrawal.setType("withdraw");
        withdrawal.setAmount(amount);
        withdrawal.setDescription("Transfer to " + toId + (description != null ? ": " + description : ""));

        Transaction deposit = new Transaction();
        deposit.setTransactionID(generateTransactionId());
        deposit.setAccount(to); // Utiliser la relation JPA
        deposit.setType("deposit");
        deposit.setAmount(amount);
        deposit.setDescription("Transfer from " + fromId + (description != null ? ": " + description : ""));

        // 3. Ajouter les transactions aux modèles de compte
        from.addTransaction(withdrawal);
        to.addTransaction(deposit);

        // 4. Persister TOUT dans UNE SEULE transaction JPA
        accountRepository.save(from);
        accountRepository.save(to);
        transactionRepository.save(withdrawal);
        transactionRepository.save(deposit);

        return true;
    }

}
