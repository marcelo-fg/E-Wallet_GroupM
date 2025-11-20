package org.groupm.ewallet.repository;

import org.groupm.ewallet.model.Account;
import java.util.List;

/**
 * Interface de gestion des comptes utilisateurs.
 * Définit les opérations de base pour la persistence des comptes.
 */
public interface AccountRepository {
    /**
     * Sauvegarde ou met à jour un compte.
     * @param account compte à sauvegarder
     */
    void save(Account account);

    /**
     * Recherche un compte par son identifiant unique.
     * @param accountId identifiant du compte (String ou int selon ton modèle)
     * @return le compte correspondant, ou null si absent
     */
    Account findById(String accountId);

    /**
     * Supprime un compte par son identifiant.
     * @param accountId identifiant du compte
     */
    void delete(String accountId);

    /**
     * Retourne tous les comptes enregistrés.
     * @return liste complète des comptes
     */
    List<Account> findAll();

    /**
     * Retourne les comptes associés à un utilisateur donné.
     * @param userId identifiant de l'utilisateur
     * @return liste des comptes liés
     */
    List<Account> findByUserId(String userId);
}
