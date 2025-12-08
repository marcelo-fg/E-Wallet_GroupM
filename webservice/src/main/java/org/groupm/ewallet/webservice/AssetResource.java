package org.groupm.ewallet.webservice;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.groupm.ewallet.model.Asset;
import org.groupm.ewallet.model.Portfolio;
import org.groupm.ewallet.model.User;

import org.groupm.ewallet.repository.AssetRepository;
import org.groupm.ewallet.repository.PortfolioRepository;
import org.groupm.ewallet.repository.UserRepository;

import java.util.List;

@Path("/assets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class AssetResource {

    @Inject
    private AssetRepository assetRepository;

    @Inject
    private PortfolioRepository portfolioRepository;

    @Inject
    private UserRepository userRepository;

    // ============================================================
    // GET ALL ASSETS
    // ============================================================

    @GET
    public Response getAllAssets() {
        List<Asset> assets = assetRepository.findAll();
        return Response.ok(assets).build();
    }

    // ============================================================
    // GET ASSET BY SYMBOL
    // ============================================================

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

    // ============================================================
    // ADD ASSET (global)
    // ============================================================

    @POST
    public Response addAsset(Asset asset) {
        try {

            // Price should already be set by webapp layer
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

    // ============================================================
    // GET ASSETS FOR PORTFOLIO
    // ============================================================

    @GET
    @Path("/portfolio/{portfolioId}")
    public Response getAssetsByPortfolio(@PathParam("portfolioId") int portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId);
        if (portfolio == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Portefeuille non trouvé : " + portfolioId)
                    .build();
        }
        return Response.ok(portfolio.getAssets()).build();
    }

    // ============================================================
    // ADD ASSET TO PORTFOLIO
    // ============================================================

    @POST
    @Path("/portfolio/{portfolioId}")
    public Response addAssetToPortfolio(@PathParam("portfolioId") int portfolioId, Asset asset) {
        try {
            asset.setPortfolioID(portfolioId);

            // Price should already be set by webapp layer
            assetRepository.save(asset);
            attachAssetToPortfolio(portfolioId, asset);

            return Response.status(Response.Status.CREATED).entity(asset).build();

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erreur lors de l’ajout de l’actif au portefeuille : " + e.getMessage())
                    .build();
        }
    }

    // ============================================================
    // NOTE: Price management has been moved to webapp layer
    // Webservice should NOT call external APIs
    // ============================================================

    // ============================================================
    // HELPER : ATTACH ASSET + REFRESH PRICES
    // ============================================================

    private void attachAssetToPortfolio(int portfolioId, Asset asset) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId);
        if (portfolio == null)
            return;

        portfolio.getAssets().add(asset);

        // NOTE: Price refreshing should be done by webapp before calling this endpoint

        // recalc total
        double total = portfolio.getAssets().stream()
                .mapToDouble(Asset::getTotalValue)
                .sum();

        portfolio.setTotalValue(total);
        portfolioRepository.save(portfolio);

        // update user without replacing entire portfolio list
        User user = userRepository.findById(portfolio.getUserID());
        if (user != null)
            userRepository.save(user);
    }
}