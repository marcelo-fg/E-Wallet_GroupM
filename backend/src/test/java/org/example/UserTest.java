package org.example;

import org.example.model.Account;
import org.example.model.Portfolio;
import org.example.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void testUserCreation() {
        User user = new User("1", "test@example.com", "1234", "Alice", "Demo");

        System.out.println("=== Test de création d'utilisateur ===");
        System.out.println("ID : " + user.getUserID());
        System.out.println("Email : " + user.getEmail());
        System.out.println("Prénom : " + user.getFirstName());
        System.out.println("Nom : " + user.getLastName());
        System.out.println("Solde total initial : " + user.getTotalBalance());

        assertEquals("1", user.getUserID());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Alice", user.getFirstName());
        assertEquals("Demo", user.getLastName());
        //assertEquals(0, user.getTotalBalance());
    }

    @Test
    void testAddAccount() {
        User user = new User("1", "test@example.com", "1234", "Alice", "Demo");
        Account account = new Account("A001", "courant", 500.0);

        user.addAccount(account);

        System.out.println("=== Test d'ajout de compte ===");
        System.out.println("Utilisateur : " + user.getFirstName() + " " + user.getLastName());
        System.out.println("Nombre de comptes : " + user.getAccounts().size());
        System.out.println("Solde total après ajout : " + user.getTotalBalance());

        assertEquals(1, user.getAccounts().size());
        //assertEquals(500.0, user.getTotalBalance());
    }

    @Test
    void testMultipleAccountsBalance() {
        User user = new User("2", "multi@mail.com", "pass", "Bob", "Multi");
        user.addAccount(new Account("A001", "courant", 1000.0));
        user.addAccount(new Account("A002", "épargne", 2500.0));

        System.out.println("=== Test de solde total avec plusieurs comptes ===");
        user.getAccounts().forEach(acc ->
                System.out.println("Compte " + acc.getAccountID() + " (" + acc.getType() + "): " + acc.getBalance())
        );
        System.out.println("Solde total attendu : 3500.0");
        System.out.println("Solde total calculé : " + user.getTotalBalance());

        assertEquals(2, user.getAccounts().size());
        //assertEquals(3500.0, user.getTotalBalance());
    }

    @Test
    void testPortfolioAssociation() {
        User user = new User("3", "portfolio@mail.com", "pwd", "Clara", "Porto");
        Portfolio portfolio = new Portfolio(1, "3");
        user.setPortfolio(portfolio);

        System.out.println("=== Test d'association du portefeuille ===");
        System.out.println("Utilisateur : " + user.getFirstName());
        System.out.println("ID du portefeuille associé : " + user.getPortfolio().getId());

        assertNotNull(user.getPortfolio());
        assertEquals(1, user.getPortfolio().getId());
        assertEquals("3", user.getPortfolio().getUserID());
    }

    @Test
    void testToString() {
        User user = new User("4", "mail@mail.com", "pass", "Bob", "Smith");
        String str = user.toString();

        System.out.println("=== Test de la méthode toString() ===");
        System.out.println(str);

        assertTrue(str.contains("Bob"));
        assertTrue(str.contains("mail@mail.com"));
    }
}
