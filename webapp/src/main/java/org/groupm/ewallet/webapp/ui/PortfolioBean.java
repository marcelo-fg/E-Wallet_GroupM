package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.groupm.ewallet.webapp.connector.ExternalAsset;
import org.groupm.ewallet.webapp.service.WebAppService;

import java.io.Serializable;
import java.util.List;

/**
 * Bean de session principal pour la gestion des portefeuilles et de leurs actifs.
 * Sert de couche de présentation entre les pages JSF et la couche service.
 */
@Named
@SessionScoped
public class PortfolioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private WebAppService webAppService;

    /**
     * Identifiants techniques des portefeuilles de l'utilisateur connecté.
     */
    private List<Integer> portfolioIds;

    /**
     * Identifiant du portefeuille actuellement sélectionné dans l'UI.
     */
    private Integer selectedPortfolioId;

    /**
     * Représentation textuelle des actifs pour le portefeuille sélectionné.
     * (liste de chaînes prête à être affichée dans la vue).
     */
    private List<String> assets;

    // -------------------------------------------------------------------------
    // PROPRIETE POUR LE NOM DE PORTEFEUILLE
    // -------------------------------------------------------------------------

    /**
     * Nom du portefeuille saisi par l'utilisateur lors de la création
     * via le formulaire JSF (binding: #{portfolioBean.portfolioName}).
     */
    private String portfolioName;

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    // -------------------------------------------------------------------------
    // API EXTERNE
    // -------------------------------------------------------------------------

    /**
     * Type d'actif choisi dans l'UI pour l'API externe (crypto / stock / etf).
     */
    private String selectedType;

    /**
     * Symbole externe sélectionné (BTC, AAPL, SPY, ...).
     */
    private String selectedExternalSymbol;

    /**
     * Identifiant d'API externe (utilisé pour certaines API crypto).
     */
    private String selectedExternalApiId;

    /**
     * Nom "lisible" de l'actif externe (Bitcoin, Apple Inc., ...).
     */
    private String selectedExternalName;

    /**
     * Liste des actifs disponibles depuis l'API externe pour le type choisi.
     */
    private List<ExternalAsset> availableAssets;

    // -------------------------------------------------------------------------
    // FORMULAIRE COMMUN POUR LES ACTIFS
    // -------------------------------------------------------------------------

    /**
     * Quantité d'actif à ajouter au portefeuille.
     */
    private double assetQuantity;

    /**
     * Prix de marché unitaire renvoyé par une API ou saisi à la main.
     */
    private double marketUnitPrice;

    /**
     * Valeur unitaire de l'actif stockée dans le portefeuille.
     * Peut être alignée sur marketUnitPrice ou saisie manuellement.
     */
    private double assetUnitValue;

    // -------------------------------------------------------------------------
    // ACTIF MANUEL
    // -------------------------------------------------------------------------

    /**
     * Nom de l'actif lorsqu'il est ajouté manuellement.
     */
    private String assetName;

    /**
     * Type de l'actif lorsqu'il est ajouté manuellement (ex: "stock", "crypto").
     */
    private String assetType;

    /**
     * Symbole associé à l'actif manuel (ex: "AAPL", "BTC").
     */
    private String assetSymbol;

    // -------------------------------------------------------------------------
    // FORMULAIRE PERSONNALISÉ
    // -------------------------------------------------------------------------

    /**
     * Indique si le formulaire d'actif personnalisé doit être affiché.
     * Initialisé à true pour qu'il soit visible par défaut.
     */
    private boolean showCustomAssetForm = true;

    public boolean isShowCustomAssetForm() {
        return showCustomAssetForm;
    }

    public void setShowCustomAssetForm(boolean showCustomAssetForm) {
        this.showCustomAssetForm = showCustomAssetForm;
    }

    /**
     * Permet de basculer l'affichage du formulaire personnalisé
     * (appelé depuis un bouton dans l'interface).
     */
    public void toggleCustomAssetForm() {
        this.showCustomAssetForm = !this.showCustomAssetForm;
    }

    // -------------------------------------------------------------------------
    // GESTION DE SESSION
    // -------------------------------------------------------------------------

    /**
     * Récupère l'identifiant fonctionnel de l'utilisateur courant
     * depuis la session HTTP.
     *
     * @return userId ou null si non présent en session.
     */
    private String getUserIdFromSession() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session =
                (HttpSession) context.getExternalContext().getSession(false);

        if (session == null) {
            return null;
        }
        return (String) session.getAttribute("userId");
    }

    // -------------------------------------------------------------------------
    // PORTFOLIOS
    // -------------------------------------------------------------------------

    /**
     * Retourne la liste des identifiants de portefeuilles.
     * Déclenche un chargement lazy si nécessaire.
     *
     * @return liste d'identifiants de portefeuilles (jamais null).
     */
    public List<Integer> getPortfolioIds() {
        if (portfolioIds == null) {
            loadPortfolios();
        }
        return portfolioIds;
    }

    /**
     * Charge les portefeuilles pour l'utilisateur courant
     * via la couche service, ou une liste vide si non connecté.
     */
    public void loadPortfolios() {
        String userId = getUserIdFromSession();
        if (userId == null) {
            portfolioIds = List.of();
            return;
        }
        portfolioIds = webAppService.getPortfoliosForUser(userId);
    }

    /**
     * Charge les actifs du portefeuille actuellement sélectionné.
     * Utilise une liste vide si aucun portefeuille n'est sélectionné.
     */
    public void loadAssets() {
        if (selectedPortfolioId == null) {
            assets = List.of();
            return;
        }
        assets = webAppService.getAssetsForPortfolio(selectedPortfolioId);
    }

    // -------------------------------------------------------------------------
    // API EXTERNE - CHARGEMENT ET SELECTION
    // -------------------------------------------------------------------------

    /**
     * Charge les actifs depuis l'API externe pour le type sélectionné.
     * Réinitialise la sélection courante et les prix associés.
     */
    public void loadAssetsFromApi() {
        if (selectedType == null || selectedType.isBlank()) {
            availableAssets = List.of();
            return;
        }

        availableAssets = webAppService.loadAssetsFromApi(selectedType);

        selectedExternalSymbol = null;
        selectedExternalApiId = null;
        selectedExternalName = null;
        marketUnitPrice = 0.0;
    }

    /**
     * Getter exposant le symbole externe sélectionné.
     *
     * @return symbole de l'actif externe sélectionné.
     */
    public String getSelectedExternalAsset() {
        return selectedExternalSymbol;
    }

    /**
     * Setter appelé lors du changement de sélection d'un actif externe
     * dans l'interface (ex: selectOneMenu).
     *
     * @param symbol symbole choisi par l'utilisateur.
     */
    public void setSelectedExternalAsset(String symbol) {
        this.selectedExternalSymbol = symbol;

        if (availableAssets == null) {
            return;
        }

        for (ExternalAsset ea : availableAssets) {
            if (ea.getSymbol().equalsIgnoreCase(symbol)) {
                this.selectedExternalApiId = ea.getApiId(); // crypto uniquement
                this.selectedExternalName = ea.getName();
                break;
            }
        }
    }

    /**
     * Charge le prix de marché pour l'actif externe actuellement sélectionné
     * et aligne la valeur unitaire de l'actif sur ce prix.
     */
    public void loadPriceForSelectedAsset() {
        if (selectedExternalSymbol == null) {
            return;
        }

        String idOrSymbol =
                selectedType.equalsIgnoreCase("crypto")
                        ? selectedExternalApiId
                        : selectedExternalSymbol;

        marketUnitPrice =
                webAppService.getPriceForAsset(idOrSymbol, selectedType);

        // On synchronise la valeur unitaire de l'actif sur le prix de marché.
        assetUnitValue = marketUnitPrice;
    }

    // -------------------------------------------------------------------------
    // CREATION DE PORTEFEUILLE
    // -------------------------------------------------------------------------

    /**
     * Crée un nouveau portefeuille pour l'utilisateur courant.
     * L'utilisation de portfolioName dépend de la signature réelle
     * de la méthode createPortfolioForUser du service.
     *
     * @return navigation (null pour rester sur la même page).
     */
    public String createPortfolio() {
        String userId = getUserIdFromSession();
        if (userId == null) {
            return null;
        }

        boolean created = webAppService.createPortfolioForUser(userId);

        if (created) {
            loadPortfolios();
            // Réinitialisation du champ de formulaire après succès.
            portfolioName = null;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // AJOUT D'UN ACTIF MANUEL
    // -------------------------------------------------------------------------

    /**
     * Ajoute un actif saisi manuellement au portefeuille sélectionné.
     *
     * @return navigation (null pour rester sur la même page).
     */
    public String addAsset() {
        if (selectedPortfolioId == null) {
            return null;
        }

        boolean ok = webAppService.addAssetToPortfolio(
                selectedPortfolioId,
                assetName,
                assetType,
                assetQuantity,
                assetUnitValue,
                assetSymbol
        );

        if (ok) {
            // Réinitialisation des champs du formulaire manuel.
            assetName = null;
            assetType = null;
            assetSymbol = null;
            assetQuantity = 0.0;
            assetUnitValue = 0.0;
            loadAssets();
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // AJOUT D'UN ACTIF PERSONNALISÉ (BOUTON PERSONNALISÉ)
    // -------------------------------------------------------------------------

    /**
     * Ajoute un actif personnalisé au portefeuille, puis masque le
     * formulaire personnalisé après succès.
     *
     * @return navigation (null pour rester sur la même page).
     */
    public String addCustomAsset() {
        if (selectedPortfolioId == null) {
            return null;
        }

        boolean ok = webAppService.addAssetToPortfolio(
                selectedPortfolioId,
                assetName,
                assetType,
                assetQuantity,
                assetUnitValue,
                assetSymbol
        );

        if (ok) {
            loadAssets();

            // Réinitialisation de l'état du formulaire personnalisé.
            assetName = null;
            assetType = null;
            assetSymbol = null;
            assetQuantity = 0.0;
            assetUnitValue = 0.0;
            showCustomAssetForm = false;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // AJOUT D'UN ACTIF VIA L'API EXTERNE
    // -------------------------------------------------------------------------

    /**
     * Ajoute un actif récupéré depuis l'API externe au portefeuille sélectionné.
     *
     * @return navigation (null pour rester sur la même page).
     */
    public String addAssetFromApi() {
        if (selectedPortfolioId == null) {
            return null;
        }
        if (selectedExternalSymbol == null) {
            return null;
        }

        boolean ok = webAppService.addAssetToPortfolio(
                selectedPortfolioId,
                selectedExternalName,
                selectedType,
                assetQuantity,
                marketUnitPrice,
                selectedExternalSymbol
        );

        if (ok) {
            loadAssets();

            // Réinitialisation de la sélection et des valeurs.
            selectedExternalSymbol = null;
            selectedExternalApiId = null;
            selectedExternalName = null;
            assetQuantity = 0.0;
            marketUnitPrice = 0.0;
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // GETTERS / SETTERS EXPOSÉS A L'UI
    // -------------------------------------------------------------------------

    public Integer getSelectedPortfolioId() {
        return selectedPortfolioId;
    }

    public void setSelectedPortfolioId(Integer selectedPortfolioId) {
        this.selectedPortfolioId = selectedPortfolioId;
    }

    /**
     * Accès lazy à la liste d'actifs pour le portefeuille sélectionné.
     *
     * @return liste d'actifs, ou null si aucun portefeuille n'est sélectionné.
     */
    public List<String> getAssets() {
        if (assets == null && selectedPortfolioId != null) {
            loadAssets();
        }
        return assets;
    }

    public String getSelectedType() {
        return selectedType;
    }

    public void setSelectedType(String selectedType) {
        this.selectedType = selectedType;
    }

    public List<ExternalAsset> getAvailableAssets() {
        return availableAssets;
    }

    public double getMarketUnitPrice() {
        return marketUnitPrice;
    }

    public double getAssetQuantity() {
        return assetQuantity;
    }

    public void setAssetQuantity(double assetQuantity) {
        this.assetQuantity = assetQuantity;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getAssetSymbol() {
        return assetSymbol;
    }

    public void setAssetSymbol(String assetSymbol) {
        this.assetSymbol = assetSymbol;
    }

    public double getAssetUnitValue() {
        return assetUnitValue;
    }

    public void setAssetUnitValue(double assetUnitValue) {
        this.assetUnitValue = assetUnitValue;
    }
}
