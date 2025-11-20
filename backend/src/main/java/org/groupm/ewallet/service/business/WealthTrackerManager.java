package org.groupm.ewallet.service.business;

import org.groupm.ewallet.model.WealthTracker;
import org.groupm.ewallet.model.User;
import org.groupm.ewallet.repository.WealthTrackerRepository;

/**
 * Service métier dédié au suivi et à la gestion de la richesse totale d’un utilisateur.
 * Permet la création, mise à jour, récupération et suppression du WealthTracker lié à chaque utilisateur.
 */
public class WealthTrackerManager {

    private final WealthTrackerRepository wealthTrackerRepository;

    /**
     * Constructeur avec injection du repository métier.
     * @param wealthTrackerRepository repository du suivi de richesse
     */
    public WealthTrackerManager(WealthTrackerRepository wealthTrackerRepository) {
        this.wealthTrackerRepository = wealthTrackerRepository;
    }

    /**
     * Crée ou met à jour le WealthTracker d’un utilisateur.
     * @param tracker WealthTracker métier
     * @return WealthTracker sauvegardé
     */
    public WealthTracker saveWealthTracker(WealthTracker tracker) {
        wealthTrackerRepository.save(tracker);
        return tracker;
    }

    /**
     * Récupère le WealthTracker d’un utilisateur selon son identifiant.
     * @param userID identifiant utilisateur
     * @return WealthTracker ou null
     */
    public WealthTracker getWealthTrackerByUserId(String userID) {
        return wealthTrackerRepository.findByUserId(userID);
    }

    /**
     * Supprime le WealthTracker associé à un utilisateur.
     * @param userID identifiant utilisateur
     * @return true si suppression réussie, false sinon
     */
    public boolean deleteWealthTracker(String userID) {
        WealthTracker tracker = wealthTrackerRepository.findByUserId(userID);
        if (tracker != null) {
            wealthTrackerRepository.delete(userID);
            return true;
        }
        return false;
    }

    /**
     * Met à jour les valeurs du WealthTracker en fonction des nouvelles données utilisateur.
     * Appelle la logique métier pour recalculer la richesse, la croissance, etc.
     * @param user utilisateur concerné
     * @return WealthTracker mis à jour
     */
    public WealthTracker updateWealthForUser(User user) {
        WealthTracker tracker = wealthTrackerRepository.findByUserId(user.getUserID());
        if (tracker == null) {
            tracker = new WealthTracker(user);
        }
        tracker.updateWealth();
        wealthTrackerRepository.save(tracker);
        return tracker;
    }
}
