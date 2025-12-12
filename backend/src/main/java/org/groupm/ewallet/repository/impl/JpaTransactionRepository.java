package org.groupm.ewallet.repository.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.groupm.ewallet.model.Transaction;
import org.groupm.ewallet.repository.TransactionRepository;

import java.util.List;

/**
 * JPA implementation of TransactionRepository.
 * Uses injected request-scoped EntityManager for proper transaction
 * coordination.
 */
@ApplicationScoped
public class JpaTransactionRepository implements TransactionRepository {

    @Inject
    private EntityManager em;

    @Override
    public void save(Transaction transaction) {
        if (transaction.getTransactionID() == null || transaction.getTransactionID().isEmpty()) {
            em.persist(transaction);
        } else {
            Transaction existing = em.find(Transaction.class, transaction.getTransactionID());
            if (existing == null) {
                em.persist(transaction);
            } else {
                em.merge(transaction);
            }
        }
    }

    @Override
    public Transaction findById(String id) {
        return em.find(Transaction.class, id);
    }

    @Override
    public List<Transaction> findByAccountId(String accountId) {
        return em.createQuery(
                "SELECT t FROM Transaction t WHERE t.account.accountID = :accountId ORDER BY t.timestamp DESC",
                Transaction.class)
                .setParameter("accountId", accountId)
                .getResultList();
    }

    @Override
    public List<Transaction> findByType(String type) {
        return em.createQuery(
                "SELECT t FROM Transaction t WHERE t.type = :type ORDER BY t.timestamp DESC",
                Transaction.class)
                .setParameter("type", type)
                .getResultList();
    }

    @Override
    public List<Transaction> findAll() {
        return em.createQuery("SELECT t FROM Transaction t ORDER BY t.timestamp DESC", Transaction.class)
                .getResultList();
    }

    @Override
    public void delete(String id) {
        Transaction transaction = em.find(Transaction.class, id);
        if (transaction != null) {
            em.remove(transaction);
        }
    }
}
