package org.groupm.ewallet.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.groupm.ewallet.model.Asset;
import org.groupm.ewallet.model.Portfolio;
import org.groupm.ewallet.model.User;

import org.groupm.ewallet.repository.AssetRepository;
import org.groupm.ewallet.repository.PortfolioRepository;
import org.groupm.ewallet.repository.UserRepository;

import org.groupm.ewallet.webservice.context.BackendContext;

import org.groupm.ewallet.service.connector.CurrencyConverter;
import org.groupm.ewallet.service.connector.DefaultMarketDataConnector;
import org.groupm.ewallet.service.connector.MarketDataConnector;
import org.groupm.ewallet.service.connector.MarketDataService;

import java.util.List;

@Path("/assets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssetResource {

    private static final AssetRepository assetRepository = BackendContext.ASSET_REPO;
    private static final PortfolioRepository portfolioRepository = BackendContext.PORTFOLIO_REPO;
    private static final UserRepository userRepository = BackendContext.USER_REPO;

    /** MarketDataService instancié proprement */
    private static final MarketDataService marketService =
            new MarketDataService(new DefaultMarketDataConnector());

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

            applyExternalPrice(asset);
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

            applyExternalPrice(asset);
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
    // HELPER : GET REAL PRICE FROM API (CORRIGÉ)
    // ============================================================

    private void applyExternalPrice(Asset asset) {
        if (asset.getType() == null || asset.getSymbol() == null) return;

        try {
            // On instancie le connecteur qui contient maintenant le mapping BTC -> bitcoin
            MarketDataConnector connector = new DefaultMarketDataConnector();

            double priceUsd = 0.0;

            if ("crypto".equalsIgnoreCase(asset.getType())) {
                priceUsd = connector.getCryptoPriceUsd(asset.getSymbol());
            } else {
                priceUsd = connector.getQuotePriceUsd(asset.getSymbol());
            }

            // CORRECTION : Si l'API renvoie un prix valide (> 0), on met à jour et on convertit.
            // Sinon (0.0), on GARDE la valeur envoyée par le frontend (ne pas écraser avec 0).
            if (priceUsd > 0) {
                double priceChf = CurrencyConverter.usdToChf(priceUsd);
                asset.setUnitValue(priceChf);
            } else {
                System.out.println("API price failed or 0 for " + asset.getSymbol() + ", keeping existing value: " + asset.getUnitValue());
            }

        } catch (Exception e) {
            System.err.println("Erreur récupération prix API : " + e.getMessage());
            // On ne fait rien, l'asset garde le prix envoyé par le JSF par défaut
        }
    }

    // ============================================================
    // HELPER : ATTACH ASSET + REFRESH PRICES
    // ============================================================

    private void attachAssetToPortfolio(int portfolioId, Asset asset) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId);
        if (portfolio == null) return;

        portfolio.getAssets().add(asset);

        // refresh real-time prices
        try {
            marketService.refreshPortfolioPricesUsd(portfolio);
        } catch (Exception e) {
            System.err.println("Erreur mise à jour prix portefeuille : " + e.getMessage());
        }

        // recalc total
        double total = portfolio.getAssets().stream()
                .mapToDouble(Asset::getTotalValue)
                .sum();

        portfolio.setTotalValue(total);
        portfolioRepository.save(portfolio);

        // update user without replacing entire portfolio list
        User user = userRepository.findById(portfolio.getUserID());
        if (user != null) userRepository.save(user);
    }
}