package org.example.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.model.Portfolio;
import org.example.service.UserManager;

import java.util.List;

/**
 * Ressource REST responsable de la gestion des portefeuilles d’investissement.
 * Permet la création, la lecture et la suppression de portefeuilles associés aux utilisateurs.
 */
@Path("/portfolios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PortfolioResource {

    private static final UserManager userManager = new UserManager();

    /**
     * Récupère la liste de tous les portefeuilles existants.
     * Endpoint : GET /api/portfolios
     *
     * @return liste complète des portefeuilles
     */
    @GET
    public Response getAllPortfolios() {
        List<Portfolio> portfolios = userManager.getAllPortfolios();
        return Response.ok(portfolios).build();
    }

    /**
     * Crée un nouveau portefeuille.
     * Endpoint : POST /api/portfolios
     *
     * @param portfolio portefeuille à créer
     * @return réponse HTTP 201 avec le portefeuille créé
     */
    @POST
    public Response createPortfolio(Portfolio portfolio) {
        Portfolio created = userManager.addPortfolio(portfolio);
        return Response.status(Response.Status.CREATED)
                .entity(created)
                .build();
    }

    /**
     * Récupère un portefeuille spécifique à partir de son identifiant.
     * Endpoint : GET /api/portfolios/{id}
     *
     * @param id identifiant du portefeuille
     * @return le portefeuille correspondant ou une erreur 404 si introuvable
     */
    @GET
    @Path("/{id}")
    public Response getPortfolioById(@PathParam("id") int id) {
        Portfolio portfolio = userManager.getPortfolioById(id);
        if (portfolio == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Portfolio non trouvé")
                    .build();
        }
        return Response.ok(portfolio).build();
    }

    /**
     * Supprime un portefeuille existant par son identifiant.
     * Endpoint : DELETE /api/portfolios/{id}
     *
     * @param id identifiant du portefeuille à supprimer
     * @return 204 si la suppression est réussie, 404 sinon
     */
    @DELETE
    @Path("/{id}")
    public Response deletePortfolio(@PathParam("id") int id) {
        boolean deleted = userManager.deletePortfolio(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Portfolio non trouvé")
                    .build();
        }
        return Response.noContent().build();
    }
}