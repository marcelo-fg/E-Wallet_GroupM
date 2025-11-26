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

@Named
@SessionScoped
public class PortfolioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private WebAppService webAppService;

    private List<Integer> portfolioIds;
    private Integer selectedPortfolioId;

    private List<String> assets;

    // ---- API externe ----
    private String selectedType;               // crypto / stock / etf
    private String selectedExternalSymbol;     // BTC / AAPL / SPY...
    private String selectedExternalApiId;      // bitcoin (crypto only)
    private String selectedExternalName;       // Bitcoin / Apple Inc.
    private List<ExternalAsset> availableAssets;

    // ---- Formulaire commun ----
    private double assetQuantity;
    private double marketUnitPrice;
    private double assetUnitValue;

    // ---- Manuel ----
    private String assetName;
    private String assetType;
    private String assetSymbol;

    // ---- Formulaire personnalisé ----
    private boolean showCustomAssetForm = false;

    public boolean isShowCustomAssetForm() { return showCustomAssetForm; }
    public void setShowCustomAssetForm(boolean showCustomAssetForm) { this.showCustomAssetForm = showCustomAssetForm; }

    public void toggleCustomAssetForm() {
        this.showCustomAssetForm = !this.showCustomAssetForm;
    }

    // ----------------------------------------------------------
    // SESSION
    // ----------------------------------------------------------
    private String getUserIdFromSession() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        if (session == null) return null;
        return (String) session.getAttribute("userId");
    }

    // ----------------------------------------------------------
    // PORTFOLIOS
    // ----------------------------------------------------------
    public List<Integer> getPortfolioIds() {
        if (portfolioIds == null) loadPortfolios();
        return portfolioIds;
    }

    public void loadPortfolios() {
        String userId = getUserIdFromSession();
        if (userId == null) {
            portfolioIds = List.of();
            return;
        }
        portfolioIds = webAppService.getPortfoliosForUser(userId);
    }

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

    public String getSelectedExternalAsset() {
        return selectedExternalSymbol;
    }

    public void setSelectedExternalAsset(String symbol) {
        this.selectedExternalSymbol = symbol;

        if (availableAssets == null) return;

        for (ExternalAsset ea : availableAssets) {
            if (ea.getSymbol().equalsIgnoreCase(symbol)) {
                this.selectedExternalApiId = ea.getApiId(); // crypto only
                this.selectedExternalName = ea.getName();
                break;
            }
        }
    }

    public void loadPriceForSelectedAsset() {
        if (selectedExternalSymbol == null) return;

        String idOrSymbol =
                selectedType.equalsIgnoreCase("crypto")
                        ? selectedExternalApiId
                        : selectedExternalSymbol;

        marketUnitPrice = webAppService.getPriceForAsset(idOrSymbol, selectedType);
        assetUnitValue = marketUnitPrice;
    }

    // ----------------------------------------------------------
    // CREATE PORTFOLIO
    // ----------------------------------------------------------
    public String createPortfolio() {
        String userId = getUserIdFromSession();
        if (userId == null) return null;

        if (webAppService.createPortfolioForUser(userId)) {
            loadPortfolios();
        }
        return null;
    }

    // ----------------------------------------------------------
    // ADD MANUAL ASSET (formulaire manuel)
    // ----------------------------------------------------------
    public String addAsset() {
        if (selectedPortfolioId == null) return null;

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
            assetQuantity = 0;
            assetUnitValue = 0;
            loadAssets();
        }
        return null;
    }

    // ----------------------------------------------------------
    // ADD CUSTOM ASSET (pour le bouton personnalisé)
    // ----------------------------------------------------------
    public String addCustomAsset() {
        if (selectedPortfolioId == null) return null;

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
    public String addAssetFromApi() {
        if (selectedPortfolioId == null) return null;
        if (selectedExternalSymbol == null) return null;

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
    // GETTERS
    // ----------------------------------------------------------
    public Integer getSelectedPortfolioId() { return selectedPortfolioId; }
    public void setSelectedPortfolioId(Integer selectedPortfolioId) { this.selectedPortfolioId = selectedPortfolioId; }

    public List<String> getAssets() {
        if (assets == null && selectedPortfolioId != null) loadAssets();
        return assets;
    }

    public String getSelectedType() { return selectedType; }
    public void setSelectedType(String selectedType) { this.selectedType = selectedType; }

    public List<ExternalAsset> getAvailableAssets() { return availableAssets; }

    public double getMarketUnitPrice() { return marketUnitPrice; }

    public double getAssetQuantity() { return assetQuantity; }
    public void setAssetQuantity(double assetQuantity) { this.assetQuantity = assetQuantity; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public String getAssetType() { return assetType; }
    public void setAssetType(String assetType) { this.assetType = assetType; }

    public String getAssetSymbol() { return assetSymbol; }
    public void setAssetSymbol(String assetSymbol) { this.assetSymbol = assetSymbol; }

    public double getAssetUnitValue() { return assetUnitValue; }
    public void setAssetUnitValue(double assetUnitValue) { this.assetUnitValue = assetUnitValue; }
}