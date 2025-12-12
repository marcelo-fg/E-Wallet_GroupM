package org.groupm.ewallet.repository.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.groupm.ewallet.model.Portfolio;
import org.groupm.ewallet.repository.PortfolioRepository;

import java.util.List;

/**
 * JPA implementation of PortfolioRepository.
 * Uses injected request-scoped EntityManager for proper transaction
 * coordination.
 */
@ApplicationScoped
public class JpaPortfolioRepository implements PortfolioRepository {

    @Inject
    private EntityManager em;

    @Override
    public void save(Portfolio portfolio) {
        if (portfolio.getId() == 0) {
            em.persist(portfolio);
        } else {
            em.merge(portfolio);
        }
    }

    @Override
    public Portfolio findById(int id) {
        return em.find(Portfolio.class, id);
    }

    @Override
    public List<Portfolio> findAll() {
        return em.createQuery("SELECT p FROM Portfolio p", Portfolio.class).getResultList();
    }

    @Override
    public void delete(int id) {
        Portfolio portfolio = em.find(Portfolio.class, id);
        if (portfolio != null) {
            em.remove(portfolio);
        }
    }

    @Override
    public List<Portfolio> findAllByUserId(String userID) {
        return em.createQuery("SELECT p FROM Portfolio p WHERE p.userID = :userID", Portfolio.class)
                .setParameter("userID", userID)
                .getResultList();
    }
}
