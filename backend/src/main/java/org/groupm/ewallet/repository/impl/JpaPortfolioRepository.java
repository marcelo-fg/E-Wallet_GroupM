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
        // Retry logic to handle MySQL startup timing issues
        int maxRetries = 10;
        int retryCount = 0;
        EntityManagerFactory tempEmf = null;

        while (retryCount < maxRetries) {
            try {
                System.out.println("[JpaPortfolioRepository] Attempting to connect to MySQL (attempt "
                        + (retryCount + 1) + "/" + maxRetries + ")");
                tempEmf = Persistence.createEntityManagerFactory("ewalletPU");
                System.out.println("[JpaPortfolioRepository] Successfully connected to MySQL!");
                break;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    System.err.println(
                            "[JpaPortfolioRepository] Failed to connect to MySQL after " + maxRetries + " attempts");
                    throw new RuntimeException("Could not connect to MySQL database", e);
                }
                try {
                    long waitTime = 1000 * retryCount;
                    System.out.println(
                            "[JpaPortfolioRepository] Connection failed, waiting " + waitTime + "ms before retry...");
                    Thread.sleep(waitTime);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting to retry MySQL connection", ie);
                }
            }
        }
        this.emf = tempEmf;
    }

    @Override
    public void save(Portfolio portfolio) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (portfolio.getId() == 0) {
                em.persist(portfolio);
            } else {
                em.merge(portfolio);
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
            Portfolio portfolio = em.find(Portfolio.class, id);
            if (portfolio != null) {
                em.remove(portfolio);
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
