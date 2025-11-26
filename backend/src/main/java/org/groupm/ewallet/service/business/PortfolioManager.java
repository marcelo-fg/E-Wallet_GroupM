package org.groupm.ewallet.service.business;

import org.groupm.ewallet.model.Portfolio;
import org.groupm.ewallet.repository.PortfolioRepository;

import java.util.List;

/**
 * Service métier pour la gestion des portefeuilles.
 */
public class PortfolioManager {

    private final PortfolioRepository portfolioRepository;

    public PortfolioManager(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    /**
     * Sauvegarde ou met à jour un portefeuille.
     */
    public Portfolio savePortfolio(Portfolio portfolio) {
        portfolioRepository.save(portfolio);
        return portfolio;
    }

    /**
     * Récupère TOUS les portefeuilles d’un utilisateur.
     * @param userID identifiant utilisateur
     * @return liste de portefeuilles
     */
    public List<Portfolio> getPortfoliosByUserId(String userID) {
        return portfolioRepository.findAllByUserId(userID);
    }

    /**
     * Récupère un portefeuille par son identifiant unique.
     */
    public Portfolio getPortfolioById(int id) {
        return portfolioRepository.findById(id);
    }

    /**
     * Supprime un portefeuille.
     */
    public boolean deletePortfolio(int id) {
        Portfolio portfolio = portfolioRepository.findById(id);
        if (portfolio != null) {
            portfolioRepository.delete(id);
            return true;
        }
        return false;
    }
}