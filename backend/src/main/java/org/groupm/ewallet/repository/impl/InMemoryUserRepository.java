package org.groupm.ewallet.repository.impl;

import org.groupm.ewallet.repository.UserRepository;
import org.groupm.ewallet.model.User;

import java.util.*;

/**
 * Implémentation mémoire du repository User.
 * Permet de stocker, rechercher, et supprimer les utilisateurs pour les tests/démos.
 */
public class InMemoryUserRepository implements UserRepository {

    // Stockage en mémoire : id -> User
    private final Map<String, User> users = new HashMap<>();

    @Override
    public void save(User user) {
        users.put(user.getUserID(), user);
    }

    @Override
    public User findById(String userID) {
        return users.get(userID);
    }

    @Override
    public User findByEmail(String email) {
        return users.values().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    @Override
    public void delete(String userID) {
        users.remove(userID);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }
}
