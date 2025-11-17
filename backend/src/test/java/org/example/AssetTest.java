package org.example;

import org.example.model.Asset;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AssetTest {

    @Test
    void createAssetAndValue() {
        Asset a = new Asset("Apple", "stock", 5.0, 185.5, "AAPL");
        double total = a.getQuantity() * a.getUnitValue();

        System.out.println("Création de l’actif : " + a.getAssetName());
        System.out.println("Type : " + a.getType());
        System.out.println("Quantité : " + a.getQuantity());
        System.out.println("Valeur unitaire : " + a.getUnitValue());
        System.out.println("Valeur totale calculée : " + a.getQuantity() + " × " + a.getUnitValue() + " = " + total);

        assertEquals("stock", a.getType());
        assertEquals(5.0, a.getQuantity());
        assertEquals(185.5, a.getUnitValue());
        assertEquals(927.5, total, 1e-6);
    }

    @Test
    void updateQuantityAndPrice() {
        Asset a = new Asset("BTC", "crypto", 0.05, 65000.0, "bitcoin");

        double initialQuantity = a.getQuantity();
        double initialPrice = a.getUnitValue();

        a.setQuantity(0.06);
        a.setUnitValue(64000.0);
        double updatedTotal = a.getQuantity() * a.getUnitValue();

        System.out.println("Mise à jour de l’actif : " + a.getAssetName());
        System.out.println("Quantité initiale : " + initialQuantity + " → nouvelle : " + a.getQuantity());
        System.out.println("Prix unitaire initial : " + initialPrice + " → nouveau : " + a.getUnitValue());
        System.out.println("Nouvelle valeur totale : " + a.getQuantity() + " × " + a.getUnitValue() + " = " + updatedTotal);

        assertEquals(0.06, a.getQuantity());
        assertEquals(64000.0, a.getUnitValue());
    }
}