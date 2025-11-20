package org.groupm.ewallet.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.groupm.ewallet.model.WealthTracker;
import org.groupm.ewallet.service.business.UserManager;
import org.groupm.ewallet.webservice.context.BackendContext; // <-- Import BackendContext
import java.util.logging.Logger;

/**
 * Ressource REST responsable du suivi de la richesse des utilisateurs.
 * Permet de vérifier l’état du service et de récupérer les données financières d’un utilisateur.
 */
@Path("/wealth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WealthTrackerResource {

    // UserManager instancié avec les repos mémoire partagés du BackendContext
    private static final UserManager userManager = new UserManager(
            BackendContext.USER_REPO,
            BackendContext.PORTFOLIO_REPO
    );
    private static final Logger LOGGER = Logger.getLogger(WealthTrackerResource.class.getName());

    /**
     * Endpoint de vérification du bon fonctionnement du service.
     * Endpoint : GET /api/wealth
     */
    @GET
    public Response ping() {
        return Response.ok("{\"message\": \"WealthTracker endpoint is active.\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Récupère les informations de richesse pour un utilisateur donné.
     * Endpoint : GET /api/wealth/{userId}
     */
    @GET
    @Path("/{userId}")
    public Response getWealthByUser(@PathParam("userId") String userId) {
        LOGGER.info("Calling GET /wealth/" + userId);
        WealthTracker wealth = userManager.calculateWealthForUser(userId);
        if (wealth == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User not found or no associated portfolio")
                    .build();
        }
        return Response.ok(wealth).build();
    }
}
