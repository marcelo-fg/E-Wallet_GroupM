package org.groupm.ewallet.repository;

import org.groupm.ewallet.model.Asset;
import java.util.List;

/**
 * Interface de gestion des actifs financiers.
 * Définit les opérations de base pour la persistence des assets.
 */
public interface AssetRepository {
    /**
     * Sauvegarde ou met à jour un actif.
     * @param asset actif à sauvegarder
     */
    void save(Asset asset);

    /**
     * Recherche un actif par son symbole.
     * @param symbol symbole unique de l'actif (ex : "AAPL", "BTC")
     * @return l'actif trouvé, ou null si absent
     */
    Asset findBySymbol(String symbol);

    /**
     * Supprime un actif par son symbole.
     * @param symbol symbole unique de l'actif
     */
    void delete(String symbol);

    /**
     * Retourne tous les actifs enregistrés.
     * @return liste complète des assets
     */
    List<Asset> findAll();

    /**
     * Recherche tous les actifs d’un type donné.
     * @param type type de l’actif (ex : "stock", "crypto")
     * @return liste des actifs du type
     */
    List<Asset> findByType(String type);
}
