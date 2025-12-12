package org.groupm.ewallet.repository.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.groupm.ewallet.model.Account;
import org.groupm.ewallet.repository.AccountRepository;

import java.util.List;

/**
 * JPA implementation of AccountRepository.
 * Uses injected request-scoped EntityManager for proper transaction
 * coordination.
 */
@ApplicationScoped
public class JpaAccountRepository implements AccountRepository {

    @Inject
    private EntityManager em;

    @Override
    public void save(Account account) {
        if (account.getAccountID() == null || account.getAccountID().isEmpty()) {
            em.persist(account);
        } else {
            Account existing = em.find(Account.class, account.getAccountID());
            if (existing == null) {
                em.persist(account);
            } else {
                em.merge(account);
            }
        }
    }

    @Override
    public Account findById(String id) {
        return em.find(Account.class, id);
    }

    @Override
    public List<Account> findAll() {
        return em.createQuery("SELECT a FROM Account a", Account.class).getResultList();
    }

    @Override
    public void delete(String id) {
        Account account = em.find(Account.class, id);
        if (account != null) {
            em.remove(account);
        }
    }

    @Override
    public List<Account> findByUserId(String userId) {
        return em.createQuery("SELECT a FROM Account a WHERE a.userID = :userId", Account.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
