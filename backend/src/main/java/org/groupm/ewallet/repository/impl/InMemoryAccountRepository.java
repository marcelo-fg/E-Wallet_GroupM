package org.groupm.ewallet.repository.impl;

import org.groupm.ewallet.repository.AccountRepository;
import org.groupm.ewallet.model.Account;

import java.util.*;

/**
 * Implémentation mémoire simple du repository Account pour la démo/tests.
 */
public class InMemoryAccountRepository implements AccountRepository {
    // Stockage en mémoire : id -> Account
    private final Map<String, Account> accounts = new HashMap<>();

    @Override
    public void save(Account account) {
        accounts.put(account.getAccountID(), account);
    }

    @Override
    public Account findById(String accountID) {
        return accounts.get(accountID);
    }

    @Override
    public void delete(String accountID) {
        accounts.remove(accountID);
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(accounts.values());
    }

    @Override
    public List<Account> findByUserId(String userID) {
        return accounts.values().stream()
                .filter(acc -> acc.getUserID() != null && acc.getUserID().equals(userID))
                .toList();
    }
}
