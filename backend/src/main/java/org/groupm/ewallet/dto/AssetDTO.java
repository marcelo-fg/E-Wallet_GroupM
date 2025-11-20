package org.groupm.ewallet.dto;

/**
 * DTO pour les données des actifs financiers.
 */
public class AssetDTO {
    private String symbol;
    private String type;
    private String assetName;
    private double unitValue;
    private double quantity;

    // Getters/setters

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getAssetName() { return assetName; }
    public void setAssetName(String assetName) { this.assetName = assetName; }

    public double getUnitValue() { return unitValue; }
    public void setUnitValue(double unitValue) { this.unitValue = unitValue; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
}
