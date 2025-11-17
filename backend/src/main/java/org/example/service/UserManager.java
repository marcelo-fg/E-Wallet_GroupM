package org.example.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.example.model.User;
import org.example.model.Account;
import org.example.model.Portfolio;
import org.example.model.WealthTracker;
import org.example.model.Asset;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Service responsable de la gestion des utilisateurs et de leurs portefeuilles.
 * Il permet d’enregistrer, d’authentifier, de modifier et de supprimer des utilisateurs,
 * ainsi que de calculer leur richesse totale.
 */
@ApplicationScoped
public class UserManager {

    /** Liste globale des utilisateurs enregistrés. */
    private static final List<User> users = new ArrayList<>();

    /** Liste globale des portefeuilles associés aux utilisateurs. */
    private static final List<Portfolio> portfolios = new ArrayList<>();

    /**
     * Constructeur par défaut.
     * Initialise la liste des utilisateurs avec des données de démonstration.
     */
    public UserManager() {
        populateUsers();
    }

    /**
     * Crée et ajoute des utilisateurs et comptes de démonstration.
     * Méthode utilisée pour initialiser le service avec des données de test.
     */
    public void populateUsers() {
        // Premier utilisateur avec un compte courant
        User user1 = new User("1", "test@example.com", "1234", "test", "Demo");
        Account account1 = new Account("A001", "courant", 1200.50);
        user1.addAccount(account1);

        // Ajout des utilisateurs à la liste
        users.add(user1);
    }

    /**
     * Enregistre un nouvel utilisateur dans le système.
     *
     * @param userID identifiant unique de l’utilisateur
     * @param email adresse e-mail
     * @param password mot de passe
     * @param firstName prénom
     * @param lastName nom de famille
     * @return l’utilisateur nouvellement créé
     */
    public User registerUser(String userID, String email, String password, String firstName, String lastName) {
        User newUser = new User(userID, email, password, firstName, lastName);
        users.add(newUser);
        return newUser;
    }

    /**
     * Authentifie un utilisateur via son adresse e-mail et son mot de passe.
     *
     * @param email adresse e-mail
     * @param password mot de passe
     * @return l’utilisateur si l’authentification réussit, sinon null
     */
    public User login(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                System.out.println("Connexion réussie pour " + user.getFirstName());
                return user;
            }
        }
        System.out.println("Échec de la connexion : e-mail ou mot de passe invalide.");
        return null;
    }

    /**
     * Affiche dans la console la liste des utilisateurs enregistrés.
     */
    @SuppressWarnings("unused")
    public void listUsers() {
        for (User user : users) {
            System.out.println(user);
        }
    }

    /**
     * Retourne une copie de la liste des utilisateurs.
     *
     * @return liste des utilisateurs
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    /**
     * Retourne la liste des portefeuilles enregistrés.
     *
     * @return liste des portefeuilles
     */
    public List<Portfolio> getAllPortfolios() {
        return new ArrayList<>(portfolios);
    }

    /**
     * Ajoute un portefeuille à la liste globale.
     *
     * @param portfolio portefeuille à ajouter
     * @return le portefeuille ajouté
     */
    public Portfolio addPortfolio(Portfolio portfolio) {
        portfolios.add(portfolio);
        return portfolio;
    }

    /**
     * Recherche un portefeuille par son identifiant.
     *
     * @param id identifiant du portefeuille
     * @return portefeuille correspondant ou null s’il n’existe pas
     */
    public Portfolio getPortfolioById(int id) {
        for (Portfolio portfolio : portfolios) {
            if (portfolio.getId() == id) {
                return portfolio;
            }
        }
        return null;
    }

    /**
     * Supprime un portefeuille par identifiant.
     *
     * @param id identifiant du portefeuille à supprimer
     * @return true si le portefeuille a été supprimé, sinon false
     */
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

    /**
     * Recherche un utilisateur par identifiant.
     *
     * @param id identifiant numérique (converti en chaîne)
     * @return utilisateur correspondant ou null s’il n’existe pas
     */
    public User getUserById(int id) {
        for (User user : users) {
            if (user.getUserID().equals(String.valueOf(id))) {
                return user;
            }
        }
        return null;
    }

    /**
     * Supprime un utilisateur par identifiant.
     *
     * @param id identifiant de l’utilisateur à supprimer
     * @return true si la suppression a réussi, sinon false
     */
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

    /**
     * Calcule la richesse totale d’un utilisateur, incluant les actifs
     * de ses portefeuilles et la valeur de ses comptes.
     *
     * @param userId identifiant de l’utilisateur
     * @return objet WealthTracker contenant la richesse totale
     */
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

    /**
     * Met à jour les informations d’un utilisateur existant.
     * Seuls les champs fournis dans le nouvel objet sont mis à jour.
     *
     * @param id identifiant de l’utilisateur à modifier
     * @param newUser nouvel objet utilisateur contenant les mises à jour
     * @return true si la mise à jour a réussi, sinon false
     */
    public boolean updateUser(int id, User newUser) {
        for (User user : users) {
            if (user.getUserID().equals(String.valueOf(id))) {
                if (newUser.getFirstName() != null) user.setFirstName(newUser.getFirstName());
                if (newUser.getLastName() != null) user.setLastName(newUser.getLastName());
                if (newUser.getEmail() != null) user.setEmail(newUser.getEmail());
                if (newUser.getPassword() != null) user.setPassword(newUser.getPassword());
                return true;
            }
        }
        return false;
    }
}