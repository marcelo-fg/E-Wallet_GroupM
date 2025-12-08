package org.groupm.ewallet.webapp.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import org.groupm.ewallet.webapp.connector.ExternalAsset;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for Finnhub API integration (stock and ETF data).
 */
@ApplicationScoped
public class FinnhubService {

    private static final String API_URL = "https://finnhub.io/api/v1";
    private static final String API_KEY = System.getenv("FINNHUB_API_KEY");

    /**
     * Loads a list of US stocks from Finnhub.
     */
    public List<ExternalAsset> loadStockAssets() {
        try {
            if (API_KEY == null || API_KEY.isBlank()) {
                // Mock fallback
                return List.of(
                        new ExternalAsset("Apple Inc.", "AAPL", "AAPL"),
                        new ExternalAsset("Microsoft Corp.", "MSFT", "MSFT"),
                        new ExternalAsset("Tesla Inc.", "TSLA", "TSLA"),
                        new ExternalAsset("Amazon.com Inc.", "AMZN", "AMZN"),
                        new ExternalAsset("Google LLC", "GOOGL", "GOOGL"));
            }

            Client client = ClientBuilder.newClient();
            String url = API_URL + "/stock/symbol?exchange=US&token=" + API_KEY;

            WebTarget target = client.target(url);
            String json = target.request(MediaType.APPLICATION_JSON).get(String.class);

            var arr = Json.createReader(new StringReader(json)).readArray();
            List<ExternalAsset> list = new ArrayList<>();

            for (var v : arr) {
                var o = v.asJsonObject();

                String symbol = o.getString("symbol", null);
                String name = o.getString("description", null);

                if (symbol != null && name != null) {
                    list.add(new ExternalAsset(name, symbol, symbol));
                }
            }

            return list;

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * Loads a list of ETFs and fund-like instruments from Finnhub.
     * NOTE: Finnhub ETF endpoint requires premium subscription (returns HTTP 403).
     * Using mock data as fallback for all cases.
     */
    public List<ExternalAsset> loadEtfAssets() {
        // Always return mock data since Finnhub ETF endpoint requires premium
        // subscription
        return List.of(
                // Large Cap US Equity ETFs
                new ExternalAsset("SPDR S&P 500 ETF Trust", "SPY", "SPY"),
                new ExternalAsset("iShares Core S&P 500 ETF", "IVV", "IVV"),
                new ExternalAsset("Vanguard S&P 500 ETF", "VOO", "VOO"),
                new ExternalAsset("Invesco QQQ Trust", "QQQ", "QQQ"),
                new ExternalAsset("Vanguard Total Stock Market ETF", "VTI", "VTI"),
                new ExternalAsset("iShares Russell 1000 Growth ETF", "IWF", "IWF"),
                new ExternalAsset("Vanguard Growth ETF", "VUG", "VUG"),
                new ExternalAsset("iShares Core S&P Total US Stock Market ETF", "ITOT", "ITOT"),

                // Mid & Small Cap US Equity ETFs
                new ExternalAsset("iShares Russell 2000 ETF", "IWM", "IWM"),
                new ExternalAsset("Vanguard Mid-Cap ETF", "VO", "VO"),
                new ExternalAsset("iShares Core S&P Mid-Cap ETF", "IJH", "IJH"),
                new ExternalAsset("Vanguard Small-Cap ETF", "VB", "VB"),
                new ExternalAsset("iShares Core S&P Small-Cap ETF", "IJR", "IJR"),

                // International Equity ETFs
                new ExternalAsset("Vanguard FTSE Developed Markets ETF", "VEA", "VEA"),
                new ExternalAsset("iShares MSCI EAFE ETF", "EFA", "EFA"),
                new ExternalAsset("Vanguard FTSE Emerging Markets ETF", "VWO", "VWO"),
                new ExternalAsset("iShares Core MSCI Emerging Markets ETF", "IEMG", "IEMG"),
                new ExternalAsset("iShares MSCI Emerging Markets ETF", "EEM", "EEM"),
                new ExternalAsset("Vanguard Total International Stock ETF", "VXUS", "VXUS"),
                new ExternalAsset("iShares Core MSCI Total International Stock ETF", "IXUS", "IXUS"),

                // Sector ETFs
                new ExternalAsset("Technology Select Sector SPDR Fund", "XLK", "XLK"),
                new ExternalAsset("Financial Select Sector SPDR Fund", "XLF", "XLF"),
                new ExternalAsset("Health Care Select Sector SPDR Fund", "XLV", "XLV"),
                new ExternalAsset("Consumer Discretionary Select Sector SPDR", "XLY", "XLY"),
                new ExternalAsset("Energy Select Sector SPDR Fund", "XLE", "XLE"),
                new ExternalAsset("Industrial Select Sector SPDR Fund", "XLI", "XLI"),
                new ExternalAsset("Utilities Select Sector SPDR Fund", "XLU", "XLU"),
                new ExternalAsset("Real Estate Select Sector SPDR Fund", "XLRE", "XLRE"),
                new ExternalAsset("Consumer Staples Select Sector SPDR", "XLP", "XLP"),
                new ExternalAsset("Materials Select Sector SPDR Fund", "XLB", "XLB"),
                new ExternalAsset("Communication Services Select Sector SPDR", "XLC", "XLC"),

                // Bond ETFs
                new ExternalAsset("iShares Core US Aggregate Bond ETF", "AGG", "AGG"),
                new ExternalAsset("Vanguard Total Bond Market ETF", "BND", "BND"),
                new ExternalAsset("iShares iBoxx $ Investment Grade Corporate Bond ETF", "LQD", "LQD"),
                new ExternalAsset("iShares 20+ Year Treasury Bond ETF", "TLT", "TLT"),
                new ExternalAsset("iShares 7-10 Year Treasury Bond ETF", "IEF", "IEF"),
                new ExternalAsset("Vanguard Short-Term Bond ETF", "BSV", "BSV"),
                new ExternalAsset("iShares iBoxx $ High Yield Corporate Bond ETF", "HYG", "HYG"),
                new ExternalAsset("SPDR Bloomberg High Yield Bond ETF", "JNK", "JNK"),

                // Dividend ETFs
                new ExternalAsset("Vanguard Dividend Appreciation ETF", "VIG", "VIG"),
                new ExternalAsset("iShares Select Dividend ETF", "DVY", "DVY"),
                new ExternalAsset("Vanguard High Dividend Yield ETF", "VYM", "VYM"),
                new ExternalAsset("SPDR S&P Dividend ETF", "SDY", "SDY"),
                new ExternalAsset("Schwab US Dividend Equity ETF", "SCHD", "SCHD"),

                // Growth & Value ETFs
                new ExternalAsset("Vanguard Value ETF", "VTV", "VTV"),
                new ExternalAsset("iShares Russell 1000 Value ETF", "IWD", "IWD"),
                new ExternalAsset("iShares S&P 500 Growth ETF", "IVW", "IVW"),

                // Commodity & Gold ETFs
                new ExternalAsset("SPDR Gold Trust", "GLD", "GLD"),
                new ExternalAsset("iShares Gold Trust", "IAU", "IAU"),
                new ExternalAsset("Invesco DB Commodity Index Tracking Fund", "DBC", "DBC"),
                new ExternalAsset("United States Oil Fund", "USO", "USO"),

                // Other Popular ETFs
                new ExternalAsset("ARK Innovation ETF", "ARKK", "ARKK"),
                new ExternalAsset("iShares Biotechnology ETF", "IBB", "IBB"),
                new ExternalAsset("VanEck Semiconductor ETF", "SMH", "SMH"),
                new ExternalAsset("iShares US Real Estate ETF", "IYR", "IYR"));
    }

    /**
     * Requests the current price of a stock or ETF from Finnhub.
     */
    public double getStockEtfPrice(String symbol) {
        try {
            if (API_KEY == null || API_KEY.isBlank()) {
                // Generate a pseudo-random price based on hash code to be consistent
                return Math.abs(symbol.hashCode() % 500) + 50.0;
            }

            Client client = ClientBuilder.newClient();
            String url = API_URL + "/quote?symbol=" + symbol + "&token=" + API_KEY;

            WebTarget target = client.target(url);
            String json = target.request(MediaType.APPLICATION_JSON).get(String.class);

            var obj = Json.createReader(new StringReader(json)).readObject();

            if (obj.containsKey("c")) {
                return obj.getJsonNumber("c").doubleValue();
            }

            return 0.0;

        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }

    /**
     * Requests historical prices for a stock/ETF asset using Candle endpoint.
     * 
     * @param symbol The ticker symbol
     * @param days   Number of days of history (converted to 'count' or 'resolution'
     *               logic for Finnhub)
     *               Note: Finnhub free tier has limits. Using '/stock/candle'.
     */
    public List<Double> getHistoricalStockPrice(String symbol, int days) {
        try {
            if (API_KEY == null || API_KEY.isBlank()) {
                return new ArrayList<>();
            }

            // Finnhub Candle: /stock/candle?symbol=AAPL&resolution=D&from=...&to=...
            // Resolution 'D' = Daily.
            long to = System.currentTimeMillis() / 1000;
            long from = to - (days * 86400L);

            Client client = ClientBuilder.newClient();
            String url = API_URL + "/stock/candle?symbol=" + symbol + "&resolution=D&from=" + from + "&to=" + to
                    + "&token=" + API_KEY;

            WebTarget target = client.target(url);
            String json = target.request(MediaType.APPLICATION_JSON).get(String.class);

            var reader = Json.createReader(new StringReader(json));
            var obj = reader.readObject();
            reader.close();

            List<Double> prices = new ArrayList<>();

            if (obj.containsKey("c")) {
                // "c" is an array of close prices
                var closeArray = obj.getJsonArray("c");
                for (var p : closeArray) {
                    prices.add(p.toString().equals("null") ? 0.0 : Double.parseDouble(p.toString()));
                }
            }
            return prices;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
