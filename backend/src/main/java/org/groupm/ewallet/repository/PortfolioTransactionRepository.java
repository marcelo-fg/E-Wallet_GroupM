package org.groupm.ewallet.repository;

import org.groupm.ewallet.model.PortfolioTransaction;
import java.util.List;

/**
 * Repository interface for PortfolioTransaction persistence operations.
 */
public interface PortfolioTransactionRepository {

    /**
     * Saves a portfolio transaction.
     */
    void save(PortfolioTransaction transaction);

    /**
     * Finds a transaction by its ID.
     */
    PortfolioTransaction findById(long id);

    /**
     * Finds all transactions for a given portfolio.
     */
    List<PortfolioTransaction> findByPortfolioId(int portfolioId);

    /**
     * Finds all transactions for a given symbol across all portfolios.
     */
    List<PortfolioTransaction> findBySymbol(String symbol);

    /**
     * Finds all transactions.
     */
    List<PortfolioTransaction> findAll();

    /**
     * Deletes a transaction by its ID.
     */
    void delete(long id);
}
