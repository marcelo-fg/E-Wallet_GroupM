package org.groupm.ewallet.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.groupm.ewallet.model.Portfolio;
import org.groupm.ewallet.repository.PortfolioRepository;

import java.util.List;

/**
 * Implémentation JPA du PortfolioRepository.
 */
public class JpaPortfolioRepository implements PortfolioRepository {

    private final EntityManagerFactory emf;

    public JpaPortfolioRepository() {
        this.emf = Persistence.createEntityManagerFactory("ewalletPU");
    }

    @Override
    public void save(Portfolio portfolio) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (portfolio.getId() == 0) {
                em.persist(portfolio);
            } else {
                if (em.find(Portfolio.class, portfolio.getId()) == null) {
                    em.persist(portfolio);
                } else {
                    em.merge(portfolio);
                }
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
    public Portfolio findById(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Portfolio.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Portfolio> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Portfolio p", Portfolio.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Portfolio p = em.find(Portfolio.class, id);
            if (p != null) {
                em.remove(p);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Portfolio> findAllByUserId(String userID) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Portfolio p WHERE p.userID = :userID", Portfolio.class)
                    .setParameter("userID", userID)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
