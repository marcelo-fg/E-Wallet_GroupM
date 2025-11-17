package org.example.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.model.Transaction;
import org.example.service.AccountManager;

import java.util.List;

/**
 * Ressource REST responsable de la gestion des transactions.
 * Permet la création, la lecture et la suppression des transactions
 * associées aux comptes utilisateurs.
 */
@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {

    private static final AccountManager accountManager = new AccountManager();

    /**
     * Récupère la liste complète des transactions enregistrées.
     * Endpoint : GET /api/transactions
     *
     * @return liste des transactions existantes
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
     * @param transaction transaction à créer
     * @return transaction créée avec le statut HTTP 201
     */
    @POST
    public Response createTransaction(Transaction transaction) {
        Transaction created = accountManager.addTransaction(transaction);
        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    /**
     * Récupère une transaction spécifique à partir de son identifiant.
     * Endpoint : GET /api/transactions/{id}
     *
     * @param id identifiant de la transaction
     * @return la transaction correspondante ou une erreur 404 si introuvable
     */
    @GET
    @Path("/{id}")
    public Response getTransactionById(@PathParam("id") int id) {
        Transaction transaction = accountManager.getTransactionById(id);
        if (transaction == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Transaction non trouvée")
                    .build();
        }
        return Response.ok(transaction).build();
    }

    /**
     * Supprime une transaction spécifique.
     * Endpoint : DELETE /api/transactions/{id}
     *
     * @param id identifiant de la transaction à supprimer
     * @return 204 si suppression réussie, 404 sinon
     */
    @DELETE
    @Path("/{id}")
    public Response deleteTransaction(@PathParam("id") int id) {
        boolean deleted = accountManager.deleteTransaction(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Transaction non trouvée")
                    .build();
        }
        return Response.noContent().build();
    }
}