package org.example.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.model.Asset;
import org.example.service.MarketDataService;

import java.util.List;

/**
 * Ressource REST responsable de la gestion des actifs financiers.
 * Fournit des endpoints pour consulter et ajouter des actifs (actions, cryptos, ETF, etc.).
 */
@Path("/assets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AssetResource {

    private static final MarketDataService marketDataService = new MarketDataService();

    /**
     * Récupère la liste de tous les actifs disponibles.
     * Endpoint : GET /api/assets
     *
     * @return liste des actifs connus
     */
    @GET
    public Response getAllAssets() {
        List<Asset> assets = marketDataService.getAllAssets();
        return Response.ok(assets).build();
    }

    /**
     * Récupère un actif spécifique à partir de son symbole.
     * Endpoint : GET /api/assets/{symbol}
     *
     * @param symbol symbole de l’actif (ex. : AAPL, BTC, SPY)
     * @return l’actif correspondant ou une erreur 404 si introuvable
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
     * Ajoute un nouvel actif dans le système.
     * Endpoint : POST /api/assets
     *
     * @param asset nouvel actif à enregistrer
     * @return réponse HTTP 201 avec l’actif créé ou une erreur 400 en cas d’échec
     */
    @POST
    public Response addAsset(Asset asset) {
        try {
            marketDataService.addAsset(asset);
            return Response.status(Response.Status.CREATED)
                    .entity(asset)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erreur lors de l’ajout de l’actif : " + e.getMessage())
                    .build();
        }
    }
}