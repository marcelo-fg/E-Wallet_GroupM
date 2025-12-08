package org.groupm.ewallet.webservice;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.groupm.ewallet.model.Portfolio;
import org.groupm.ewallet.model.Asset;
import org.groupm.ewallet.repository.PortfolioRepository;
import org.groupm.ewallet.service.business.UserManager;

import java.util.List;

/**
 * Ressource REST responsable de la gestion des portefeuilles d’investissement.
 * Permet la création, la lecture et la suppression de portefeuilles associés
 * aux utilisateurs.
 */
@Path("/portfolios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class PortfolioResource {

    @Inject
    private UserManager userManager;

    @Inject
    private PortfolioRepository portfolioRepository;

    /**
     * Récupère la liste de tous les portefeuilles existants.
     * Endpoint : GET /api/portfolios
     */
    @GET
    public Response getAllPortfolios() {
        List<Portfolio> portfolios = portfolioRepository.findAll();
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

    /**
     * Ajoute un asset à un portefeuille existant.
     * Endpoint : POST /api/portfolios/{id}/assets
     */
    @POST
    @Path("/{id}/assets")
    public Response addAssetToPortfolio(@PathParam("id") int portfolioId, Asset asset) {
        Portfolio portfolio = userManager.getPortfolioById(portfolioId);
        if (portfolio == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Portfolio not found\"}").build();
        }

        // IMPORTANT: The request contains "unitPrice" but Asset model uses "unitValue"
        // Since unitValue is zero by default, we need to ensure it's properly set
        // Note: if asset.getUnitValue() is still 0, the price wasn't mapped correctly

        System.out.println("[PortfolioResource] Adding asset: " + asset.getSymbol());
        System.out.println("  Type: " + asset.getType());
        System.out.println("  Quantity: " + asset.getQuantity());
        System.out.println("  UnitValue: " + asset.getUnitValue());

        // Add the asset to the portfolio
        portfolio.addAsset(asset);

        // Save the updated portfolio
        userManager.addOrUpdatePortfolio(portfolio);

        return Response.status(Response.Status.CREATED)
                .entity(asset)
                .build();
    }

    /**
     * Récupère tous les assets d'un portefeuille.
     * Endpoint : GET /api/portfolios/{id}/assets
     */
    @GET
    @Path("/{id}/assets")
    public Response getPortfolioAssets(@PathParam("id") int portfolioId) {
        Portfolio portfolio = userManager.getPortfolioById(portfolioId);
        if (portfolio == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Portfolio not found\"}").build();
        }

        return Response.ok(portfolio.getAssets()).build();
    }
}
