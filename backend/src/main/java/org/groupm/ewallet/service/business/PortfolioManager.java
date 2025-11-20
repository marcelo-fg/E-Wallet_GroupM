package org.groupm.ewallet.service.business;

import org.groupm.ewallet.model.Portfolio;
import org.groupm.ewallet.model.Asset;
import org.groupm.ewallet.repository.PortfolioRepository;

import java.util.List;

/**
 * Service métier pour la gestion des portefeuilles d’investissement.
 * Permet de créer, récupérer, modifier, supprimer et d’ajouter/retirer des actifs du portefeuille.
 */
public class PortfolioManager {

    private final PortfolioRepository portfolioRepository;

    /**
     * Constructeur avec injection de repository pour la persistance.
     * @param portfolioRepository repository métier
     */
    public PortfolioManager(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    /**
     * Crée ou met à jour un portefeuille.
     * @param portfolio portefeuille à enregistrer
     * @return portefeuille persistant
     */
    public Portfolio savePortfolio(Portfolio portfolio) {
        portfolioRepository.save(portfolio);
        return portfolio;
    }

    /**
     * Récupère le portefeuille d’un utilisateur.
     * @param userID identifiant utilisateur
     * @return portefeuille ou null
     */
    public Portfolio getPortfolioByUserId(String userID) {
        return portfolioRepository.findByUserId(userID);
    }

    /**
     * Récupère un portefeuille par son identifiant unique.
     * @param id identifiant portefeuille
     * @return portefeuille ou null
     */
    public Portfolio getPortfolioById(int id) {
        return portfolioRepository.findById(id);
    }

    /**
     * Supprime le portefeuille via son identifiant.
     * @param id identifiant portefeuille
     * @return true si supprimé, false sinon
     */
    public boolean deletePortfolio(int id) {
        Portfolio portfolio = portfolioRepository.findById(id);
        if (portfolio != null) {
            portfolioRepository.delete(id);
            return true;
        }
        return false;
    }

    /**
     * Ajoute un actif au portefeuille.
     * @param id identifiant portefeuille
     * @param asset actif à ajouter
     * @return true si réussite, false sinon
     */
    public boolean addAssetToPortfolio(int id, Asset asset) {
        Portfolio portfolio = portfolioRepository.findById(id);
        if (portfolio != null && asset != null) {
            portfolio.addAsset(asset);
            portfolioRepository.save(portfolio);
            return true;
        }
        return false;
    }

    /**
     * Retire un actif du portefeuille par son nom/identifiant.
     * @param id identifiant portefeuille
     * @param assetName nom de l’actif à retirer
     * @return true si supprimé, false sinon
     */
    public boolean removeAssetFromPortfolio(int id, String assetName) {
        Portfolio portfolio = portfolioRepository.findById(id);
        if (portfolio != null && assetName != null) {
            portfolio.removeAsset(assetName);
            portfolioRepository.save(portfolio);
            return true;
        }
        return false;
    }

    /**
     * Retourne la liste des actifs contenus dans le portefeuille.
     * @param id identifiant portefeuille
     * @return liste des actifs ou liste vide
     */
    public List<Asset> getAssetsOfPortfolio(int id) {
        Portfolio portfolio = portfolioRepository.findById(id);
        if (portfolio != null) {
            return portfolio.getAssets();
        }
        return List.of();
    }
}
