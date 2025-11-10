package org.example;


import org.example.model.Account;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    @Test
    void testDeposit() {
        Account acc = new Account("A001", "épargne", 1000.0);
        acc.deposit(500.0);
        assertEquals(1500.0, acc.getBalance());
    }

    @Test
    void testWithdraw() {
        Account acc = new Account("A002", "courant", 800.0);
        acc.withdraw(300.0);
        assertEquals(500.0, acc.getBalance());
    }

    @Test
    void testWithdrawOverdraft() {
        Account acc = new Account("A003", "courant", 100.0);
        acc.withdraw(200.0); // devrait être refusé
        assertEquals(100.0, acc.getBalance());
    }

    @Test
    void testAddTransaction() {
        Account acc = new Account("A004", "épargne", 500.0);
        acc.deposit(200);
        assertEquals(1, acc.getTransactions().size());
    }
}