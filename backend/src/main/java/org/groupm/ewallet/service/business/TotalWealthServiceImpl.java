package org.groupm.ewallet.repository;

import org.groupm.ewallet.model.User;
import org.groupm.ewallet.model.WealthTracker;
import org.groupm.ewallet.service.business.TotalWealthService;
import org.groupm.ewallet.service.business.UserManager;
import org.groupm.ewallet.service.business.WealthTrackerManager;

/**
 * Implémentation de TotalWealthService qui délègue à WealthTrackerManager.
 * Cette classe appartient à la couche service métier et ne dépend pas de JSF.
 */
public class TotalWealthServiceImpl implements TotalWealthService {

    private final WealthTrackerManager wealthTrackerManager;
    private final UserManager userManager;

    /**
     * Constructeur principal.
     * Les dépendances sont injectées par la configuration (CDI, manuel, etc.).
     *
     * @param wealthTrackerManager service chargé de gérer les WealthTracker
     * @param userManager          service métier utilisateur (accès comptes / portefeuilles)
     */
    public TotalWealthServiceImpl(WealthTrackerManager wealthTrackerManager,
                                  UserManager userManager) {
        this.wealthTrackerManager = wealthTrackerManager;
        this.userManager = userManager;
    }

    /**
     * Résout l'utilisateur courant.
     * À adapter plus tard avec ta vraie gestion d'authentification.
     *
     * @return utilisateur courant ou null si introuvable
     */
    private User resolveCurrentUser() {
        // TODO: remplacer "demo-user" par l'ID réellement connecté (session, token, etc.)
        return userManager.getUserById("demo-user");
    }

    /**
     * Retourne la richesse mise à jour pour l'utilisateur courant.
     * Utilise WealthTrackerManager pour recalculer comptes + portefeuilles et persister.
     */
    @Override
    public WealthTracker getCurrentUserWealth() {
        User current = resolveCurrentUser();
        if (current == null) {
            return null;
        }
        return wealthTrackerManager.updateWealthForUser(current);
    }

    /**
     * Retourne la richesse mise à jour pour un utilisateur donné.
     *
     * @param userId identifiant fonctionnel de l'utilisateur
     * @return WealthTracker à jour ou null si l'utilisateur n'existe pas
     */
    @Override
    public WealthTracker getWealthForUser(String userId) {
        User user = userManager.getUserById(userId);
        if (user == null) {
            return null;
        }
        return wealthTrackerManager.updateWealthForUser(user);
    }
}
