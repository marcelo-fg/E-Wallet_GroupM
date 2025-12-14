package org.groupm.ewallet;

import org.groupm.ewallet.model.Transaction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest {

    @Test
    void testTransactionCreation() {
        Transaction txn = new Transaction("TXN-1", "deposit", 150.0, "Dépôt initial");

        System.out.println("=== Test de création de transaction ===");
        System.out.println("ID : " + txn.getTransactionID());
        System.out.println("Type : " + txn.getType());
        System.out.println("Montant : " + txn.getAmount());
        System.out.println("Description : " + txn.getDescription());
        System.out.println("Horodatage : " + txn.getFormattedTimestamp());

        assertEquals("TXN-1", txn.getTransactionID());
        assertEquals("deposit", txn.getType());
        assertEquals(150.0, txn.getAmount());
        assertEquals("Dépôt initial", txn.getDescription());
        assertNotNull(txn.getTimestamp());
        assertTrue(txn.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testTransactionWithAccountID() {
        Transaction txn = new Transaction("TXN-2", "withdraw", 75.0, "Retrait guichet");
        org.groupm.ewallet.model.Account account = new org.groupm.ewallet.model.Account();
        account.setAccountID("A001");
        txn.setAccount(account);

        System.out.println("=== Test de transaction avec accountID ===");
        System.out.println("ID : " + txn.getTransactionID());
        System.out.println("Compte associé : " + txn.getAccountID());
        System.out.println("Type : " + txn.getType());
        System.out.println("Montant : " + txn.getAmount());
        System.out.println("Description : " + txn.getDescription());

        assertEquals("TXN-2", txn.getTransactionID());
        assertEquals("withdraw", txn.getType());
        assertEquals(75.0, txn.getAmount());
        assertEquals("Retrait guichet", txn.getDescription());
        assertEquals("A001", txn.getAccountID());
        assertNotNull(txn.getTimestamp());
    }

    @Test
    void testFormattedTimestamp() {
        Transaction txn = new Transaction("TXN-3", "deposit", 50.0, "Test format date");
        String formatted = txn.getFormattedTimestamp();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        java.time.LocalDate parsed = java.time.LocalDate.parse(formatted, formatter);

        System.out.println("=== Test de formatage de la date ===");
        System.out.println("Horodatage formaté : " + formatted);

        assertNotNull(formatted);
        assertEquals(parsed.format(formatter), formatted);
    }

    @Test
    void testToStringMethod() {
        Transaction txn = new Transaction("TXN-4", "deposit", 200.0, "Test affichage", "A002");
        String output = txn.toString();

        System.out.println("=== Test de la méthode toString() ===");
        System.out.println(output);

        // New toString format: [TIMESTAMP] TYPE | Montant: AMOUNT
        assertTrue(output.contains("DEPOSIT") || output.contains("deposit"));
        assertTrue(output.contains("200.0"));
    }
}
