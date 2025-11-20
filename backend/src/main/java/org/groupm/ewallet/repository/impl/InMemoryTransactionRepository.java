package org.groupm.ewallet.repository.impl;

import org.groupm.ewallet.repository.TransactionRepository;
import org.groupm.ewallet.model.Transaction;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository mémoire pour Transaction.
 */
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<String, Transaction> transactions = new HashMap<>();

    @Override
    public void save(Transaction transaction) {
        transactions.put(transaction.getTransactionID(), transaction);
    }

    @Override
    public Transaction findById(String transactionID) {
        return transactions.get(transactionID);
    }

    @Override
    public void delete(String transactionID) {
        transactions.remove(transactionID);
    }

    @Override
    public List<Transaction> findAll() {
        return new ArrayList<>(transactions.values());
    }

    @Override
    public List<Transaction> findByAccountId(String accountID) {
        return transactions.values().stream()
                .filter(tx -> tx.getAccountID() != null && tx.getAccountID().equals(accountID))
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByType(String type) {
        return transactions.values().stream()
                .filter(tx -> tx.getType() != null && tx.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }
}
