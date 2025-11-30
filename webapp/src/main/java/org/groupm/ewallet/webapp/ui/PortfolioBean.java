package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.groupm.ewallet.webapp.connector.ExternalAsset;
import org.groupm.ewallet.webapp.service.WebAppService;

import java.io.Serializable;
import java.util.*;

/**
 * Session-scoped bean for managing portfolios and their assets.
 * Acts as presentation layer between JSF pages and the WebAppService.
 */
@Named
@SessionScoped
public class PortfolioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private WebAppService webAppService;

    /**
     * Technical identifiers of the current user's portfolios.
     */
    private List<Integer> portfolioIds;

    /**
     * Display names of portfolios, kept in session.
     * Key = portfolioId, value = human readable name.
     */
    private final Map<Integer, String> portfolioNames = new HashMap<>();

    /**
     * Currently selected portfolio id in the UI.
     */
    private Integer selectedPortfolioId;

    /**
     * Textual representation of assets for the selected portfolio
     * as returned by the backend.
     */
    private List<String> assets;

    // -------------------------------------------------------------------------
    // PORTFOLIO NAME (FORM FIELD)
    // -------------------------------------------------------------------------

    /**
     * Portfolio name entered by the user on creation.
     */
    private String portfolioName;

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    // -------------------------------------------------------------------------
    // EXTERNAL API (BUY SIDE)
    // -------------------------------------------------------------------------

    private String selectedType;
    private String selectedExternalSymbol;
    private String selectedExternalApiId;
    private String selectedExternalName;
    private List<ExternalAsset> availableAssets;

    // -------------------------------------------------------------------------
    // COMMON ASSET FORM (BUY / CUSTOM)
    // -------------------------------------------------------------------------

    private double assetQuantity;
    private double marketUnitPrice;
    private double assetUnitValue;

    // -------------------------------------------------------------------------
    // MANUAL ASSET FIELDS
    // -------------------------------------------------------------------------

    private String assetName;
    private String assetType;
    private String assetSymbol;

    // -------------------------------------------------------------------------
    // CUSTOM ASSET FORM TOGGLE
    // -------------------------------------------------------------------------

    private boolean showCustomAssetForm = true;

    public boolean isShowCustomAssetForm() {
        return showCustomAssetForm;
    }

    public void setShowCustomAssetForm(boolean showCustomAssetForm) {
        this.showCustomAssetForm = showCustomAssetForm;
    }

    /**
     * Toggles visibility of the custom asset form.
     */
    public void toggleCustomAssetForm() {
        this.showCustomAssetForm = !this.showCustomAssetForm;
    }

    // -------------------------------------------------------------------------
    // SELL SIDE (IN-MEMORY ONLY)
    // -------------------------------------------------------------------------

    /**
     * Symbol parsed from the held asset line, used for price lookup.
     */
    private String selectedHeldSymbol;

    /**
     * Human readable name or full line of the held asset selected for selling.
     */
    private String selectedHeldName;

    /**
     * Quantity the user wants to sell from the held asset.
     */
    private double sellQuantity;

    /**
     * Market price used for the sell transaction.
     */
    private double sellMarketPrice;

    /**
     * Cached list of trades for the selected portfolio, used to display history.
     */
    private List<WebAppService.PortfolioTrade> tradeHistory;

    // -------------------------------------------------------------------------
    // SESSION HANDLING
    // -------------------------------------------------------------------------

    /**
     * Retrieves the current user id from HTTP session.
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
     * Lazy access to the user portfolio ids.
     */
    public List<Integer> getPortfolioIds() {
        if (portfolioIds == null) {
            loadPortfolios();
        }
        return portfolioIds;
    }

    /**
     * Loads portfolio ids for the current user and initializes default names.
     */
    public void loadPortfolios() {
        String userId = getUserIdFromSession();
        if (userId == null) {
            portfolioIds = List.of();
            return;
        }
        portfolioIds = webAppService.getPortfoliosForUser(userId);
        for (Integer id : portfolioIds) {
            portfolioNames.putIfAbsent(id, "Portfolio " + id);
        }
    }

    /**
     * Loads asset list and trade history for the currently selected portfolio.
     */
    public void loadAssets() {
        if (selectedPortfolioId == null) {
            assets = List.of();
            tradeHistory = List.of();
            return;
        }
        assets = webAppService.getAssetsForPortfolio(selectedPortfolioId);
        tradeHistory = webAppService.getTradesForPortfolio(selectedPortfolioId);
    }

    /**
     * Returns a JSF-friendly list of portfolio id + label for the current user.
     */
    public List<WebAppService.PortfolioInfo> getPortfolioInfos() {
        List<WebAppService.PortfolioInfo> out = new ArrayList<>();
        if (getPortfolioIds() == null) {
            return out;
        }
        for (Integer id : portfolioIds) {
            String name = portfolioNames.getOrDefault(id, "Portfolio " + id);
            out.add(new WebAppService.PortfolioInfo(id, name));
        }
        return out;
    }

    /**
     * Returns the display name for the currently selected portfolio.
     */
    public String getSelectedPortfolioLabel() {
        if (selectedPortfolioId == null) {
            return null;
        }
        return portfolioNames.getOrDefault(selectedPortfolioId, "Portfolio " + selectedPortfolioId);
    }

    // -------------------------------------------------------------------------
    // EXTERNAL API - LOADING AND SELECTION (BUY)
    // -------------------------------------------------------------------------

    /**
     * Loads available assets from external API based on selected type.
     */
    public void loadAssetsFromApi() {
        if (selectedType == null || selectedType.isBlank()) {
            availableAssets = List.of();
            selectedExternalSymbol = null;
            selectedExternalApiId = null;
            selectedExternalName = null;
            marketUnitPrice = 0.0;
            assetUnitValue = 0.0;
            return;
        }

        availableAssets = webAppService.loadAssetsFromApi(selectedType);

        selectedExternalSymbol = null;
        selectedExternalApiId = null;
        selectedExternalName = null;
        marketUnitPrice = 0.0;
        assetUnitValue = 0.0;
    }

    public String getSelectedExternalAsset() {
        return selectedExternalSymbol;
    }

    /**
     * Called when the user selects an external asset from the combo box.
     * Resolves API id and name, then refreshes the market price.
     */
    public void setSelectedExternalAsset(String symbol) {
        this.selectedExternalSymbol = symbol;

        if (availableAssets == null) {
            return;
        }

        this.selectedExternalApiId = null;
        this.selectedExternalName = null;

        for (ExternalAsset ea : availableAssets) {
            if (ea.getSymbol().equalsIgnoreCase(symbol)) {
                this.selectedExternalApiId = ea.getApiId();
                this.selectedExternalName = ea.getName();
                break;
            }
        }

        loadPriceForSelectedAsset();
    }

    /**
     * Loads market price for the currently selected external asset.
     * Keeps unit value aligned with the market price.
     */
    public void loadPriceForSelectedAsset() {
        if (selectedExternalSymbol == null || selectedType == null) {
            marketUnitPrice = 0.0;
            assetUnitValue = 0.0;
            return;
        }

        String idOrSymbol =
                selectedType.equalsIgnoreCase("crypto")
                        ? selectedExternalApiId
                        : selectedExternalSymbol;

        if (idOrSymbol == null || idOrSymbol.isBlank()) {
            marketUnitPrice = 0.0;
            assetUnitValue = 0.0;
            return;
        }

        double price = webAppService.getPriceForAsset(idOrSymbol, selectedType);
        if (price <= 0.0) {
            // Do not overwrite a previous valid price with zero because of a transient API issue.
            return;
        }

        marketUnitPrice = price;
        assetUnitValue = marketUnitPrice;
    }

    // -------------------------------------------------------------------------
    // PORTFOLIO CREATION
    // -------------------------------------------------------------------------

    /**
     * Creates a new portfolio for the current user and associates a display name.
     */
    public String createPortfolio() {
        String userId = getUserIdFromSession();
        if (userId == null) {
            return null;
        }

        boolean created = webAppService.createPortfolioForUser(userId);

        if (created) {
            loadPortfolios();
            if (!portfolioIds.isEmpty()) {
                Integer newestId = portfolioIds.get(portfolioIds.size() - 1);
                String name = (portfolioName != null && !portfolioName.isBlank())
                        ? portfolioName
                        : "Portfolio " + newestId;
                portfolioNames.put(newestId, name);
                selectedPortfolioId = newestId;
            }
            portfolioName = null;
            loadAssets();
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // MANUAL ASSET ADD
    // -------------------------------------------------------------------------

    /**
     * Adds a manually defined asset to the selected portfolio.
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

    /**
     * Adds a custom asset and hides the custom form after success.
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

    // -------------------------------------------------------------------------
    // ADD ASSET FROM EXTERNAL API (BUY)
    // -------------------------------------------------------------------------

    /**
     * Adds the selected external asset to the current portfolio
     * and records a BUY trade in memory.
     */
    public String addAssetFromApi() {
        if (selectedPortfolioId == null) {
            return null;
        }
        if (selectedExternalSymbol == null || selectedExternalName == null) {
            return null;
        }

        if (assetQuantity <= 0.0) {
            return null;
        }

        if (marketUnitPrice <= 0.0) {
            loadPriceForSelectedAsset();
        }
        if (marketUnitPrice <= 0.0) {
            // Still no valid price, do not save a zero-priced asset.
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
            // Record BUY trade for PnL history.
            webAppService.recordPortfolioTrade(
                    selectedPortfolioId,
                    selectedExternalName,
                    selectedExternalSymbol,
                    "BUY",
                    assetQuantity,
                    marketUnitPrice
            );

            loadAssets();

            selectedExternalSymbol = null;
            selectedExternalApiId = null;
            selectedExternalName = null;
            assetQuantity = 0.0;
            marketUnitPrice = 0.0;
            assetUnitValue = 0.0;
        }

        return null;
    }

    // -------------------------------------------------------------------------
    // SELL ASSET AT MARKET PRICE (IN-MEMORY)
    // -------------------------------------------------------------------------

    /**
     * Called when the user selects a held asset line to sell in the UI.
     * The line is parsed to extract the symbol between parentheses.
     */
    public void setSelectedHeldSymbol(String line) {
        this.selectedHeldSymbol = null;
        this.selectedHeldName = null;
        this.sellMarketPrice = 0.0;

        if (line == null || line.isBlank()) {
            return;
        }

        // Keep full line as display name
        this.selectedHeldName = line;

        // Example line: "BNB (BNB) : 2.00 × 0.00 = 0.00"
        int open = line.indexOf('(');
        int close = line.indexOf(')');
        if (open >= 0 && close > open) {
            String symbol = line.substring(open + 1, close).trim();
            if (!symbol.isBlank()) {
                this.selectedHeldSymbol = symbol;
            }
        }

        if (this.selectedHeldSymbol == null) {
            // Fallback if parsing failed
            this.selectedHeldSymbol = line;
        }

        refreshSellPrice();
    }

    public String getSelectedHeldSymbol() {
        return selectedHeldSymbol;
    }

    /**
     * Refreshes the market price for the currently selected held asset.
     * Uses the same type as the last external selection when possible,
     * defaults to "stock" as a safer generic type.
     */
    public void refreshSellPrice() {
        if (selectedHeldSymbol == null || selectedHeldSymbol.isBlank()) {
            sellMarketPrice = 0.0;
            return;
        }
        String typeToUse = (selectedType != null && !selectedType.isBlank())
                ? selectedType
                : "stock";
        double price = webAppService.getPriceForAsset(selectedHeldSymbol, typeToUse);
        if (price > 0.0) {
            sellMarketPrice = price;
        }
    }

    /**
     * Sells a given quantity of the selected held asset at market price.
     * This only records an in-memory trade for PnL and global history; it does
     * not currently update backend-held quantities (backend API is missing).
     */
    public String sellAssetAtMarketPrice() {
        if (selectedPortfolioId == null) {
            return null;
        }
        if (selectedHeldSymbol == null || sellQuantity <= 0.0) {
            return null;
        }

        if (sellMarketPrice <= 0.0) {
            refreshSellPrice();
        }
        if (sellMarketPrice <= 0.0) {
            return null;
        }

        String name = (selectedHeldName != null) ? selectedHeldName : selectedHeldSymbol;

        webAppService.recordPortfolioTrade(
                selectedPortfolioId,
                name,
                selectedHeldSymbol,
                "SELL",
                sellQuantity,
                sellMarketPrice
        );

        // In a full implementation, a backend call should update remaining quantity here.

        loadAssets();
        sellQuantity = 0.0;

        return null;
    }

    // -------------------------------------------------------------------------
    // TRADE HISTORY ACCESSORS
    // -------------------------------------------------------------------------

    /**
     * Returns the in-memory trade history for the selected portfolio.
     */
    public List<WebAppService.PortfolioTrade> getTradeHistory() {
        if (selectedPortfolioId == null) {
            return List.of();
        }
        if (tradeHistory == null) {
            tradeHistory = webAppService.getTradesForPortfolio(selectedPortfolioId);
        }
        return tradeHistory;
    }

    /**
     * Realized PnL calculated per symbol using FIFO matching of BUY and SELL lots.
     * If buy and sell prices are identical for the matched quantity, PnL is zero.
     */
    public double getRealizedPnl() {
        if (selectedPortfolioId == null) {
            return 0.0;
        }

        List<WebAppService.PortfolioTrade> trades = getTradeHistory();
        // Open positions by symbol: queue of [quantity, price] lots
        Map<String, Deque<double[]>> openPositions = new HashMap<>();
        double realizedPnl = 0.0;

        for (WebAppService.PortfolioTrade t : trades) {
            String symbol = t.getSymbol();
            if (symbol == null) {
                continue;
            }
            openPositions.putIfAbsent(symbol, new ArrayDeque<>());
            Deque<double[]> lots = openPositions.get(symbol);

            if ("BUY".equalsIgnoreCase(t.getType())) {
                // Add a buy lot [qty, price]
                lots.addLast(new double[]{t.getQuantity(), t.getUnitPrice()});
            } else if ("SELL".equalsIgnoreCase(t.getType())) {
                double qtyToSell = t.getQuantity();
                double sellPrice = t.getUnitPrice();

                while (qtyToSell > 0 && !lots.isEmpty()) {
                    double[] lot = lots.peekFirst();
                    double lotQty = lot[0];
                    double lotPrice = lot[1];

                    double matchedQty = Math.min(qtyToSell, lotQty);

                    // PnL on the matched part of this lot
                    realizedPnl += (sellPrice - lotPrice) * matchedQty;

                    lotQty -= matchedQty;
                    qtyToSell -= matchedQty;

                    if (lotQty <= 0.0) {
                        lots.removeFirst();
                    } else {
                        lot[0] = lotQty;
                    }
                }
                // Any remaining qtyToSell is ignored for this prototype.
            }
        }

        return realizedPnl;
    }

    // -------------------------------------------------------------------------
    // GETTERS / SETTERS EXPOSED TO UI
    // -------------------------------------------------------------------------

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

    public double getSellQuantity() {
        return sellQuantity;
    }

    public void setSellQuantity(double sellQuantity) {
        this.sellQuantity = sellQuantity;
    }

    public double getSellMarketPrice() {
        return sellMarketPrice;
    }
}
