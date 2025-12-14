package org.groupm.ewallet.repository.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.groupm.ewallet.model.PortfolioTransaction;
import org.groupm.ewallet.repository.PortfolioTransactionRepository;

import java.util.List;

/**
 * JPA implementation of PortfolioTransactionRepository.
 */
@ApplicationScoped
public class JpaPortfolioTransactionRepository implements PortfolioTransactionRepository {

    @Inject
    private EntityManager em;

    @Override
    public void save(PortfolioTransaction transaction) {
        if (transaction.getId() == 0) {
            em.persist(transaction);
        } else {
            em.merge(transaction);
        }
    }

    @Override
    public PortfolioTransaction findById(long id) {
        return em.find(PortfolioTransaction.class, id);
    }

    @Override
    public List<PortfolioTransaction> findByPortfolioId(int portfolioId) {
        return em.createQuery(
                "SELECT pt FROM PortfolioTransaction pt WHERE pt.portfolioId = :portfolioId ORDER BY pt.timestamp DESC",
                PortfolioTransaction.class)
                .setParameter("portfolioId", portfolioId)
                .getResultList();
    }

    @Override
    public List<PortfolioTransaction> findBySymbol(String symbol) {
        return em.createQuery(
                "SELECT pt FROM PortfolioTransaction pt WHERE pt.symbol = :symbol ORDER BY pt.timestamp DESC",
                PortfolioTransaction.class)
                .setParameter("symbol", symbol)
                .getResultList();
    }

    @Override
    public List<PortfolioTransaction> findAll() {
        return em.createQuery(
                "SELECT pt FROM PortfolioTransaction pt ORDER BY pt.timestamp DESC",
                PortfolioTransaction.class)
                .getResultList();
    }

    @Override
    public void delete(long id) {
        PortfolioTransaction transaction = em.find(PortfolioTransaction.class, id);
        if (transaction != null) {
            em.remove(transaction);
        }
    }
}
