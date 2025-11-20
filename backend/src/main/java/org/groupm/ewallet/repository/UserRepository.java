package org.groupm.ewallet.repository;

import org.groupm.ewallet.model.User;
import java.util.List;

/**
 * Interface de gestion de la persistance des utilisateurs.
 */
public interface UserRepository {
    void save(User user);
    User findById(String userID);
    User findByEmail(String email);
    void delete(String userID);
    List<User> findAll();
}
