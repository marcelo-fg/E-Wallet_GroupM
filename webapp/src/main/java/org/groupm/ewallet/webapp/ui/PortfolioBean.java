package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.groupm.ewallet.webapp.connector.ExternalAsset;
import org.groupm.ewallet.webapp.service.WebAppService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Backing bean JSF pour la gestion des portefeuilles et de leurs actifs.
 * Portee session car l'utilisateur navigue entre plusieurs ecrans
 * en conservant le portefeuille selectionne.
 */
@Named
@SessionScoped
public class PortfolioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private WebAppService webAppService;

    /** Identifiants des portefeuilles disponibles pour l'utilisateur courant. */
    private List<Integer> portfolioIds;

    /** Noms des portefeuilles alignes sur portfolioIds (meme index). */
    private List<String> portfolioNames;

    /** Identifiant du portefeuille selectionne dans l'UI. */
    private Integer selectedPortfolioId;

    /** Liste d'actifs du portefeuille selectionne. Pour l'instant representee par des String. */
    private List<String> assets;

    /** Nom saisi par l'utilisateur pour la creation d'un nouveau portefeuille. */
    private String newPortfolioName;

    // ---- API externe ----
    /** Type d'actif a charger depuis l'API externe: crypto / stock / etf. */
    private String selectedType;

    /** Symbole selectionne dans la liste externe (BTC, AAPL, SPY...). */
    private String selectedExternalSymbol;

    /** Identifiant API (pour les cryptos uniquement, ex: "bitcoin"). */
    private String selectedExternalApiId;

    /** Nom humain de l'actif externe (Bitcoin, Apple Inc., ...). */
    private String selectedExternalName;

    /** Liste d'actifs recuperes depuis l'API externe pour un type donne. */
    private List<ExternalAsset> availableAssets;

    // ---- Formulaire commun ----
    /** Quantite d'actif a ajouter au portefeuille. */
    private double assetQuantity;

    /** Prix unitaire marche recupere depuis l'API externe. */
    private double marketUnitPrice;

    /** Valeur unitaire saisie / utilisee pour l'ajout d'un actif (marche ou manuel). */
    private double assetUnitValue;

    // ---- Manuel ----
    /** Nom saisi manuellement pour un actif personnalise. */
    private String assetName;

    /** Type saisi manuellement pour un actif personnalise. */
    private String assetType;

    /** Symbole saisi manuellement pour un actif personnalise. */
    private String assetSymbol;

    // ---- Formulaire personnalise ----
    /** Indique si le formulaire d'actif personnalise est affiche. */
    private boolean showCustomAssetForm = false;

    // ----------------------------------------------------------
    // UI helpers
    // ----------------------------------------------------------

    public boolean isShowCustomAssetForm() {
        return showCustomAssetForm;
    }

    public void setShowCustomAssetForm(boolean showCustomAssetForm) {
        this.showCustomAssetForm = showCustomAssetForm;
    }

    /**
     * Bascule l'etat d'affichage du formulaire d'actif personnalise.
     * Utilise par le bouton "Creer un actif personnalise".
     */
    public void toggleCustomAssetForm() {
        this.showCustomAssetForm = !this.showCustomAssetForm;
    }

    // ----------------------------------------------------------
    // SESSION
    // ----------------------------------------------------------

    /**
     * Recupere l'identifiant fonctionnel de l'utilisateur courant depuis la session HTTP.
     * @return userId ou null si l'utilisateur n'est pas authentifie.
     */
    private String getUserIdFromSession() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        if (session == null) {
            return null;
        }
        return (String) session.getAttribute("userId");
    }

    // ----------------------------------------------------------
    // PORTFOLIOS
    // ----------------------------------------------------------

    /**
     * Retourne la liste des identifiants de portefeuilles pour l'utilisateur courant.
     * Chargement lazy via le service.
     */
    public List<Integer> getPortfolioIds() {
        if (portfolioIds == null) {
            loadPortfolios();
        }
        return portfolioIds;
    }

    /**
     * Retourne le nom du portefeuille courant a partir de selectedPortfolioId.
     * Utilise pour afficher "Portefeuille courant" avec le bon libelle.
     */
    public String getCurrentPortfolioName() {
        if (portfolioIds == null || portfolioNames == null || selectedPortfolioId == null) {
            return null;
        }
        int index = portfolioIds.indexOf(selectedPortfolioId);
        if (index < 0 || index >= portfolioNames.size()) {
            return null;
        }
        return portfolioNames.get(index);
    }

    /**
     * Recharge la liste des portefeuilles (ids + noms) pour l'utilisateur courant.
     * Pour l'instant, les noms sont des placeholders "Portefeuille <id>".
     * A remplacer quand le backend exposera les vrais noms.
     */
    public void loadPortfolios() {
        String userId = getUserIdFromSession();
        if (userId == null) {
            portfolioIds = List.of();
            portfolioNames = List.of();
            return;
        }

        // IDs recuperees via le service existant
        portfolioIds = webAppService.getPortfoliosForUser(userId);

        // Placeholder des noms : "Portefeuille <id>"
        List<String> names = new ArrayList<>();
        for (Integer id : portfolioIds) {
            names.add("Portefeuille " + id);
        }
        portfolioNames = names;
    }

    /**
     * Creation d'un nouveau portefeuille avec nom optionnel.
     * Utilise la surcharge du WebAppService pour prendre en compte le nom.
     */
    public String createPortfolio() {
        String userId = getUserIdFromSession();
        if (userId == null) {
            return null;
        }

        boolean created;
        if (newPortfolioName == null || newPortfolioName.isBlank()) {
            created = webAppService.createPortfolioForUser(userId);
        } else {
            created = webAppService.createPortfolioForUser(userId, newPortfolioName.trim());
        }

        if (created) {
            loadPortfolios();
            newPortfolioName = null;
        }

        return null;
    }

    /**
     * Charge les actifs pour le portefeuille selectionne.
     */
    public void loadAssets() {
        if (selectedPortfolioId == null) {
            assets = List.of();
            return;
        }
        assets = webAppService.getAssetsForPortfolio(selectedPortfolioId);
    }

    // ----------------------------------------------------------
    // API EXTERNE
    // ----------------------------------------------------------

    /**
     * Charge la liste d'actifs externes pour le type selectionne.
     */
    public void loadAssetsFromApi() {
        if (selectedType == null || selectedType.isBlank()) {
            availableAssets = List.of();
            return;
        }

        availableAssets = webAppService.loadAssetsFromApi(selectedType);

        // Reset de la selection courante et du prix.
        selectedExternalSymbol = null;
        selectedExternalApiId = null;
        selectedExternalName = null;
        marketUnitPrice = 0.0;
    }

    public String getSelectedExternalAsset() {
        return selectedExternalSymbol;
    }

    /**
     * Met a jour l'actif externe selectionne et met en cache les metadonnees utiles.
     */
    public void setSelectedExternalAsset(String symbol) {
        this.selectedExternalSymbol = symbol;

        if (availableAssets == null) {
            return;
        }

        for (ExternalAsset ea : availableAssets) {
            if (ea.getSymbol().equalsIgnoreCase(symbol)) {
                this.selectedExternalApiId = ea.getApiId();   // crypto only
                this.selectedExternalName = ea.getName();
                break;
            }
        }
    }

    /**
     * Appelle l'API marche pour recuperer le prix unitaire de l'actif selectionne.
     */
    public void loadPriceForSelectedAsset() {
        if (selectedExternalSymbol == null || selectedType == null) {
            return;
        }

        String idOrSymbol =
                selectedType.equalsIgnoreCase("crypto")
                        ? selectedExternalApiId
                        : selectedExternalSymbol;

        marketUnitPrice = webAppService.getPriceForAsset(idOrSymbol, selectedType);
        assetUnitValue = marketUnitPrice;
    }

    // ----------------------------------------------------------
    // ADD MANUAL ASSET (formulaire manuel)
    // ----------------------------------------------------------

    /**
     * Ajoute un actif saisi manuellement au portefeuille selectionne.
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
            assetName = null;
            assetType = null;
            assetSymbol = null;
            assetQuantity = 0.0;
            assetUnitValue = 0.0;
            loadAssets();
        }
        return null;
    }

    // ----------------------------------------------------------
    // ADD CUSTOM ASSET (pour le bouton personnalisé)
    // ----------------------------------------------------------

    /**
     * Ajoute un actif personnalise et masque le formulaire si OK.
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
            assetName = null;
            assetType = null;
            assetSymbol = null;
            assetQuantity = 0.0;
            assetUnitValue = 0.0;
            showCustomAssetForm = false;
        }
        return null;
    }

    // ----------------------------------------------------------
    // ADD API ASSET
    // ----------------------------------------------------------

    /**
     * Ajoute au portefeuille l'actif selectionne depuis l'API externe,
     * puis recharge la liste des actifs pour l'afficher a l'ecran.
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
            selectedExternalSymbol = null;
            selectedExternalApiId = null;
            selectedExternalName = null;
            assetQuantity = 0.0;
            marketUnitPrice = 0.0;
        }

        return null;
    }

    // ----------------------------------------------------------
    // GETTERS / SETTERS
    // ----------------------------------------------------------

    public Integer getSelectedPortfolioId() {
        return selectedPortfolioId;
    }

    public void setSelectedPortfolioId(Integer selectedPortfolioId) {
        this.selectedPortfolioId = selectedPortfolioId;
    }

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

    public String getNewPortfolioName() {
        return newPortfolioName;
    }

    public void setNewPortfolioName(String newPortfolioName) {
        this.newPortfolioName = newPortfolioName;
    }

    public List<String> getPortfolioNames() {
        return portfolioNames;
    }
}
