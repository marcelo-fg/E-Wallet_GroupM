package org.example;

import org.example.model.*;
import org.example.service.*;

public class Main {
    public static void main(String[] args) {
        UserManager userManager = new UserManager();
        User marcelo = userManager.registerUser("U001", "marcelo@example.com", "1234", "Marcelo", "Gonçalves");

        AccountManager accountManager = new AccountManager();
        accountManager.importRevolutMockToUser(marcelo);
        accountManager.listUserAccounts(marcelo);
    }
}