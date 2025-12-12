package org.groupm.ewallet.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * CDI Producer for a shared EntityManagerFactory singleton.
 * Replaces the multiple EMF instances that were being created per repository.
 * This ensures all repositories share the same connection pool and transaction
 * context.
 */
@ApplicationScoped
public class EntityManagerFactoryProducer {

    private EntityManagerFactory emf;

    @PostConstruct
    public void init() {
        // Retry logic to handle MySQL startup timing issues in Docker
        int maxRetries = 10;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                this.emf = Persistence.createEntityManagerFactory("ewalletPU");
                System.out.println("[EMF] EntityManagerFactory created successfully.");
                return;
            } catch (Exception e) {
                retryCount++;
                System.out.println("[EMF] Retry " + retryCount + "/" + maxRetries + " - waiting for database...");
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("Could not connect to MySQL database after " + maxRetries + " attempts",
                            e);
                }
                try {
                    Thread.sleep(1000 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting to retry MySQL connection", ie);
                }
            }
        }
    }

    @Produces
    @ApplicationScoped
    public EntityManagerFactory produceEntityManagerFactory() {
        return emf;
    }

    @PreDestroy
    public void cleanup() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("[EMF] EntityManagerFactory closed.");
        }
    }
}
