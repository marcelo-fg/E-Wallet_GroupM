package org.groupm.ewallet;

import org.groupm.ewallet.model.Account;
import org.groupm.ewallet.model.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class AccountTest {

    @Test
    void testAccountCreation() {
        Account acc = new Account("A001", "courant", 1000.0);

        assertEquals("A001", acc.getAccountID());
        assertEquals("courant", acc.getType());
        assertEquals(BigDecimal.valueOf(1000.0), acc.getBalanceAsBigDecimal());
        assertNotNull(acc.getTransactions());
        assertTrue(acc.getTransactions().isEmpty());
    }

    @Test
    void testAccountCreationWithBigDecimal() {
        Account acc = new Account("A001", "courant", new BigDecimal("1000.50"));

        assertEquals("A001", acc.getAccountID());
        assertEquals("courant", acc.getType());
        assertEquals(new BigDecimal("1000.50"), acc.getBalanceAsBigDecimal());
    }

    @Test
    void testSetters() {
        Account acc = new Account();

        acc.setAccountID("A002");
        acc.setUserID("U99");
        acc.setType("épargne");
        acc.setBalance(new BigDecimal("2500.00"));

        assertEquals("A002", acc.getAccountID());
        assertEquals("U99", acc.getUserID());
        assertEquals("épargne", acc.getType());
        assertEquals(new BigDecimal("2500.00"), acc.getBalanceAsBigDecimal());
    }

    @Test
    void testAddTransaction() {
        Account acc = new Account("A003", "courant", 500.0);

        Transaction tx = new Transaction(
                "TXN001",
                "deposit",
                200.0,
                "Initial deposit",
                "A003");

        acc.addTransaction(tx);

        assertEquals(1, acc.getTransactions().size());
        assertEquals("TXN001", acc.getTransactions().get(0).getTransactionID());
        assertEquals(BigDecimal.valueOf(200.0), acc.getTransactions().get(0).getAmountAsBigDecimal());
    }

    @Test
    void testBigDecimalPrecision() {
        Account acc = new Account();
        // Test that BigDecimal handles precision correctly
        acc.setBalance(new BigDecimal("0.1"));
        BigDecimal addition = acc.getBalanceAsBigDecimal().add(new BigDecimal("0.2"));

        // This should equal 0.3 exactly with BigDecimal (unlike double)
        assertEquals(new BigDecimal("0.3"), addition);
    }
}