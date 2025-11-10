package org.example;

import org.example.model.Asset;
import org.example.model.Portfolio;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PortfolioTest {


    @Test
    void portfolioTotalValue() {
        Portfolio p = new Portfolio();
        p.addAsset(new Asset("Apple", "stock", 5, 200.0, "AAPL"));      // 1000
        p.addAsset(new Asset("SPY", "etf", 10, 450.0, "SPY"));          // 4500
        p.addAsset(new Asset("BTC", "crypto", 0.1, 60000.0, "bitcoin"));// 6000

        double expected = 1000 + 4500 + 6000; // 11500
        // (a) si tu as p.getTotalValue():
        // assertEquals(expected, p.getTotalValue(), 1e-6);

        // (b) sinon, recalcule via les assets:
        double total = p.getAssets().stream()
                .mapToDouble(a -> a.getQuantity() * a.getUnitValue())
                .sum();
        assertEquals(expected, total, 1e-6);
    }
}