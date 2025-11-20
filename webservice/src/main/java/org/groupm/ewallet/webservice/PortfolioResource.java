package org.groupm.ewallet.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.groupm.ewallet.model.Portfolio;
import org.groupm.ewallet.service.business.UserManager;
import org.groupm.ewallet.webservice.context.BackendContext;

import java.util.List;

/**
 * Ressource REST responsable de la gestion des portefeuilles d’investissement.
 * Permet la création, la lecture et la suppression de portefeuilles associés aux utilisateurs.
 */
@Path("/portfolios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PortfolioResource {

    // UserManager partagé, alimenté par les repos singleton du BackendContext
    private static final UserManager userManager = new UserManager(
            BackendContext.USER_REPO,
            BackendContext.PORTFOLIO_REPO
    );

    /**
     * Récupère la liste de tous les portefeuilles existants.
     * Endpoint : GET /api/portfolios
     */
    @GET
    public Response getAllPortfolios() {
        List<Portfolio> portfolios = BackendContext.PORTFOLIO_REPO.findAll();
        return Response.ok(portfolios).build();
    }

    /**
     * Crée un nouveau portefeuille.
     * Endpoint : POST /api/portfolios
     */
    @POST
    public Response createPortfolio(Portfolio portfolio) {
        Portfolio created = userManager.addOrUpdatePortfolio(portfolio);
        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    /**
     * Récupère un portefeuille spécifique à partir de son identifiant.
     * Endpoint : GET /api/portfolios/{id}
     */
    @GET
    @Path("/{id}")
    public Response getPortfolioById(@PathParam("id") int id) {
        Portfolio portfolio = userManager.getPortfolioById(id);
        if (portfolio == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Portfolio non trouvé").build();
        }
        return Response.ok(portfolio).build();
    }

    /**
     * Supprime un portefeuille existant par son identifiant.
     * Endpoint : DELETE /api/portfolios/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deletePortfolio(@PathParam("id") int id) {
        boolean deleted = userManager.deletePortfolio(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Portfolio non trouvé").build();
        }
        return Response.noContent().build();
    }
}
