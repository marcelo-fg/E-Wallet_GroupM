package org.groupm.ewallet.repository;

import org.groupm.ewallet.model.Portfolio;
import java.util.List;

/**
 * Interface de persistance pour les portefeuilles d’investissement.
 */
public interface PortfolioRepository {

    void save(Portfolio portfolio);

    Portfolio findById(int id);

    void delete(int id);

    List<Portfolio> findAll();

    /**
     * Retourne tous les portefeuilles appartenant à un utilisateur.
     * @param userID identifiant de l'utilisateur
     */
    List<Portfolio> findAllByUserId(String userID);
}