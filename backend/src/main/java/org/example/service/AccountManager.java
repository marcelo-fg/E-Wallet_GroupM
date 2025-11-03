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

    public void listUserAccounts(User user) {
        System.out.println("\n📘 Comptes de " + user.getFirstName() + " :");
        for (Account acc : user.getAccounts()) {
            System.out.println(acc);
        }
        System.out.println("Solde total : " + user.getTotalBalance() + " CHF");
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
        Iterator<Account> iterator = accounts.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Account account = iterator.next();
            // Utilisation de getAccountID() si disponible, sinon comparaison par index
            // if (account.getAccountID() == id) {
            if (index == id) {
                iterator.remove();
                return true;
            }
            index++;
        }
        return false;
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
}