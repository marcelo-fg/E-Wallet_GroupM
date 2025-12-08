package org.groupm.ewallet.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.groupm.ewallet.model.User;
import org.groupm.ewallet.repository.UserRepository;

import java.util.List;

/**
 * Implémentation JPA du UserRepository.
 */
public class JpaUserRepository implements UserRepository {

    private final EntityManagerFactory emf;

    public JpaUserRepository() {
        // Retry logic to handle MySQL startup timing issues
        int maxRetries = 10;
        int retryCount = 0;
        EntityManagerFactory tempEmf = null;

        while (retryCount < maxRetries) {
            try {
                System.out.println("[JpaUserRepository] Attempting to connect to MySQL (attempt " + (retryCount + 1)
                        + "/" + maxRetries + ")");
                tempEmf = Persistence.createEntityManagerFactory("ewalletPU");
                System.out.println("[JpaUserRepository] Successfully connected to MySQL!");
                break;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    System.err.println(
                            "[JpaUserRepository] Failed to connect to MySQL after " + maxRetries + " attempts");
                    throw new RuntimeException("Could not connect to MySQL database", e);
                }
                try {
                    long waitTime = 1000 * retryCount; // Exponential backoff
                    System.out.println(
                            "[JpaUserRepository] Connection failed, waiting " + waitTime + "ms before retry...");
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
    public User findByEmail(String email) {
        EntityManager em = emf.createEntityManager();
        try {
            List<User> users = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                    .setParameter("email", email)
                    .getResultList();
            return users.isEmpty() ? null : users.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public User findById(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public void save(User user) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (em.find(User.class, user.getUserID()) == null) {
                em.persist(user);
            } else {
                em.merge(user);
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
    public void delete(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            User user = em.find(User.class, id);
            if (user != null) {
                em.remove(user);
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
    public List<User> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u", User.class).getResultList();
        } finally {
            em.close();
        }
    }
}
