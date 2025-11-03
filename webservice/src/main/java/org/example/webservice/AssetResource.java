package org.example.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.model.Asset;
import org.example.service.MarketDataService;

import java.util.List;

@Path("/assets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssetResource {

    private static final MarketDataService marketDataService = new MarketDataService();

    /**
     * Récupère la liste de tous les actifs connus
     * Endpoint : GET /api/assets
     */
    @GET
    public Response getAllAssets() {
        List<Asset> assets = marketDataService.getAllAssets();
        return Response.ok(assets).build();
    }

    /**
     * Récupère un actif spécifique par symbole (ex: AAPL, BTC, SPY)
     * Endpoint : GET /api/assets/{symbol}
     */
    @GET
    @Path("/{symbol}")
    public Response getAssetBySymbol(@PathParam("symbol") String symbol) {
        Asset asset = marketDataService.getAssetBySymbol(symbol);
        if (asset == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Aucun actif trouvé pour le symbole : " + symbol)
                    .build();
        }
        return Response.ok(asset).build();
    }

    /**
     * Ajoute un nouvel actif dans le système (si tu veux permettre des ajouts manuels)
     * Endpoint : POST /api/assets
     */
    @POST
    public Response addAsset(Asset asset) {
        try {
            marketDataService.addAsset(asset);
            return Response.status(Response.Status.CREATED).entity(asset).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erreur lors de l'ajout de l'actif : " + e.getMessage())
                    .build();
        }
    }
}