package org.example;


import org.example.model.Account;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    @Test
    void testDeposit() {
        Account acc = new Account("A001", "épargne", 1000.0);
        double initialBalance = acc.getBalance();
        double depositAmount = 500.0;
        acc.deposit(depositAmount);

        System.out.println("Nouveau montant pour le compte " + acc.getAccountID()
                + " : " + initialBalance + " + " + depositAmount + " = " + acc.getBalance());

        assertEquals(1500.0, acc.getBalance());
    }

    @Test
    void testWithdraw() {
        Account acc = new Account("A002", "courant", 800.0);
        double initialBalance = acc.getBalance();
        double withdrawnAmount = 300.0;
        acc.withdraw(withdrawnAmount);
        System.out.println("Montant retiré du compte " + acc.getAccountID()
                + " : " + initialBalance + " - " + withdrawnAmount + " = " + acc.getBalance());
        assertEquals(500.0, acc.getBalance());
    }

    @Test
    void testWithdrawOverdraft() {
        Account acc = new Account("A003", "courant", 100.0);
        double initialBalance = acc.getBalance();
        double attemptedWithdraw = 200.0;
        acc.withdraw(attemptedWithdraw); // devrait être refusé
        System.out.println("Tentative de retrait invalide sur le compte " + acc.getAccountID()
                + " : solde inchangé = " + acc.getBalance());
        assertEquals(100.0, acc.getBalance());
    }

    @Test
    void testAddTransaction() {
        Account acc = new Account("A004", "épargne", 500.0);
        acc.deposit(200);
        System.out.println("Transaction ajoutée sur le compte " + acc.getAccountID()
                + " : nombre de transactions = " + acc.getTransactions().size());
        assertEquals(1, acc.getTransactions().size());
    }
}