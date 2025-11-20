package org.groupm.ewallet.service.business;

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
public class AccountManager {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    /**
     * Constructeur avec injection de dépendances (repository).
     * Permet de mocker ou reconfigurer la persistance selon besoin.
     *
     * @param accountRepository     repository de comptes
     * @param transactionRepository repository de transactions
     */
    public AccountManager(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Récupère tous les comptes.
     * @return liste complète des comptes
     */
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    /**
     * Ajoute un nouveau compte.
     * @param account compte à persister
     * @return compte ajouté
     */
    public Account addAccount(Account account) {
        accountRepository.save(account);
        return account;
    }

    /**
     * Recherche un compte par son identifiant (String pour cohérence).
     * @param id identifiant du compte
     * @return compte ou null
     */
    public Account getAccountById(String id) {
        return accountRepository.findById(id);
    }

    /**
     * Supprime un compte existant selon son identifiant.
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
     * @param id identifiant du compte
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
            accountRepository.save(account); // Update via repo
            return true;
        }
        return false;
    }

    /**
     * Liste tous les comptes d’un utilisateur donné.
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
     * @param transaction modèle reçu du webservice
     * @return transaction persistée
     */
    public Transaction addTransaction(Transaction transaction) {

        // Vérification du compte cible
        if (transaction.getAccountID() == null) {
            throw new IllegalArgumentException("Aucun 'accountID' fourni pour la transaction.");
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

        if (type == null || amount <= 0) {
            throw new IllegalArgumentException("Type de transaction invalide ou montant <= 0.");
        }

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

}
