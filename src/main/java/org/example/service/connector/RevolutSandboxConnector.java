package org.example.service.connector;

import org.example.model.Account;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RevolutSandboxConnector {

    // ✅ Méthode publique pour charger les comptes mock depuis un fichier JSON
    public List<Account> loadMockAccounts() {
        List<Account> accounts = new ArrayList<>();
        try (InputStream is = getClass().getResourceAsStream("/revolut_mock.json")) {
            if (is == null) {
                System.err.println("❌ Fichier revolut_mock.json introuvable !");
                return accounts;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(is);
            for (JsonNode node : root.path("accounts")) {
                String id = node.path("accountID").asText();
                String type = node.path("type").asText();
                double balance = node.path("balance").asDouble();
                accounts.add(new Account(id, type, balance));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du mock Revolut : " + e.getMessage());
        }
        return accounts;
    }
}