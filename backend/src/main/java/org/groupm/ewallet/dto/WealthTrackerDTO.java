package org.groupm.ewallet.dto;

/**
 * DTO pour le suivi de richesse de l’utilisateur.
 */
public class WealthTrackerDTO {
    private String userID;
    private double totalWealthUsd;
    private double totalWealthChf;
    private double growthRate;

    // Getters/setters

    public String getUserID() { return userID; }
    public void setUserID(String userID) { this.userID = userID; }

    public double getTotalWealthUsd() { return totalWealthUsd; }
    public void setTotalWealthUsd(double totalWealthUsd) { this.totalWealthUsd = totalWealthUsd; }

    public double getTotalWealthChf() { return totalWealthChf; }
    public void setTotalWealthChf(double totalWealthChf) { this.totalWealthChf = totalWealthChf; }

    public double getGrowthRate() { return growthRate; }
    public void setGrowthRate(double growthRate) { this.growthRate = growthRate; }
}
