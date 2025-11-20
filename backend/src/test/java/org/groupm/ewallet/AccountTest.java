package org.groupm.ewallet;

import org.groupm.ewallet.model.Account;
import org.groupm.ewallet.model.Transaction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    @Test
    void testAccountCreation() {
        Account acc = new Account("A001", "courant", 1000.0);

        assertEquals("A001", acc.getAccountID());
        assertEquals("courant", acc.getType());
        assertEquals(1000.0, acc.getBalance());
        assertNotNull(acc.getTransactions());
        assertTrue(acc.getTransactions().isEmpty());
    }

    @Test
    void testSetters() {
        Account acc = new Account();

        acc.setAccountID("A002");
        acc.setUserID("U99");
        acc.setType("épargne");
        acc.setBalance(2500.0);

        assertEquals("A002", acc.getAccountID());
        assertEquals("U99", acc.getUserID());
        assertEquals("épargne", acc.getType());
        assertEquals(2500.0, acc.getBalance());
    }

    @Test
    void testAddTransaction() {
        Account acc = new Account("A003", "courant", 500.0);

        Transaction tx = new Transaction(
                "TXN001",
                "deposit",
                200.0,
                "Initial deposit",
                "A003"
        );

        acc.addTransaction(tx);

        assertEquals(1, acc.getTransactions().size());
        assertEquals("TXN001", acc.getTransactions().get(0).getTransactionID());
        assertEquals(200.0, acc.getTransactions().get(0).getAmount());
    }
}