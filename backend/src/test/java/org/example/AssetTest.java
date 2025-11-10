package org.example;

import org.example.model.Asset;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AssetTest {

    @Test
    void createAssetAndValue() {
        Asset a = new Asset("Apple", "stock", 5.0, 185.5, "AAPL");
        assertEquals("stock", a.getType());
        assertEquals(5.0, a.getQuantity());
        assertEquals(185.5, a.getUnitValue());
        // valeur totale (simple check)
        double total = a.getQuantity() * a.getUnitValue();
        assertEquals(927.5, total, 1e-6);
    }

    @Test
    void updateQuantityAndPrice() {
        Asset a = new Asset("BTC", "crypto", 0.05, 65000.0, "bitcoin");
        a.setQuantity(0.06);
        a.setUnitValue(64000.0);
        assertEquals(0.06, a.getQuantity());
        assertEquals(64000.0, a.getUnitValue());
    }
}