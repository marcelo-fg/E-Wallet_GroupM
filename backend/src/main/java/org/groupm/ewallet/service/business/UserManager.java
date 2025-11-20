package org.groupm.ewallet.service.business;

import org.groupm.ewallet.model.User;
import org.groupm.ewallet.model.Portfolio;
import org.groupm.ewallet.model.WealthTracker;
import org.groupm.ewallet.model.Asset;
import org.groupm.ewallet.repository.UserRepository;
import org.groupm.ewallet.repository.PortfolioRepository;

import java.util.List;

/**
 * Service métier de gestion des utilisateurs et portefeuilles.
 * Permet l’enregistrement, l’authentification, la modification, la suppression
 * et le calcul de richesse totale via abstraction repository.
 */
public class UserManager {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    /**
     * Constructeur avec injection des repositories métiers.
     * @param userRepository       repository des utilisateurs
     * @param portfolioRepository  repository des portefeuilles
     */
    public UserManager(UserRepository userRepository, PortfolioRepository portfolioRepository) {
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
    }

    /**
     * Enregistre un nouvel utilisateur.
     * @param user utilisateur à créer (sans mot de passe en clair : à encoder/hacher côté service)
     * @return utilisateur créé
     */
    public User registerUser(User user) {
        // Ici, ajoute une gestion pro du mot de passe : hash/encoder avant .save
        userRepository.save(user);
        return user;
    }

    /**
     * Authentifie un utilisateur via son email et son mot de passe.
     * @param email email utilisateur
     * @param password mot de passe (idealement hashé)
     * @return utilisateur si connecté, null sinon
     */
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) { // hash-verif ici en prod
            return user;
        }
        return null;
    }

    /**
     * Retourne tous les utilisateurs enregistrés.
     * @return liste d’utilisateurs
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Recherche un utilisateur par identifiant.
     * @param userId identifiant (String pour homogénéité)
     * @return utilisateur ou null
     */
    public User getUserById(String userId) {
        return userRepository.findById(userId);
    }

    /**
     * Supprime un utilisateur via son identifiant.
     * @param userId identifiant
     * @return true si supprimé, false sinon
     */
    public boolean deleteUser(String userId) {
        User user = userRepository.findById(userId);
        if (user != null) {
            userRepository.delete(userId);
            return true;
        }
        return false;
    }

    /**
     * Met à jour les champs d’un utilisateur existant.
     * Ne modifie que les champs non nuls dans l’objet newUser.
     * @param userId identifiant à mettre à jour
     * @param newUser modèle des champs à modifier
     * @return true si mise à jour réussie
     */
    public boolean updateUser(String userId, User newUser) {
        User user = userRepository.findById(userId);
        if (user != null) {
            if (newUser.getFirstName() != null) user.setFirstName(newUser.getFirstName());
            if (newUser.getLastName() != null) user.setLastName(newUser.getLastName());
            if (newUser.getEmail() != null) user.setEmail(newUser.getEmail());
            if (newUser.getPassword() != null) user.setPassword(newUser.getPassword()); // encode ici en prod
            userRepository.save(user);
            return true;
        }
        return false;
    }

    /**
     * Retourne tous les portefeuilles enregistrés.
     * @return liste portefeuilles
     */
    public List<Portfolio> getAllPortfolios() {
        // Si besoin d’une liste complète, expose le repo
        // Sinon, filtre par utilisateur via repo dédié
        // portfolioRepository.findByUserId(userId)
        throw new UnsupportedOperationException("Non implémenté ici, expose via PortfolioManager");
    }

    /**
     * Recherche un portefeuille par ID pour un utilisateur.
     * @param id identifiant portefeuille
     * @return portefeuille or null
     */
    public Portfolio getPortfolioById(int id) {
        return portfolioRepository.findById(id);
    }

    /**
     * Ajoute/modifie un portefeuille pour un utilisateur donné.
     * @param portfolio portefeuille à enregistrer
     * @return portefeuille créé/mis à jour
     */
    public Portfolio addOrUpdatePortfolio(Portfolio portfolio) {
        portfolioRepository.save(portfolio);
        return portfolio;
    }

    /**
     * Supprime un portefeuille par identifiant.
     * @param id identifiant portefeuille
     * @return true si suppresssion ok
     */
    public boolean deletePortfolio(int id) {
        Portfolio portfolio = portfolioRepository.findById(id);
        if (portfolio != null) {
            portfolioRepository.delete(id);
            return true;
        }
        return false;
    }

    /**
     * Calcule la richesse totale pour un utilisateur identifié.
     * Agrège la valeur de tous les actifs et comptes.
     * @param userId identifiant du user
     * @return WealthTracker métier (calculé)
     */
    public WealthTracker calculateWealthForUser(String userId) {
        User user = userRepository.findById(userId);
        if (user == null) return null;
        Portfolio portfolio = portfolioRepository.findByUserId(userId);

        double totalWealth = 0.0;
        if (portfolio != null) {
            for (Asset asset : portfolio.getAssets()) {
                totalWealth += asset.getUnitValue() * asset.getQuantity();
            }
        }
        // Ajoute le total des comptes si voulu :
        totalWealth += user.getTotalBalance();

        WealthTracker tracker = new WealthTracker(user);
        tracker.updateWealth(); // Met à jour toutes les données calculables
        return tracker;
    }
}
