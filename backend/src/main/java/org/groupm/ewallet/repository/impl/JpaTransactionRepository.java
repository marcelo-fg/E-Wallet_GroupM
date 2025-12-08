package org.groupm.ewallet.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.groupm.ewallet.model.Transaction;
import org.groupm.ewallet.repository.TransactionRepository;

import java.util.List;

/**
 * JPA implementation of TransactionRepository.
 */
public class JpaTransactionRepository implements TransactionRepository {

    private final EntityManagerFactory emf;

    public JpaTransactionRepository() {
        this.emf = Persistence.createEntityManagerFactory("ewalletPU");
        System.out.println("[JpaTransactionRepository] EntityManagerFactory created successfully");
    }

    @Override
    public void save(Transaction transaction) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (transaction.getTransactionID() == null || transaction.getTransactionID().isEmpty()) {
                em.persist(transaction);
            } else {
                em.merge(transaction);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Transaction findById(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Transaction.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Transaction> findByAccountId(String accountId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em
                    .createQuery("SELECT t FROM Transaction t WHERE t.accountID = :accountId ORDER BY t.timestamp DESC",
                            Transaction.class)
                    .setParameter("accountId", accountId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Transaction> findByType(String type) {
        EntityManager em = emf.createEntityManager();
        try {
            return em
                    .createQuery("SELECT t FROM Transaction t WHERE t.type = :type ORDER BY t.timestamp DESC",
                            Transaction.class)
                    .setParameter("type", type)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Transaction> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT t FROM Transaction t ORDER BY t.timestamp DESC", Transaction.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Transaction transaction = em.find(Transaction.class, id);
            if (transaction != null) {
                em.remove(transaction);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
