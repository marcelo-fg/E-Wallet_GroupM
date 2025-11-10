package org.example;

import org.example.model.Transaction;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {

    @Test
    void testTransactionCreation() {
        Transaction txn = new Transaction("TXN-1", "deposit", 150.0, "Dépôt initial");

        assertEquals("TXN-1", txn.getTransactionID());
        assertEquals("deposit", txn.getType());
        assertEquals(150.0, txn.getAmount());
        assertEquals("Dépôt initial", txn.getDescription());
        assertNotNull(txn.getTimestamp());
        assertTrue(txn.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}