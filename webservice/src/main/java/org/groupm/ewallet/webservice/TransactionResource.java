package org.groupm.ewallet.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.groupm.ewallet.model.Transaction;
import org.groupm.ewallet.model.Account;
import org.groupm.ewallet.repository.TransactionRepository;
import org.groupm.ewallet.repository.AccountRepository;
import org.groupm.ewallet.service.business.AccountManager;
import org.groupm.ewallet.webservice.context.BackendContext;

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
public class TransactionResource {

    // Managers / repositories partagés du backend
    private static final AccountManager accountManager = new AccountManager(
            BackendContext.ACCOUNT_REPO,
            BackendContext.TRANSACTION_REPO
    );
    private static final TransactionRepository transactionRepository =
            BackendContext.TRANSACTION_REPO;
    private static final AccountRepository accountRepository =
            BackendContext.ACCOUNT_REPO;

    /**
     * Récupère la liste complète des transactions enregistrées.
     * Endpoint : GET /api/transactions
     */
    @GET
    public Response getAllTransactions() {
        List<Transaction> transactions = transactionRepository.findAll();
        return Response.ok(transactions).build();
    }

    /**
     * Crée une nouvelle transaction.
     * Endpoint : POST /api/transactions
     */
    @POST
    public Response createTransaction(Transaction transaction) {

        // Validation préliminaire
        if (transaction == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Requête invalide : aucune transaction reçue.")
                    .build();
        }
        if (transaction.getType() == null || transaction.getType().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Le champ 'type' est obligatoire.")
                    .build();
        }
        if (transaction.getAccountID() == null || transaction.getAccountID().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Le champ 'accountID' est obligatoire.")
                    .build();
        }
        if (transaction.getAmount() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Le montant doit être strictement supérieur à zéro.")
                    .build();
        }

        // Log simple pour debug
        System.out.println("[DEBUG] Transaction reçue : " + transaction);

        try {
            Transaction created = accountManager.addTransaction(transaction);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erreur métier : " + e.getMessage())
                    .build();
        } catch (Exception e) {
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
        Transaction transaction = transactionRepository.findById(id);
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
        Account account = accountRepository.findById(accountId);
        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Aucun compte trouvé pour l'identifiant : " + accountId)
                    .build();
        }

        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);
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

        // Récupère tous les comptes de l'utilisateur
        List<Account> userAccounts = accountRepository.findByUserId(userId);
        if (userAccounts == null || userAccounts.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Aucun compte trouvé pour l'utilisateur : " + userId)
                    .build();
        }

        // Agrège toutes les transactions
        List<Transaction> allTransactions = new ArrayList<>();
        for (Account acc : userAccounts) {
            List<Transaction> txList =
                    transactionRepository.findByAccountId(acc.getAccountID());
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
}