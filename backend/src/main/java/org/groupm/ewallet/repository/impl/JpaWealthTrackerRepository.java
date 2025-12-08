package org.groupm.ewallet.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.groupm.ewallet.model.WealthTracker;
import org.groupm.ewallet.repository.WealthTrackerRepository;

import java.util.List;

/**
 * JPA implementation of WealthTrackerRepository.
 * Manages persistence of WealthTracker snapshots and history.
 */
public class JpaWealthTrackerRepository implements WealthTrackerRepository {

    private final EntityManagerFactory emf;

    public JpaWealthTrackerRepository() {
        this.emf = Persistence.createEntityManagerFactory("ewalletPU");
        System.out.println("[JpaWealthTrackerRepository] EntityManagerFactory created successfully");
    }

    @Override
    public void save(WealthTracker tracker) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            // Since WealthTracker doesn't have a reliable ID before save logic in Manager
            // might be tricky,
            // we check if it already has an ID assigned by JPA
            if (tracker.getId() == 0) {
                // Check if one already exists for this user to update it instead of creating
                // duplicates
                // This enforces One-Tracker-Per-User logic
                WealthTracker existing = findByUserId(tracker.getUser().getUserID());
                if (existing != null) {
                    tracker.setId(existing.getId()); // Take over the ID
                    em.merge(tracker);
                } else {
                    em.persist(tracker);
                }
            } else {
                em.merge(tracker);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public WealthTracker findByUserId(String userId) {
        EntityManager em = emf.createEntityManager();
        try {
            List<WealthTracker> results = em.createQuery(
                    "SELECT w FROM WealthTracker w WHERE w.user.userID = :userId", WealthTracker.class)
                    .setParameter("userId", userId)
                    .setMaxResults(1)
                    .getResultList();
            return results.isEmpty() ? null : results.get(0);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(String userId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            WealthTracker tracker = findByUserId(userId);
            if (tracker != null) {
                // Because we are in a new EM session, we need to merge or find again within
                // transaction
                // findByUserId uses its own EM. So we do it here:
                WealthTracker toDelete = em.find(WealthTracker.class, tracker.getId());
                if (toDelete != null) {
                    em.remove(toDelete);
                }
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
