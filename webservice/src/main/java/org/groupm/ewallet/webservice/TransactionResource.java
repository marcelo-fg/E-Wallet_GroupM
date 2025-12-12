package org.groupm.ewallet.webservice;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.groupm.ewallet.model.Transaction;
import org.groupm.ewallet.model.Account;
import org.groupm.ewallet.service.business.AccountManager;

import java.util.List;
import java.util.ArrayList;

/**
 * Ressource REST responsable de la gestion des transactions.
 * Permet la création, la lecture et la suppression des transactions
 * associées aux comptes utilisateurs.
 */
@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class TransactionResource {

    @Inject
    private AccountManager accountManager;

    /**
     * Récupère la liste complète des transactions enregistrées.
     * Endpoint : GET /api/transactions
     */
    @GET
    public Response getAllTransactions() {
        List<Transaction> transactions = accountManager.getAllTransactions();
        return Response.ok(transactions).build();
    }

    /**
     * Crée une nouvelle transaction.
     * Endpoint : POST /api/transactions
     * 
     * Délègue toute la validation et logique métier à AccountManager.
     */
    @POST
    public Response createTransaction(Transaction transaction) {
        try {
            Transaction created = accountManager.addTransaction(transaction);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
        } catch (IllegalArgumentException e) {
            // Conversion exception métier → HTTP 400
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erreur : " + e.getMessage())
                    .build();
        } catch (Exception e) {
            // Erreur interne
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Erreur interne lors du traitement de la transaction.")
                    .build();
        }
    }

    /**
     * Récupère une transaction spécifique à partir de son identifiant.
     * Endpoint : GET /api/transactions/{id}
     */
    @GET
    @Path("/{id}")
    public Response getTransactionById(@PathParam("id") String id) {
        Transaction transaction = accountManager.getTransactionById(id);
        if (transaction == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Transaction non trouvée")
                    .build();
        }
        return Response.ok(transaction).build();
    }

    /**
     * Récupère toutes les transactions d'un compte donné.
     * Endpoint : GET /api/transactions/account/{accountId}
     */
    @GET
    @Path("/account/{accountId}")
    public Response getTransactionsByAccount(@PathParam("accountId") String accountId) {
        if (accountId == null || accountId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'identifiant de compte est obligatoire.")
                    .build();
        }

        // Vérifie que le compte existe
        Account account = accountManager.getAccountById(accountId);
        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Aucun compte trouvé pour l'identifiant : " + accountId)
                    .build();
        }

        List<Transaction> transactions = accountManager.getTransactionsByAccountId(accountId);
        return Response.ok(transactions).build();
    }

    /**
     * Récupère toutes les transactions de tous les comptes d'un utilisateur.
     * Endpoint : GET /api/transactions/user/{userId}
     */
    @GET
    @Path("/user/{userId}")
    public Response getTransactionsByUser(@PathParam("userId") String userId) {
        if (userId == null || userId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("L'identifiant utilisateur est obligatoire.")
                    .build();
        }

        // Récupère tous les comptes de l'utilisateur via AccountManager
        List<Account> userAccounts = accountManager.getAllAccounts().stream()
                .filter(a -> userId.equals(a.getUserID()))
                .toList();

        if (userAccounts.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Aucun compte trouvé pour l'utilisateur : " + userId)
                    .build();
        }

        // Agrège toutes les transactions
        List<Transaction> allTransactions = new ArrayList<>();
        for (Account acc : userAccounts) {
            List<Transaction> txList = accountManager.getTransactionsByAccountId(acc.getAccountID());
            if (txList != null && !txList.isEmpty()) {
                allTransactions.addAll(txList);
            }
        }

        return Response.ok(allTransactions).build();
    }

    /**
     * Supprime une transaction spécifique.
     * Endpoint : DELETE /api/transactions/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deleteTransaction(@PathParam("id") String id) {
        boolean deleted = accountManager.deleteTransaction(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Transaction non trouvée")
                    .build();
        }
        return Response.noContent().build();
    }

    /**
     * DTO pour la requête de virement.
     */
    public static class TransferRequest {
        public String fromAccount;
        public String toAccount;
        public double amount;
        public String category;
        public String description;
    }

    /**
     * Effectue un virement entre deux comptes.
     * Endpoint : POST /api/transactions/transfer
     */
    @POST
    @Path("/transfer")
    public Response transfer(TransferRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Requête invalide.")
                    .build();
        }

        boolean success = accountManager.transfer(
                request.fromAccount,
                request.toAccount,
                request.amount,
                request.category,
                request.description);

        if (success) {
            return Response.ok("Virement effectué avec succès.").build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Échec du virement (solde insuffisant ou compte introuvable).")
                    .build();
        }
    }
}