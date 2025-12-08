package org.groupm.ewallet.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.groupm.ewallet.model.Account;
import org.groupm.ewallet.repository.AccountRepository;

import java.util.List;

/**
 * JPA implementation of AccountRepository.
 */
public class JpaAccountRepository implements AccountRepository {

    private final EntityManagerFactory emf;

    public JpaAccountRepository() {
        this.emf = Persistence.createEntityManagerFactory("ewalletPU");
        System.out.println("[JpaAccountRepository] EntityManagerFactory created successfully");
    }

    @Override
    public void save(Account account) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (account.getAccountID() == null || account.getAccountID().isEmpty()) {
                em.persist(account);
            } else {
                em.merge(account);
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
    public Account findById(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Account.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Account> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT a FROM Account a", Account.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Account account = em.find(Account.class, id);
            if (account != null) {
                em.remove(account);
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
    public List<Account> findByUserId(String userId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT a FROM Account a WHERE a.userID = :userId", Account.class)
                    .setParameter("userId", userId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
