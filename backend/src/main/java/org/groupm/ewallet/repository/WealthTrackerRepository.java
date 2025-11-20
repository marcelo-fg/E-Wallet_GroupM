package org.groupm.ewallet.repository;

import org.groupm.ewallet.model.WealthTracker;

/**
 * Interface de persistance pour le suivi de la richesse/utilisateur.
 */
public interface WealthTrackerRepository {
    void save(WealthTracker tracker);
    WealthTracker findByUserId(String userID);
    void delete(String userID);
}
