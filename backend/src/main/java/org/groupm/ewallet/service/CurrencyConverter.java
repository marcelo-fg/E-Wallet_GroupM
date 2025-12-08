package org.groupm.ewallet.service;

/**
 * Simple utility for currency conversion.
 * Uses fixed exchange rate for demonstration purposes.
 * In production, this should use a real-time exchange rate API.
 */
public class CurrencyConverter {

    private static final double CHF_TO_USD_RATE = 1.12; // Example fixed rate
    private static final double USD_TO_CHF_RATE = 1.0 / CHF_TO_USD_RATE;

    /**
     * Converts CHF to USD.
     */
    public static double chfToUsd(double chf) {
        return chf * CHF_TO_USD_RATE;
    }

    /**
     * Converts USD to CHF.
     */
    public static double usdToChf(double usd) {
        return usd * USD_TO_CHF_RATE;
    }
}
