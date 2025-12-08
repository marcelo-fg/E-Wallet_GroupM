package org.groupm.ewallet.webapp.service;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Price caching service that stores asset prices on-demand.
 * This reduces API calls to external services and provides consistent pricing
 * even when APIs are rate-limited or require premium subscriptions.
 * 
 * Cache operates in lazy mode: prices are fetched only when requested.
 */
@Singleton
@Startup
public class PriceCacheService {

    @Inject
    private MarketDataService marketData;

    /**
     * Cache structure: Map of "type:symbol" to CachedPrice
     * Example: "crypto:bitcoin" maps to CachedPrice with price and timestamp
     */
    private final Map<String, CachedPrice> priceCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        System.out.println("[PriceCacheService] Initialized in lazy mode (on-demand caching)");
    }

    /**
     * Gets a cached price for an asset. If not in cache, fetches from API and
     * caches it.
     */
    public double getCachedPrice(String symbol, String type) {
        String key = type + ":" + symbol.toLowerCase();

        CachedPrice cached = priceCache.get(key);
        if (cached != null && !cached.isExpired()) {
            return cached.price;
        }

        // Not in cache or expired, fetch fresh price
        double freshPrice = marketData.getPriceForAsset(symbol, type);
        if (freshPrice > 0.0) {
            priceCache.put(key, new CachedPrice(freshPrice, LocalDateTime.now()));
        }

        return freshPrice;
    }

    /**
     * Manually adds a price to the cache.
     */
    public void setCachedPrice(String symbol, String type, double price) {
        String key = type + ":" + symbol.toLowerCase();
        priceCache.put(key, new CachedPrice(price, LocalDateTime.now()));
    }

    /**
     * Gets cache statistics for monitoring.
     */
    public String getCacheStats() {
        return String.format("Cache Size: %d | Sample: %s",
                priceCache.size(),
                priceCache.keySet().stream().limit(5).toList());
    }

    /**
     * Inner class to hold cached price data with timestamp.
     */
    private static class CachedPrice {
        final double price;
        final LocalDateTime lastUpdate;

        CachedPrice(double price, LocalDateTime lastUpdate) {
            this.price = price;
            this.lastUpdate = lastUpdate;
        }

        /**
         * Considers price expired after 5 minutes.
         */
        boolean isExpired() {
            return lastUpdate.plusMinutes(5).isBefore(LocalDateTime.now());
        }
    }
}
