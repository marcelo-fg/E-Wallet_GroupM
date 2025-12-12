package org.groupm.ewallet.service.business;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.groupm.ewallet.model.*;
import org.groupm.ewallet.repository.impl.JpaUserRepository;
import org.groupm.ewallet.repository.impl.JpaPortfolioRepository;

import java.util.List;
import java.util.UUID;

/**
 * Service métier de gestion des utilisateurs et de leurs portefeuilles
 * d'investissement.
 * Supporte désormais plusieurs portefeuilles par utilisateur.
 * 
 * Toutes les méthodes de modification utilisent @Transactional pour garantir
 * ACID.
 */
@ApplicationScoped
public class UserManager {

    @Inject
    private JpaUserRepository userRepository;

    @Inject
    private JpaPortfolioRepository portfolioRepository;

    // =====================================================================
    // USERS
    // =====================================================================

    /**
     * Enregistre un nouvel utilisateur.
     * Génère un identifiant unique si absent.
     * Ne crée pas de portefeuille automatiquement (multi-portefeuilles).
     */
    @Transactional
    public User registerUser(User user) {

        if (user.getUserID() == null || user.getUserID().isBlank()) {
            user.setUserID(UUID.randomUUID().toString());
        }

        userRepository.save(user);
        return user;
    }

    /**
     * Authentifie un utilisateur selon email + mot de passe.
     * 
     * @return l'utilisateur si OK, sinon null
     */
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId);
    }

    @Transactional
    public boolean deleteUser(String userId) {
        User user = userRepository.findById(userId);
        if (user != null) {
            userRepository.delete(userId);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean updateUser(String userId, User newUser) {
        User user = userRepository.findById(userId);
        if (user != null) {
            if (newUser.getFirstName() != null)
                user.setFirstName(newUser.getFirstName());
            if (newUser.getLastName() != null)
                user.setLastName(newUser.getLastName());
            if (newUser.getEmail() != null)
                user.setEmail(newUser.getEmail());
            if (newUser.getPassword() != null)
                user.setPassword(newUser.getPassword());
            userRepository.save(user);
            return true;
        }
        return false;
    }

    // =====================================================================
    // PORTFOLIOS
    // =====================================================================

    /**
     * Ajoute un portefeuille ou met à jour un portefeuille existant.
     */
    @Transactional
    public Portfolio addOrUpdatePortfolio(Portfolio portfolio) {

        if (portfolio == null) {
            throw new IllegalArgumentException("Le portefeuille ne peut pas être null.");
        }

        // Mise à jour si le portefeuille existe déjà
        if (portfolio.getId() != 0) {
            Portfolio existing = portfolioRepository.findById(portfolio.getId());
            if (existing != null) {
                portfolioRepository.save(portfolio);
                return portfolio;
            }
        }

        // Sinon → création
        if (portfolio.getUserID() == null) {
            throw new IllegalArgumentException("UserID manquant pour ce portefeuille.");
        }

        portfolioRepository.save(portfolio);

        // Ajouter le portefeuille à l'utilisateur correspondant
        User user = userRepository.findById(portfolio.getUserID());
        if (user != null) {
            user.addPortfolio(portfolio);
            userRepository.save(user);
        }

        return portfolio;
    }

    /**
     * Récupère un portefeuille par son identifiant unique.
     */
    public Portfolio getPortfolioById(int id) {
        return portfolioRepository.findById(id);
    }

    /**
     * Récupère tous les portefeuilles.
     */
    public List<Portfolio> getAllPortfolios() {
        return portfolioRepository.findAll();
    }

    @Transactional
    public boolean deletePortfolio(int id) {
        Portfolio portfolio = portfolioRepository.findById(id);
        if (portfolio != null) {
            portfolioRepository.delete(id);
            return true;
        }
        return false;
    }

    // =====================================================================
    // WEALTH TRACKER
    // =====================================================================

    /**
     * Calcule la richesse totale d'un utilisateur en prenant en compte :
     * tous ses comptes bancaires
     * ainsi que la valeur totale de tous ses portefeuilles.
     */
    @Transactional
    public WealthTracker calculateWealthForUser(String userId) {

        User user = userRepository.findById(userId);
        if (user == null) {
            return null;
        }

        List<Portfolio> portfolios = portfolioRepository.findAllByUserId(userId);
        for (Portfolio p : portfolios) {
            p.recalculateTotalValue();
        }

        WealthTracker tracker = new WealthTracker(user);
        tracker.updateWealth();
        return tracker;
    }
}