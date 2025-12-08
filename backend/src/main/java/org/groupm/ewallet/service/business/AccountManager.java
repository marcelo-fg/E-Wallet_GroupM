package org.groupm.ewallet.service.business;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.groupm.ewallet.model.Account;
import org.groupm.ewallet.model.User;
import org.groupm.ewallet.model.Transaction;
import org.groupm.ewallet.repository.AccountRepository;
import org.groupm.ewallet.repository.TransactionRepository;

import java.util.List;

/**
 * Service métier pour la gestion des comptes et des transactions.
 * Cette classe encapsule la logique de création, modification, suppression
 * et récupération des comptes/transactions via abstraction repository.
 */
@ApplicationScoped
public class AccountManager {

    @Inject
    private AccountRepository accountRepository;

    @Inject
    private TransactionRepository transactionRepository;

    // CDI will inject repositories automatically

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
    public boolean deleteAccount(String id) {
        Account account = accountRepository.findById(id);
        if (account != null) {
            accountRepository.delete(id);
            return true;
        }
        return false;
    }

    /**
     * Met à jour les informations d’un compte existant.
     * Les champs non nuls du nouvel objet remplacent ceux du compte existant.
     *
     * @param id         identifiant du compte
     * @param newAccount données à mettre à jour
     * @return true si mise à jour réussie, false sinon
     */
    public boolean updateAccount(String id, Account newAccount) {
        Account account = accountRepository.findById(id);
        if (account != null) {
            if (newAccount.getType() != null) {
                account.setType(newAccount.getType());
            }
            if (newAccount.getBalance() != 0) {
                account.setBalance(newAccount.getBalance());
            }
            if (newAccount.getName() != null) {
                account.setName(newAccount.getName());
            }
            accountRepository.save(account); // Update via repo
            return true;
        }
        return false;
    }

    /**
     * Liste tous les comptes d’un utilisateur donné.
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
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis();
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
            throw new IllegalArgumentException("Transaction ne peut pas être null.");
        }
        if (transaction.getAccountID() == null || transaction.getAccountID().isEmpty()) {
            throw new IllegalArgumentException("Aucun 'accountID' fourni pour la transaction.");
        }
        if (transaction.getType() == null || transaction.getType().isEmpty()) {
            throw new IllegalArgumentException("Le type de transaction est obligatoire.");
        }
        if (transaction.getAmount() <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement supérieur à zéro.");
        }

        Account account = accountRepository.findById(transaction.getAccountID());

        if (account == null) {
            throw new IllegalArgumentException("Le compte spécifié n'existe pas : " + transaction.getAccountID());
        }

        // Génération automatique de l'ID si absent
        if (transaction.getTransactionID() == null || transaction.getTransactionID().isEmpty()) {
            transaction.setTransactionID(generateTransactionId());
        }

        String type = transaction.getType();
        double amount = transaction.getAmount();

        switch (type.toLowerCase()) {

            case "deposit":
                account.setBalance(account.getBalance() + amount);
                break;

            case "withdraw":
                if (account.getBalance() < amount) {
                    throw new IllegalArgumentException("Solde insuffisant pour effectuer un retrait.");
                }
                account.setBalance(account.getBalance() - amount);
                break;

            default:
                throw new IllegalArgumentException("Type de transaction non supporté : " + type);
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
     * Supprime une transaction.
     */
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
        // Validations préliminaires
        if (fromId == null || toId == null) {
            throw new IllegalArgumentException("Les identifiants de compte ne peuvent pas être null.");
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Impossible de transférer vers le même compte.");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Le montant doit être strictement positif.");
        }

        // Chargement des comptes
        Account from = accountRepository.findById(fromId);
        Account to = accountRepository.findById(toId);

        if (from == null) {
            throw new IllegalArgumentException("Compte source introuvable : " + fromId);
        }
        if (to == null) {
            throw new IllegalArgumentException("Compte destination introuvable : " + toId);
        }

        // Validation du solde AVANT toute modification
        if (from.getBalance() < amount) {
            throw new IllegalArgumentException(
                    "Solde insuffisant. Disponible: " + from.getBalance() + "€, Demandé: " + amount + "€");
        }

        // === TRANSACTION ATOMIQUE : Tout ou rien ===

        // 1. Modifier les balances
        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);

        // 2. Créer les transactions (historique)
        Transaction withdrawal = new Transaction();
        withdrawal.setTransactionID(generateTransactionId());
        withdrawal.setAccountID(fromId);
        withdrawal.setType("withdraw");
        withdrawal.setAmount(amount);
        withdrawal.setDescription("Transfer to " + toId + (description != null ? ": " + description : ""));

        Transaction deposit = new Transaction();
        deposit.setTransactionID(generateTransactionId());
        deposit.setAccountID(toId);
        deposit.setType("deposit");
        deposit.setAmount(amount);
        deposit.setDescription("Transfer from " + fromId + (description != null ? ": " + description : ""));

        // 3. Ajouter les transactions aux modèles de compte
        from.addTransaction(withdrawal);
        to.addTransaction(deposit);

        // 4. Persister TOUT dans UNE SEULE transaction JPA
        // Si UNE SEULE opération échoue → rollback automatique de TOUT
        accountRepository.save(from);
        accountRepository.save(to);
        transactionRepository.save(withdrawal);
        transactionRepository.save(deposit);

        // Si on arrive ici, transaction JPA commit automatiquement
        // Sinon, exception → rollback automatique via @Transactional
        return true;
    }

}
