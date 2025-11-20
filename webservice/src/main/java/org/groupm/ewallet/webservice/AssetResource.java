package org.groupm.ewallet.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.groupm.ewallet.model.Asset;
import org.groupm.ewallet.model.Portfolio;
import org.groupm.ewallet.repository.AssetRepository;
import org.groupm.ewallet.repository.PortfolioRepository;
import org.groupm.ewallet.webservice.context.BackendContext;
import org.groupm.ewallet.repository.UserRepository;
import org.groupm.ewallet.model.User;

import java.util.List;

/**
 * Ressource REST responsable de la gestion des actifs financiers.
 * Fournit des endpoints pour consulter et ajouter des actifs (actions, cryptos, ETF, etc.).
 */
@Path("/assets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssetResource {

    // Repository partagé, singleton du BackendContext
    private static final AssetRepository assetRepository = BackendContext.ASSET_REPO;
    private static final PortfolioRepository portfolioRepository = BackendContext.PORTFOLIO_REPO;
    private static final UserRepository userRepository = BackendContext.USER_REPO;

    /**
     * Récupère la liste de tous les actifs disponibles.
     * Endpoint : GET /api/assets
     */
    @GET
    public Response getAllAssets() {
        List<Asset> assets = assetRepository.findAll();
        return Response.ok(assets).build();
    }

    /**
     * Récupère un actif spécifique à partir de son symbole.
     * Endpoint : GET /api/assets/{symbol}
     */
    @GET
    @Path("/{symbol}")
    public Response getAssetBySymbol(@PathParam("symbol") String symbol) {
        Asset asset = assetRepository.findBySymbol(symbol);
        if (asset == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Aucun actif trouvé pour le symbole : " + symbol)
                    .build();
        }
        return Response.ok(asset).build();
    }

    /**
     * Ajoute un nouvel actif dans le système.
     * Endpoint : POST /api/assets
     */
    @POST
    public Response addAsset(Asset asset) {
        try {
            assetRepository.save(asset);
            if (asset.getPortfolioID() != 0) {
                attachAssetToPortfolio(asset.getPortfolioID(), asset);
            }
            return Response.status(Response.Status.CREATED).entity(asset).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erreur lors de l’ajout de l’actif : " + e.getMessage())
                    .build();
        }
    }
    @GET
    @Path("/portfolio/{portfolioId}")
    public Response getAssetsByPortfolio(@PathParam("portfolioId") int portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId);
        if (portfolio == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Portefeuille non trouvé pour l'id : " + portfolioId)
                    .build();
        }
        return Response.ok(portfolio.getAssets()).build();
    }

    @POST
    @Path("/portfolio/{portfolioId}")
    public Response addAssetToPortfolio(@PathParam("portfolioId") int portfolioId, Asset asset) {
        try {
            asset.setPortfolioID(portfolioId);
            assetRepository.save(asset);
            attachAssetToPortfolio(portfolioId, asset);
            return Response.status(Response.Status.CREATED).entity(asset).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erreur lors de l’ajout de l’actif au portefeuille : " + e.getMessage())
                    .build();
        }
    }

    private void attachAssetToPortfolio(int portfolioId, Asset asset) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId);
        if (portfolio == null) return;

        portfolio.getAssets().add(asset);

        double total = 0.0;
        for (Asset a : portfolio.getAssets()) {
            total += a.getTotalValue();
        }
        portfolio.setTotalValue(total);

        portfolioRepository.save(portfolio);
        User user = userRepository.findById(portfolio.getUserID());
        if (user != null) {
            user.setPortfolio(portfolio);
            userRepository.save(user);
        }
    }
}