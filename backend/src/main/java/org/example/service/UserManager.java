package org.example.service;

import org.example.model.User;
import java.util.ArrayList;
import java.util.List;

/**
 * La classe UserManager est responsable de la gestion des utilisateurs dans l'application.
 * Elle permet d'enregistrer de nouveaux utilisateurs, de gérer la connexion des utilisateurs existants,
 * et d'afficher la liste des utilisateurs enregistrés.
 */
public class UserManager {
    /**
     * Liste qui stocke tous les utilisateurs enregistrés.
     * Chaque utilisateur est représenté par un objet de la classe User.
     */
    private List<User> users;

    /**
     * Constructeur de la classe UserManager.
     * Il initialise la liste des utilisateurs comme une liste vide.
     * Cela signifie qu'au départ, aucun utilisateur n'est enregistré.
     */
    public UserManager() {
        this.users = new ArrayList<>();
    }

    /**
     * Cette méthode permet d'enregistrer un nouvel utilisateur.
     * Elle prend en paramètres les informations nécessaires pour créer un utilisateur :
     * - userID : un identifiant unique pour l'utilisateur
     * - email : l'adresse email de l'utilisateur
     * - password : le mot de passe choisi par l'utilisateur
     * - firstName : le prénom de l'utilisateur
     * - lastName : le nom de famille de l'utilisateur
     *
     * La méthode crée un nouvel objet User avec ces informations,
     * l'ajoute à la liste des utilisateurs, puis retourne cet objet.
     *
     * @param userID Identifiant unique de l'utilisateur
     * @param email Adresse email de l'utilisateur
     * @param password Mot de passe de l'utilisateur
     * @param firstName Prénom de l'utilisateur
     * @param lastName Nom de famille de l'utilisateur
     * @return L'objet User nouvellement créé
     */
    public User registerUser(String userID, String email, String password, String firstName, String lastName) {
        User newUser = new User(userID, email, password, firstName, lastName);
        users.add(newUser);
        return newUser;
    }

    /**
     * Cette méthode permet à un utilisateur de se connecter.
     * Elle prend en paramètres l'email et le mot de passe fournis par l'utilisateur.
     * La méthode parcourt la liste des utilisateurs pour vérifier si un utilisateur
     * a l'email et le mot de passe correspondants.
     *
     * Si une correspondance est trouvée, un message de succès est affiché,
     * et l'objet User correspondant est retourné.
     * Sinon, un message d'erreur est affiché, et la méthode retourne null.
     *
     * @param email Email fourni pour la connexion
     * @param password Mot de passe fourni pour la connexion
     * @return L'objet User si la connexion réussit, sinon null
     */
    public User login(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                System.out.println("Login successful for " + user.getFirstName());
                return user;
            }
        }
        System.out.println("Invalid email or password.");
        return null;
    }

    /**
     * Cette méthode affiche la liste de tous les utilisateurs enregistrés.
     * Elle parcourt la liste des utilisateurs et affiche chaque utilisateur.
     * Cela peut être utile pour vérifier quels utilisateurs sont actuellement enregistrés.
     */
    public void listUsers() {
        for (User user : users) {
            System.out.println(user);
        }
    }

    /**
     * Retourne la liste de tous les utilisateurs enregistrés.
     * @return Liste des utilisateurs
     */
    public List<User> getAllUsers() {
        return this.users;
    }
}