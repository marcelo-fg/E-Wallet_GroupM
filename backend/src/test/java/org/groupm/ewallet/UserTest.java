package org.groupm.ewallet;

import org.groupm.ewallet.model.Account;
import org.groupm.ewallet.model.Portfolio;
import org.groupm.ewallet.model.User;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void testUserCreation() {
        User user = new User("1", "test@example.com", "1234", "Alice", "Demo");

        assertEquals("1", user.getUserID());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Alice", user.getFirstName());
        assertEquals("Demo", user.getLastName());
        assertEquals(0.0, user.getTotalBalance());
        assertTrue(user.getPortfolios().isEmpty());
    }

    @Test
    void testAddAccount() {
        User user = new User("1", "test@example.com", "1234", "Alice", "Demo");
        Account account = new Account("A001", "courant", 500.0);

        user.addAccount(account);

        assertEquals(1, user.getAccounts().size());
        assertEquals(500.0, user.getTotalBalance());
    }

    @Test
    void testMultipleAccountsBalance() {
        User user = new User("2", "multi@mail.com", "pass", "Bob", "Multi");
        user.addAccount(new Account("A001", "courant", 1000.0));
        user.addAccount(new Account("A002", "épargne", 2500.0));

        assertEquals(2, user.getAccounts().size());
        assertEquals(3500.0, user.getTotalBalance());
    }

    @Test
    void testPortfolioAssociation() {
        User user = new User("3", "portfolio@mail.com", "pwd", "Clara", "Porto");

        Portfolio portfolio = new Portfolio("3");
        portfolio.setId(1); // forcer l'ID pour le test

        user.addPortfolio(portfolio);

        // Vérification
        List<Portfolio> portfolios = user.getPortfolios();

        assertEquals(1, portfolios.size());
        assertEquals(1, portfolios.get(0).getId());
        assertEquals("3", portfolios.get(0).getUserID());
    }

    @Test
    void testToString() {
        User user = new User("4", "mail@mail.com", "pass", "Bob", "Smith");

        String str = user.toString();

        assertTrue(str.contains("Bob"));
        assertTrue(str.contains("mail@mail.com"));
    }
}