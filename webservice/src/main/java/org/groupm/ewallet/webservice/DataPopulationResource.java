package org.groupm.ewallet.webservice;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.groupm.ewallet.model.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * REST resource for populating the database with demo data.
 * Used for Phase 3 evaluation to demonstrate 1000+ records per table.
 */
@Path("/admin")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class DataPopulationResource {

    // International first names (French, German, English, Italian, Spanish)
    private static final String[] FIRST_NAMES = {
            // English
            "James", "Mary", "John", "Emma", "Michael", "Olivia", "William", "Sophia",
            "David", "Isabella", "Richard", "Mia", "Joseph", "Charlotte", "Thomas", "Amelia",
            // French
            "Jean", "Marie", "Pierre", "Camille", "Louis", "Léa", "Antoine", "Manon",
            "Nicolas", "Chloé", "François", "Julie", "Laurent", "Aurélie", "Mathieu", "Céline",
            // German
            "Hans", "Anna", "Klaus", "Lena", "Stefan", "Julia", "Andreas", "Laura",
            "Markus", "Sarah", "Tobias", "Lisa", "Florian", "Katharina", "Lukas", "Sophie",
            // Italian
            "Marco", "Giulia", "Luca", "Francesca", "Alessandro", "Chiara", "Andrea", "Sara",
            "Matteo", "Valentina", "Lorenzo", "Alessia", "Davide", "Martina", "Federico", "Elena",
            // Spanish
            "Carlos", "María", "Miguel", "Carmen", "Pablo", "Ana", "Diego", "Lucía",
            "Alejandro", "Paula", "Daniel", "Laura", "Javier", "Marta", "Roberto", "Isabel"
    };

    // International last names
    private static final String[] LAST_NAMES = {
            // English
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Wilson", "Taylor", "Anderson",
            "Thomas", "Jackson", "White", "Harris", "Martin", "Thompson", "Moore", "Clark",
            // French
            "Dubois", "Moreau", "Laurent", "Bernard", "Petit", "Leroy", "Roux", "Girard",
            "Bonnet", "Dupont", "Lambert", "Fontaine", "Rousseau", "Vincent", "Muller", "Lefebvre",
            // German
            "Müller", "Schmidt", "Schneider", "Fischer", "Weber", "Meyer", "Wagner", "Becker",
            "Schulz", "Hoffmann", "Schäfer", "Koch", "Bauer", "Richter", "Klein", "Wolf",
            // Italian
            "Rossi", "Russo", "Ferrari", "Esposito", "Bianchi", "Romano", "Colombo", "Ricci",
            "Marino", "Greco", "Bruno", "Gallo", "Conti", "De Luca", "Mancini", "Costa",
            // Spanish
            "García", "Rodríguez", "Martínez", "López", "González", "Hernández", "Pérez", "Sánchez",
            "Ramírez", "Torres", "Flores", "Rivera", "Gómez", "Díaz", "Reyes", "Morales"
    };

    // Email domains
    private static final String[] EMAIL_DOMAINS = {
            "gmail.com", "outlook.com", "yahoo.com", "icloud.com", "protonmail.com",
            "hotmail.com", "mail.com", "gmx.com", "zoho.com", "fastmail.com"
    };

    // Password components for strong passwords
    private static final String[] PASSWORD_WORDS = {
            "Thunder", "Crystal", "Shadow", "Phoenix", "Dragon", "Mountain", "Ocean", "Storm",
            "Forest", "Galaxy", "Diamond", "Falcon", "Tiger", "Eagle", "Wolf", "Lion",
            "Blizzard", "Sunset", "Aurora", "Nebula", "Cosmos", "Vortex", "Zenith", "Quantum"
    };
    private static final String PASSWORD_SPECIAL = "!@#$%&*?";

    private static final String[] ACCOUNT_TYPES = { "Checking", "Savings", "Investment", "Retirement" };
    private static final String[] ACCOUNT_NAMES = { "Main Account", "Emergency Fund", "Vacation", "Daily Expenses",
            "Business" };

    private static final String[] CRYPTO_SYMBOLS = { "BTC", "ETH", "BNB", "XRP", "ADA", "SOL", "DOT", "DOGE", "AVAX",
            "LINK" };
    private static final String[] STOCK_SYMBOLS = { "AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "META", "NVDA", "JPM", "V",
            "WMT" };
    private static final String[] ETF_SYMBOLS = { "SPY", "QQQ", "VTI", "IVV", "VOO", "VEA", "VWO", "BND", "GLD",
            "VNQ" };

    private static final String[] TRANSACTION_TYPES = { "deposit", "withdraw" };
    private static final String[] TRANSACTION_DESCRIPTIONS = {
            "Salary deposit", "ATM withdrawal", "Online purchase", "Transfer", "Bill payment",
            "Investment", "Refund", "Interest", "Service fee", "Subscription"
    };

    private EntityManagerFactory emf;

    private EntityManager getEntityManager() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("ewalletPU");
        }
        return emf.createEntityManager();
    }

    /**
     * Generate a realistic email address with varied formats.
     * Uses index to ensure uniqueness.
     */
    private String generateEmail(String firstName, String lastName, int index, Random random) {
        String domain = EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];
        String firstClean = normalizeString(firstName.toLowerCase());
        String lastClean = normalizeString(lastName.toLowerCase());

        int format = random.nextInt(5);

        return switch (format) {
            case 0 -> firstClean + "." + lastClean + index + "@" + domain; // prenom.nom123@
            case 1 -> lastClean + "." + firstClean + index + "@" + domain; // nom.prenom123@
            case 2 -> firstClean + "-" + lastClean + index + "@" + domain; // prenom-nom123@
            case 3 -> firstClean + "_" + lastClean + index + "@" + domain; // prenom_nom123@
            default -> firstClean.charAt(0) + lastClean + index + "@" + domain; // jdoe123@
        };
    }

    /**
     * Normalize string by removing accents for email addresses.
     */
    private String normalizeString(String input) {
        return java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll(" ", "");
    }

    /**
     * Generate a strong password: Word1Word2 + number + special char.
     */
    private String generateStrongPassword(Random random) {
        String word1 = PASSWORD_WORDS[random.nextInt(PASSWORD_WORDS.length)];
        String word2 = PASSWORD_WORDS[random.nextInt(PASSWORD_WORDS.length)];
        int num = 10 + random.nextInt(90); // 10-99
        char special = PASSWORD_SPECIAL.charAt(random.nextInt(PASSWORD_SPECIAL.length()));
        return word1 + word2 + num + special;
    }

    /**
     * GET /api/admin/stats - Returns current record counts per table.
     */
    @GET
    @Path("/stats")
    public Response getStats() {
        EntityManager em = getEntityManager();
        try {
            Map<String, Object> stats = new LinkedHashMap<>();

            Long userCount = em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
            Long accountCount = em.createQuery("SELECT COUNT(a) FROM Account a", Long.class).getSingleResult();
            Long portfolioCount = em.createQuery("SELECT COUNT(p) FROM Portfolio p", Long.class).getSingleResult();
            Long assetCount = em.createQuery("SELECT COUNT(a) FROM Asset a", Long.class).getSingleResult();
            Long transactionCount = em.createQuery("SELECT COUNT(t) FROM Transaction t", Long.class).getSingleResult();
            Long wealthTrackerCount = em.createQuery("SELECT COUNT(w) FROM WealthTracker w", Long.class)
                    .getSingleResult();

            stats.put("users", userCount);
            stats.put("accounts", accountCount);
            stats.put("portfolios", portfolioCount);
            stats.put("assets", assetCount);
            stats.put("transactions", transactionCount);
            stats.put("wealthTrackers", wealthTrackerCount);
            stats.put("totalRecords",
                    userCount + accountCount + portfolioCount + assetCount + transactionCount + wealthTrackerCount);

            return Response.ok(stats).build();
        } finally {
            em.close();
        }
    }

    /**
     * POST /api/admin/populate - Populates all tables with 1000+ records.
     */
    @POST
    @Path("/populate")
    public Response populateDatabase() {
        EntityManager em = getEntityManager();
        Random random = new Random();
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            em.getTransaction().begin();

            int usersCreated = 0;
            int accountsCreated = 0;
            int portfoliosCreated = 0;
            int assetsCreated = 0;
            int transactionsCreated = 0;
            int wealthTrackersCreated = 0;

            // Generate 1000 users
            for (int i = 0; i < 1000; i++) {
                String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];

                // Generate realistic email with varied formats
                String email = generateEmail(firstName, lastName, i, random);

                // Generate strong password
                String password = generateStrongPassword(random);

                // Use full UUID like normal registration
                String userId = UUID.randomUUID().toString();

                User user = new User(userId, email, password, firstName, lastName);

                // Create 1-3 accounts per user
                int numAccounts = 1 + random.nextInt(3);
                for (int j = 0; j < numAccounts; j++) {
                    String accountId = "demo-acc-" + UUID.randomUUID().toString().substring(0, 8);
                    String type = ACCOUNT_TYPES[random.nextInt(ACCOUNT_TYPES.length)];
                    String name = ACCOUNT_NAMES[random.nextInt(ACCOUNT_NAMES.length)] + " " + (j + 1);
                    double balance = 100 + random.nextDouble() * 9900; // 100 - 10000

                    Account account = new Account(accountId, userId, type, balance);
                    account.setName(name);

                    // Create 2-5 transactions per account
                    int numTransactions = 2 + random.nextInt(4);
                    for (int k = 0; k < numTransactions; k++) {
                        String txnId = "demo-txn-" + UUID.randomUUID().toString().substring(0, 8);
                        String txnType = TRANSACTION_TYPES[random.nextInt(TRANSACTION_TYPES.length)];
                        double amount = 10 + random.nextDouble() * 990;
                        String description = TRANSACTION_DESCRIPTIONS[random.nextInt(TRANSACTION_DESCRIPTIONS.length)];

                        Transaction txn = new Transaction(txnId, txnType, amount, description);
                        // Random timestamp between Dec 1, 2025 00:00 and Dec 15, 2025 08:00
                        LocalDateTime startDate = LocalDateTime.of(2025, 12, 1, 0, 0);
                        long hoursRange = 14 * 24 + 8; // 14 days + 8 hours = 344 hours
                        txn.setTimestamp(
                                startDate.plusHours(random.nextLong(hoursRange)).plusMinutes(random.nextInt(60)));
                        account.addTransaction(txn);
                        transactionsCreated++;
                    }

                    user.addAccount(account);
                    accountsCreated++;
                }

                // Create 1-2 portfolios per user - Add to user's list for proper FK linking
                int numPortfolios = 1 + random.nextInt(2);
                for (int j = 0; j < numPortfolios; j++) {
                    Portfolio portfolio = new Portfolio();
                    portfolio.setName("Portfolio " + (j + 1));

                    // Create 2-4 assets per portfolio
                    int numAssets = 2 + random.nextInt(3);
                    for (int k = 0; k < numAssets; k++) {
                        String[] symbols;
                        String type;
                        int typeChoice = random.nextInt(3);
                        if (typeChoice == 0) {
                            symbols = CRYPTO_SYMBOLS;
                            type = "crypto";
                        } else if (typeChoice == 1) {
                            symbols = STOCK_SYMBOLS;
                            type = "stock";
                        } else {
                            symbols = ETF_SYMBOLS;
                            type = "etf";
                        }

                        String symbol = symbols[random.nextInt(symbols.length)];
                        double quantity = 0.1 + random.nextDouble() * 10;
                        double unitValue = 10 + random.nextDouble() * 1000;

                        Asset asset = new Asset(symbol, type, symbol + " Asset", unitValue);
                        asset.setQuantity(quantity);
                        portfolio.addAsset(asset);
                        assetsCreated++;
                    }

                    user.addPortfolio(portfolio); // This ensures proper FK linking via JPA relationship
                    portfoliosCreated++;
                }

                em.persist(user); // Cascade will persist accounts, portfolios, assets, transactions
                usersCreated++;

                // Flush every 100 users to avoid memory issues
                if (i % 100 == 0) {
                    em.flush();
                    em.clear();
                }
            }

            em.getTransaction().commit();

            result.put("success", true);
            result.put("usersCreated", usersCreated);
            result.put("accountsCreated", accountsCreated);
            result.put("portfoliosCreated", portfoliosCreated);
            result.put("assetsCreated", assetsCreated);
            result.put("transactionsCreated", transactionsCreated);
            result.put("message", "Database populated successfully with demo data!");

            return Response.ok(result).build();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            result.put("success", false);
            result.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
        } finally {
            em.close();
        }
    }

    /**
     * DELETE /api/admin/clear - Clears all demo data (keeps real users).
     */
    @DELETE
    @Path("/clear")
    public Response clearDemoData() {
        EntityManager em = getEntityManager();
        Map<String, Object> result = new LinkedHashMap<>();

        try {
            em.getTransaction().begin();

            // Use native SQL for MySQL compatibility
            // Delete demo transactions first (due to FK constraints)
            int txnDeleted = em.createNativeQuery("DELETE FROM transactions WHERE transaction_id LIKE 'demo-%'")
                    .executeUpdate();

            // Delete demo assets (linked to demo portfolios)
            int assetsDeleted = em.createNativeQuery(
                    "DELETE FROM assets WHERE portfolio_id IN (SELECT portfolio_id FROM portfolios WHERE user_id LIKE 'demo-user-%')")
                    .executeUpdate();

            // Delete demo portfolios
            int portfoliosDeleted = em.createNativeQuery("DELETE FROM portfolios WHERE user_id LIKE 'demo-user-%'")
                    .executeUpdate();

            // Delete demo accounts
            int accountsDeleted = em.createNativeQuery("DELETE FROM accounts WHERE account_id LIKE 'demo-%'")
                    .executeUpdate();

            // Delete demo users
            int usersDeleted = em.createNativeQuery("DELETE FROM users WHERE user_id LIKE 'demo-user-%'")
                    .executeUpdate();

            em.getTransaction().commit();

            result.put("success", true);
            result.put("usersDeleted", usersDeleted);
            result.put("accountsDeleted", accountsDeleted);
            result.put("portfoliosDeleted", portfoliosDeleted);
            result.put("assetsDeleted", assetsDeleted);
            result.put("transactionsDeleted", txnDeleted);
            result.put("message", "Demo data cleared successfully!");

            return Response.ok(result).build();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            result.put("success", false);
            result.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(result).build();
        } finally {
            em.close();
        }
    }
}
