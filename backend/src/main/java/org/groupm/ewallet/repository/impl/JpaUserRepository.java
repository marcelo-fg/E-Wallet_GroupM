package org.groupm.ewallet.repository.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.groupm.ewallet.model.User;
import org.groupm.ewallet.repository.UserRepository;

import java.util.List;

/**
 * JPA implementation of UserRepository.
 * Uses injected request-scoped EntityManager for proper transaction
 * coordination.
 */
@ApplicationScoped
public class JpaUserRepository implements UserRepository {

    @Inject
    private EntityManager em;

    @Override
    public User findByEmail(String email) {
        List<User> users = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList();
        return users.isEmpty() ? null : users.get(0);
    }

    @Override
    public User findById(String id) {
        return em.find(User.class, id);
    }

    @Override
    public void save(User user) {
        if (em.find(User.class, user.getUserID()) == null) {
            em.persist(user);
        } else {
            em.merge(user);
        }
    }

    @Override
    public void delete(String id) {
        User user = em.find(User.class, id);
        if (user != null) {
            em.remove(user);
        }
    }

    @Override
    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }
}
