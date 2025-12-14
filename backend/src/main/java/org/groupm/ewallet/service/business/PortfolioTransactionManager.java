package org.groupm.ewallet.service.business;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.groupm.ewallet.model.PortfolioTransaction;
import org.groupm.ewallet.repository.impl.JpaPortfolioTransactionRepository;

import java.util.List;

/**
 * Business service for managing portfolio transactions (BUY/SELL).
 */
@ApplicationScoped
public class PortfolioTransactionManager {

    @Inject
    private JpaPortfolioTransactionRepository transactionRepository;

    /**
     * Records a new portfolio transaction (BUY or SELL).
     */
    @Transactional
    public PortfolioTransaction recordTransaction(PortfolioTransaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null.");
        }
        if (transaction.getPortfolioId() <= 0) {
            throw new IllegalArgumentException("Portfolio ID is required.");
        }
        if (transaction.getSymbol() == null || transaction.getSymbol().isBlank()) {
            throw new IllegalArgumentException("Symbol is required.");
        }
        if (transaction.getType() == null || transaction.getType().isBlank()) {
            throw new IllegalArgumentException("Transaction type (BUY/SELL) is required.");
        }

        transactionRepository.save(transaction);
        return transaction;
    }

    /**
     * Gets all transactions for a specific portfolio.
     */
    public List<PortfolioTransaction> getTransactionsByPortfolioId(int portfolioId) {
        return transactionRepository.findByPortfolioId(portfolioId);
    }

    /**
     * Gets all portfolio transactions.
     */
    public List<PortfolioTransaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Gets a transaction by its ID.
     */
    public PortfolioTransaction getTransactionById(long id) {
        return transactionRepository.findById(id);
    }

    /**
     * Deletes a transaction by its ID.
     */
    @Transactional
    public boolean deleteTransaction(long id) {
        PortfolioTransaction existing = transactionRepository.findById(id);
        if (existing != null) {
            transactionRepository.delete(id);
            return true;
        }
        return false;
    }
}
