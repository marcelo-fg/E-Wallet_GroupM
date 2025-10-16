package org.example.service;

import org.example.model.Account;
import org.example.model.User;

import java.util.List;

public class AccountManager {
    public void importRevolutMockToUser(User user) {
        org.example.service.connector.RevolutSandboxConnector connector = new org.example.service.connector.RevolutSandboxConnector();
        List<Account> accounts = connector.loadMockAccounts();

        for (Account acc : accounts) {
            user.addAccount(acc);
        }

        System.out.println("Comptes Revolut mock importés pour " + user.getFirstName());
    }

    public void listUserAccounts(User user) {
        System.out.println("\n📘 Comptes de " + user.getFirstName() + " :");
        for (Account acc : user.getAccounts()) {
            System.out.println(acc);
        }
        System.out.println("Solde total : " + user.getTotalBalance() + " CHF");
    }
}