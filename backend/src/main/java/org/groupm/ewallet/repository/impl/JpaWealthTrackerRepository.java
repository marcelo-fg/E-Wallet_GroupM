package org.groupm.ewallet.repository.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.groupm.ewallet.model.WealthTracker;
import org.groupm.ewallet.repository.WealthTrackerRepository;

import java.util.List;

/**
 * JPA implementation of WealthTrackerRepository.
 * Uses injected request-scoped EntityManager for proper transaction
 * coordination.
 */
@ApplicationScoped
public class JpaWealthTrackerRepository implements WealthTrackerRepository {

    @Inject
    private EntityManager em;

    @Override
    public void save(WealthTracker tracker) {
        if (tracker.getId() == 0) {
            WealthTracker existing = findByUserIdInternal(tracker.getUser().getUserID());
            if (existing != null) {
                tracker.setId(existing.getId());
                em.merge(tracker);
            } else {
                em.persist(tracker);
            }
        } else {
            em.merge(tracker);
        }
    }

    private WealthTracker findByUserIdInternal(String userId) {
        List<WealthTracker> results = em.createQuery(
                "SELECT w FROM WealthTracker w WHERE w.user.userID = :userId", WealthTracker.class)
                .setParameter("userId", userId)
                .setMaxResults(1)
                .getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public WealthTracker findByUserId(String userId) {
        return findByUserIdInternal(userId);
    }

    @Override
    public void delete(String userId) {
        WealthTracker tracker = findByUserIdInternal(userId);
        if (tracker != null) {
            WealthTracker toDelete = em.find(WealthTracker.class, tracker.getId());
            if (toDelete != null) {
                em.remove(toDelete);
            }
        }
    }
}
