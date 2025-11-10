package org.example;
import org.example.model.Account;
import org.example.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class UserTest {
    @Test
    void testUserCreation() {
        User user = new User("1", "test@example.com", "1234", "Alice", "Demo");

        assertEquals("1", user.getUserID());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Alice", user.getFirstName());
        assertEquals("Demo", user.getLastName());
        assertEquals(0, user.getTotalBalance());
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
    void testToString() {
        User user = new User("1", "mail@mail.com", "pass", "Bob", "Smith");
        String str = user.toString();
        assertTrue(str.contains("Bob"));
        assertTrue(str.contains("mail@mail.com"));
    }
}
