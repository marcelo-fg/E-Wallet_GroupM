package org.groupm.ewallet.repository;

import org.groupm.ewallet.model.Portfolio;

/**
 * Interface de persistance pour les portefeuilles d’investissement.
 */
public interface PortfolioRepository {
    void save(Portfolio portfolio);
    Portfolio findById(int id);
    void delete(int id);

    /**
     * Récupère le portefeuille associé à un utilisateur.
     * @param userID identifiant de l’utilisateur
     * @return portefeuille trouvé ou null
     */
    Portfolio findByUserId(String userID);
    java.util.List<Portfolio> findAll();
}
