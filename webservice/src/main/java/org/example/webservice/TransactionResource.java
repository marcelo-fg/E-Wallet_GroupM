package org.example.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.model.Transaction;
import org.example.service.AccountManager;
import java.util.List;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionResource {

    private static final AccountManager accountManager = new AccountManager();

    @GET
    public Response getAllTransactions() {
        List<Transaction> transactions = accountManager.getAllTransactions();
        return Response.ok(transactions).build();
    }

    @POST
    public Response createTransaction(Transaction transaction) {
        Transaction created = accountManager.addTransaction(transaction);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/{id}")
    public Response getTransactionById(@PathParam("id") int id) {
        Transaction transaction = accountManager.getTransactionById(id);
        if (transaction == null)
            return Response.status(Response.Status.NOT_FOUND).entity("Transaction non trouvée").build();
        return Response.ok(transaction).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteTransaction(@PathParam("id") int id) {
        boolean deleted = accountManager.deleteTransaction(id);
        if (!deleted)
            return Response.status(Response.Status.NOT_FOUND).entity("Transaction non trouvée").build();
        return Response.noContent().build();
    }
}