package org.example.service;

import org.example.model.Account;
import org.example.model.User;
import org.example.model.Transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AccountManager {

    private List<Account> accounts = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();

    public AccountManager() {
        populate();
    }

    public void listUserAccounts(User user) {
        System.out.println("\n📘 Comptes de " + user.getFirstName() + " :");
        for (Account acc : user.getAccounts()) {
            System.out.println(acc);
        }
        System.out.println("Solde total : " + user.getTotalBalance() + " CHF");
    }

    public void populate(){
        accounts.add(new Account("1", "BCV", 20000.00));
        accounts.add(new Account("2", "UBS", 990.00));
        accounts.add(new Account("3", "Santander", 18000.00));
        accounts.add(new Account("4", "BCGE", 3450.00));
        accounts.add(new Account("5", "Pictet", 300000000.00));

    }

    // ✅ Ajouté pour le WebService
    public List<Account> getAllAccounts() {
        return accounts;
    }

    // ✅ Renommé pour le WebService
    public Account addAccount(Account account) {
        accounts.add(account);
        return account;
    }

    // ✅ Ajouté pour récupérer un compte par identifiant
    public Account getAccountById(int id) {
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            // Utilisation de getAccountID() si disponible, sinon comparaison par index
            // if (account.getAccountID() == id) {
            if (i == id) {
                return account;
            }
        }
        return null;
    }

    // ✅ Ajouté pour supprimer un compte
    public boolean deleteAccount(int id) {
        //delete account with AccountId is a string
        //string equal account id
        String stringId = String.valueOf(id); // conversion en String
        for (int i = 0; i < accounts.size(); i++) {
            Account account = accounts.get(i);
            if (account.getAccountID().equals(stringId)) {
                accounts.remove(i); // suppression
                return true;        // ✅ trouvé et supprimé
            }
        }
        return false; // ❌ non trouvé


    }

    public List<Transaction> getAllTransactions() {
        return transactions;
    }

    public Transaction addTransaction(Transaction transaction) {
        transactions.add(transaction);
        return transaction;
    }

    public Transaction getTransactionById(int id) {
        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            // Utilisation de getTransactionID() si disponible, sinon comparaison par index
            // if (transaction.getTransactionID() == id) {
            if (i == id) {
                return transaction;
            }
        }
        return null;
    }

    public boolean deleteTransaction(int id) {
        Iterator<Transaction> iterator = transactions.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Transaction transaction = iterator.next();
            // Utilisation de getTransactionID() si disponible, sinon comparaison par index
            // if (transaction.getTransactionID() == id) {
            if (index == id) {
                iterator.remove();
                return true;
            }
            index++;
        }
        return false;
    }

    public boolean updateAccount(int id, Account newAccount) {
        String stringId = String.valueOf(id);
        for (Account account : accounts) {
            if (account.getAccountID().equals(stringId)) {
                // Mise à jour des champs existants si non null
                if (newAccount.getType() != null) account.setType(newAccount.getType());
                if (newAccount.getBalance() != 0) account.setBalance(newAccount.getBalance());
                return true; // ✅ compte trouvé et modifié
            }
        }
        return false; // ❌ aucun compte trouvé avec cet ID
    }
}