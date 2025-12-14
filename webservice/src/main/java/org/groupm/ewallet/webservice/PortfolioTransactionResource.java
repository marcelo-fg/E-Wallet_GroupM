package org.groupm.ewallet.webservice;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.groupm.ewallet.model.PortfolioTransaction;
import org.groupm.ewallet.service.business.PortfolioTransactionManager;

import java.util.List;

/**
 * REST resource for portfolio transactions (BUY/SELL operations).
 * These are separate from bank account transactions.
 */
@Path("/portfolio-transactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class PortfolioTransactionResource {

    @Inject
    private PortfolioTransactionManager transactionManager;

    /**
     * Gets all portfolio transactions.
     * Endpoint: GET /api/portfolio-transactions
     */
    @GET
    public Response getAllTransactions() {
        List<PortfolioTransaction> transactions = transactionManager.getAllTransactions();
        return Response.ok(transactions).build();
    }

    /**
     * Gets transactions for a specific portfolio.
     * Endpoint: GET /api/portfolio-transactions/portfolio/{portfolioId}
     */
    @GET
    @Path("/portfolio/{portfolioId}")
    public Response getTransactionsByPortfolio(@PathParam("portfolioId") int portfolioId) {
        List<PortfolioTransaction> transactions = transactionManager.getTransactionsByPortfolioId(portfolioId);
        return Response.ok(transactions).build();
    }

    /**
     * Gets a transaction by its ID.
     * Endpoint: GET /api/portfolio-transactions/{id}
     */
    @GET
    @Path("/{id}")
    public Response getTransactionById(@PathParam("id") long id) {
        PortfolioTransaction transaction = transactionManager.getTransactionById(id);
        if (transaction == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Transaction not found\"}")
                    .build();
        }
        return Response.ok(transaction).build();
    }

    /**
     * Records a new portfolio transaction (BUY or SELL).
     * Endpoint: POST /api/portfolio-transactions
     */
    @POST
    public Response createTransaction(PortfolioTransaction transaction) {
        try {
            PortfolioTransaction created = transactionManager.recordTransaction(transaction);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Internal server error: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    /**
     * Deletes a transaction.
     * Endpoint: DELETE /api/portfolio-transactions/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deleteTransaction(@PathParam("id") long id) {
        boolean deleted = transactionManager.deleteTransaction(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Transaction not found\"}")
                    .build();
        }
        return Response.noContent().build();
    }
}
