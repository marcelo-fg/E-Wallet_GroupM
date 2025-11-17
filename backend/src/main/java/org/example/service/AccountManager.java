package org.example.service;

import org.example.model.Account;
import org.example.model.User;
import org.example.model.Transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Service de gestion des comptes et des transactions utilisateurs.
 * Cette classe permet la création, la modification, la suppression
 * et la récupération des comptes et transactions associés.
 */
public class AccountManager {

    /** Liste des comptes enregistrés. */
    private final List<Account> accounts = new ArrayList<>();

    /** Liste des transactions effectuées. */
    private final List<Transaction> transactions = new ArrayList<>();

    /**
     * Constructeur par défaut.
     * Initialise le gestionnaire avec un jeu de données de démonstration.
     */
    public AccountManager() {
        populate();
    }

    /**
     * Affiche les comptes d’un utilisateur dans la console.
     *
     * @param user utilisateur dont on souhaite afficher les comptes
     */
    public void listUserAccounts(User user) {
        System.out.println("\nComptes de " + user.getFirstName() + " :");
        for (Account account : user.getAccounts()) {
            System.out.println(account);
        }
        System.out.println("Solde total : " + user.getTotalBalance() + " CHF");
    }

    /**
     * Initialise quelques comptes fictifs pour démonstration.
     */
    public void populate() {
        accounts.add(new Account("1", "Test", 0.00));
    }

    /**
     * Retourne la liste de tous les comptes enregistrés.
     *
     * @return liste complète des comptes
     */
    public List<Account> getAllAccounts() {
        return accounts;
    }

    /**
     * Ajoute un nouveau compte à la liste.
     *
     * @param account compte à ajouter
     * @return compte ajouté
     */
    public Account addAccount(Account account) {
        accounts.add(account);
        return account;
    }

    /**
     * Recherche un compte par son identifiant (String).
     *
     * @param id identifiant numérique à comparer sous forme de chaîne
     * @return compte correspondant ou null s’il n’existe pas
     */
    public Account getAccountById(int id) {
        String stringId = String.valueOf(id);
        for (Account account : accounts) {
            if (account.getAccountID().equals(stringId)) {
                return account;
            }
        }
        return null;
    }

    /**
     * Supprime un compte existant selon son identifiant.
     *
     * @param id identifiant du compte à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    public boolean deleteAccount(int id) {
        String stringId = String.valueOf(id);
        Iterator<Account> iterator = accounts.iterator();
        while (iterator.hasNext()) {
            Account account = iterator.next();
            if (account.getAccountID().equals(stringId)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Met à jour les informations d’un compte existant.
     * Les champs non nuls du nouvel objet remplacent ceux du compte existant.
     *
     * @param id identifiant du compte à modifier
     * @param newAccount nouvelle version du compte
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateAccount(int id, Account newAccount) {
        String stringId = String.valueOf(id);
        for (Account account : accounts) {
            if (account.getAccountID().equals(stringId)) {
                if (newAccount.getType() != null) {
                    account.setType(newAccount.getType());
                }
                if (newAccount.getBalance() != 0) {
                    account.setBalance(newAccount.getBalance());
                }
                return true;
            }
        }
        return false;
    }

    // ===================== Gestion des transactions =====================

    /**
     * Retourne la liste de toutes les transactions enregistrées.
     *
     * @return liste des transactions
     */
    public List<Transaction> getAllTransactions() {
        return transactions;
    }

    /**
     * Ajoute une nouvelle transaction.
     *
     * @param transaction transaction à ajouter
     * @return transaction ajoutée
     */
    public Transaction addTransaction(Transaction transaction) {
        transactions.add(transaction);
        return transaction;
    }

    /**
     * Recherche une transaction par son identifiant.
     *
     * @param id identifiant numérique de la transaction
     * @return transaction correspondante ou null si absente
     */
    public Transaction getTransactionById(int id) {
        if (id >= 0 && id < transactions.size()) {
            return transactions.get(id);
        }
        return null;
    }

    /**
     * Supprime une transaction existante selon son identifiant.
     *
     * @param id identifiant de la transaction à supprimer
     * @return true si la transaction a été supprimée, false sinon
     */
    public boolean deleteTransaction(int id) {
        if (id >= 0 && id < transactions.size()) {
            transactions.remove(id);
            return true;
        }
        return false;
    }
}