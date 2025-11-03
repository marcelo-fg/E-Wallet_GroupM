package org.example.service;

import org.example.model.Account;
import org.example.model.User;

import java.util.ArrayList;
import java.util.List;

public class AccountManager {

    private List<Account> accounts = new ArrayList<>();


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

    // ✅ Ajouté pour le WebService
    public Account createAccount(Account account) {
        accounts.add(account);
        return account;
    }
}