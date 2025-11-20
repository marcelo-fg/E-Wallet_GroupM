package org.groupm.ewallet.model;

/**
 * Représente un actif financier (action, crypto, ETF, etc.).
 * Chaque actif possède un symbole, un type, un nom, un prix unitaire et une quantité détenue.
 */
public class Asset {

    /** Symbole unique de l’actif, par exemple "AAPL" ou "bitcoin". */
    private String symbol;

    /** Type d’actif, par exemple "stock", "crypto" ou "etf". */
    private String type;

    /** Nom complet de l’actif. */
    private String assetName;

    /** Valeur unitaire de l’actif, en CHF ou en USD selon le contexte. */
    private double unitValue;

    /** Quantité détenue de cet actif. */
    private double quantity;
    private int portfolioID;

    /**
     * Constructeur par défaut requis pour la désérialisation JSON.
     */
    public Asset() {
    }

    /**
     * Constructeur principal de la classe Asset.
     * Utilisé notamment dans les services de marché pour initialiser un actif avec ses caractéristiques.
     *
     * @param symbol Symbole unique de l’actif (ex. : "AAPL").
     * @param type Type de l’actif (ex. : "stock", "crypto").
     * @param assetName Nom complet de l’actif.
     * @param unitValue Valeur unitaire initiale.
     */
    public Asset(String symbol, String type, String assetName, double unitValue) {
        this.symbol = symbol;
        this.type = type;
        this.assetName = assetName;
        this.unitValue = unitValue;
        this.quantity = 0.0;
        this.portfolioID = 0;
    }

    /**
     * Constructeur alternatif compatible avec le programme principal.
     *
     * @param assetName Nom complet de l’actif.
     * @param type Type de l’actif.
     * @param quantity Quantité détenue.
     * @param unitValue Valeur unitaire.
     * @param symbol Symbole unique de l’actif.
     */
    public Asset(String assetName, String type, double quantity, double unitValue, String symbol) {
        this.assetName = assetName;
        this.type = type;
        this.quantity = quantity;
        this.unitValue = unitValue;
        this.symbol = symbol;
        this.portfolioID = 0;
    }

    /**
     * Constructeur simplifié pour créer un actif sans valeur initiale.
     *
     * @param symbol Symbole de l’actif.
     * @param type Type de l’actif.
     * @param assetName Nom complet de l’actif.
     */
    public Asset(String symbol, String type, String assetName) {
        this(symbol, type, assetName, 0.0);
        this.portfolioID = 0;
    }

    // ===================== Getters et Setters =====================

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public double getUnitValue() {
        return unitValue;
    }

    public void setUnitValue(double unitValue) {
        this.unitValue = unitValue;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public int getPortfolioID() {
        return portfolioID;
    }

    public void setPortfolioID(int portfolioID) {
        this.portfolioID = portfolioID;
    }

    // ===================== Méthodes utilitaires =====================

    /**
     * Calcule la valeur totale de l’actif détenu.
     *
     * @return Valeur totale = quantité × valeur unitaire.
     */
    public double getTotalValue() {
        return unitValue * quantity;
    }

    @Override
    public String toString() {
        return String.format("%s (%s): %.2f CHF x %.2f = %.2f CHF",
                assetName, symbol, unitValue, quantity, getTotalValue());
    }
}