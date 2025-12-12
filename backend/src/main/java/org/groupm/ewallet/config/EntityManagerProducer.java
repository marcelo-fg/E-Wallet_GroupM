package org.groupm.ewallet.config;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

/**
 * CDI Producer for request-scoped EntityManager instances.
 * Each HTTP request gets a single shared EntityManager that all repositories
 * use.
 * This ensures transaction coordination within a single request and enables
 * proper @Transactional behavior.
 */
public class EntityManagerProducer {

    @Inject
    private EntityManagerFactory emf;

    /**
     * Produces a request-scoped EntityManager.
     * All repository operations within the same request will share this EM,
     * enabling proper transaction coordination.
     */
    @Produces
    @RequestScoped
    public EntityManager produceEntityManager() {
        EntityManager em = emf.createEntityManager();
        System.out.println("[EM] Created request-scoped EntityManager: " + em.hashCode());
        return em;
    }

    /**
     * Disposes of the EntityManager when the request scope ends.
     * Ensures any uncommitted transaction is rolled back and the EM is closed.
     */
    public void disposeEntityManager(@Disposes EntityManager em) {
        if (em != null && em.isOpen()) {
            if (em.getTransaction().isActive()) {
                System.out.println("[EM] Rolling back active transaction before disposal");
                em.getTransaction().rollback();
            }
            em.close();
            System.out.println("[EM] Disposed EntityManager: " + em.hashCode());
        }
    }
}
