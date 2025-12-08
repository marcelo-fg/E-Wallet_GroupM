package org.groupm.ewallet.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.groupm.ewallet.repository.*;
import org.groupm.ewallet.repository.impl.*;

/**
 * CDI Producer for repositories.
 * Replaces BackendContext singleton pattern with proper dependency injection.
 */
@ApplicationScoped
public class RepositoryProducer {

    /**
     * Produces JPA UserRepository as CDI bean.
     * Singleton lifecycle managed by CDI container.
     */
    @Produces
    @ApplicationScoped
    public UserRepository produceUserRepository() {
        System.out.println("[RepositoryProducer] Creating JpaUserRepository...");
        return new JpaUserRepository();
    }

    /**
     * Produces JPA PortfolioRepository as CDI bean.
     */
    @Produces
    @ApplicationScoped
    public PortfolioRepository producePortfolioRepository() {
        System.out.println("[RepositoryProducer] Creating JpaPortfolioRepository...");
        return new JpaPortfolioRepository();
    }

    /**
     * Produces JPA AccountRepository for MySQL persistence.
     */
    @Produces
    @ApplicationScoped
    public AccountRepository produceAccountRepository() {
        System.out.println("[RepositoryProducer] Creating JpaAccountRepository...");
        return new JpaAccountRepository();
    }

    /**
     * Produces JPA TransactionRepository for MySQL persistence.
     */
    @Produces
    @ApplicationScoped
    public TransactionRepository produceTransactionRepository() {
        System.out.println("[RepositoryProducer] Creating JpaTransactionRepository...");
        return new JpaTransactionRepository();
    }

    /**
     * Produces JPA WealthTrackerRepository for MySQL persistence.
     */
    @Produces
    @ApplicationScoped
    public WealthTrackerRepository produceWealthTrackerRepository() {
        System.out.println("[RepositoryProducer] Creating JpaWealthTrackerRepository...");
        return new JpaWealthTrackerRepository();
    }

    /**
     * Produces JPA AssetRepository for MySQL persistence.
     */
    @Produces
    @ApplicationScoped
    public AssetRepository produceAssetRepository() {
        System.out.println("[RepositoryProducer] Creating JpaAssetRepository...");
        return new JpaAssetRepository();
    }
}
