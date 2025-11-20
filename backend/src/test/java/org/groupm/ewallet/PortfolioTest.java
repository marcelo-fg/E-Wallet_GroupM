package org.groupm.ewallet;

import org.groupm.ewallet.model.Asset;
import org.groupm.ewallet.model.Portfolio;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PortfolioTest {

    @Test
    void portfolioTotalValue() {
        Portfolio p = new Portfolio();

        // Ajout des actifs
        p.addAsset(new Asset("Apple", "stock", 5, 200.0, "AAPL"));      // 1000
        p.addAsset(new Asset("SPY", "etf", 10, 450.0, "SPY"));          // 4500
        p.addAsset(new Asset("BTC", "crypto", 0.1, 60000.0, "bitcoin"));// 6000

        // Affichage des détails
        System.out.println("=== Détail du portefeuille ===");
        p.getAssets().forEach(a -> {
            double value = a.getQuantity() * a.getUnitValue();
            System.out.println(a.getAssetName() + " (" + a.getType() + "): "
                    + a.getQuantity() + " × " + a.getUnitValue() + " = " + value);
        });

        double expected = 1000 + 4500 + 6000; // 11500

        // Calcul total
        double total = p.getAssets().stream()
                .mapToDouble(a -> a.getQuantity() * a.getUnitValue())
                .sum();

        System.out.println("Valeur totale attendue : " + expected);
        System.out.println("Valeur totale calculée : " + total);

        assertEquals(expected, total, 1e-6);
    }
}