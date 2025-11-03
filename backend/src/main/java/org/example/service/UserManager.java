package org.example.service;

import org.example.model.User;
import org.example.model.Portfolio;
import org.example.model.WealthTracker;
import org.example.model.Asset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * La classe UserManager est responsable de la gestion des utilisateurs dans l'application.
 * Elle permet d'enregistrer de nouveaux utilisateurs, de gérer la connexion des utilisateurs existants,
 * et d'afficher la liste des utilisateurs enregistrés.
 */
public class UserManager {

    /** Liste des utilisateurs enregistrés */
    private final List<User> users;

    /** Liste des portefeuilles associés */
    private final List<Portfolio> portfolios;

    /** Constructeur : initialise les listes */
    public UserManager() {
        this.users = new ArrayList<>();
        this.portfolios = new ArrayList<>();
    }

    /**
     * Enregistre un nouvel utilisateur.
     * @param userID Identifiant unique de l'utilisateur (String)
     * @param email Adresse email
     * @param password Mot de passe
     * @param firstName Prénom
     * @param lastName Nom de famille
     * @return L'utilisateur nouvellement créé
     */
    public User registerUser(String userID, String email, String password, String firstName, String lastName) {
        User newUser = new User(userID, email, password, firstName, lastName);
        users.add(newUser);
        return newUser;
    }

    /**
     * Authentifie un utilisateur par email et mot de passe.
     * @param email Email fourni
     * @param password Mot de passe fourni
     * @return L'utilisateur si la connexion réussit, sinon null
     */
    public User login(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                System.out.println("Connexion réussie pour " + user.getFirstName());
                return user;
            }
        }
        System.out.println("Échec de la connexion : email ou mot de passe invalide.");
        return null;
    }

    /** Affiche tous les utilisateurs dans la console */
    @SuppressWarnings("unused")
    public void listUsers() {
        for (User user : users) {
            System.out.println(user);
        }
    }

    /** Retourne tous les utilisateurs */
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /** Retourne tous les portefeuilles */
    public List<Portfolio> getAllPortfolios() {
        return new ArrayList<>(portfolios);
    }

    /** Ajoute un portefeuille */
    public Portfolio addPortfolio(Portfolio portfolio) {
        portfolios.add(portfolio);
        return portfolio;
    }

    /** Récupère un portefeuille par ID */
    public Portfolio getPortfolioById(int id) {
        for (Portfolio portfolio : portfolios) {
            if (portfolio.getId() == id) {
                return portfolio;
            }
        }
        return null;
    }

    /** Supprime un portefeuille par ID */
    public boolean deletePortfolio(int id) {
        Iterator<Portfolio> iterator = portfolios.iterator();
        while (iterator.hasNext()) {
            Portfolio portfolio = iterator.next();
            if (portfolio.getId() == id) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /** Récupère un utilisateur par ID (comparaison sur String userID) */
    public User getUserById(int id) {
        for (User user : users) {
            if (user.getUserID().equals(String.valueOf(id))) {
                return user;
            }
        }
        return null;
    }

    /** Supprime un utilisateur par ID (comparaison sur String userID) */
    public boolean deleteUser(int id) {
        Iterator<User> iterator = users.iterator();
        while (iterator.hasNext()) {
            User user = iterator.next();
            if (user.getUserID().equals(String.valueOf(id))) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /** Calcule la richesse totale pour un utilisateur donné */
    public WealthTracker calculateWealthForUser(int userId) {
        User user = getUserById(userId);
        if (user == null) {
            return null;
        }
        double totalWealth = 0.0;
        for (Portfolio portfolio : portfolios) {
            if (portfolio.getUserID().equals(user.getUserID())) {
                for (Asset asset : portfolio.getAssets()) {
                    totalWealth += asset.getUnitValue() * asset.getQuantity();
                }
            }
        }
        return new WealthTracker(userId, totalWealth);
    }
}