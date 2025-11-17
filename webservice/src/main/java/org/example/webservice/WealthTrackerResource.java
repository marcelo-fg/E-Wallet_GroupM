package org.example.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.model.WealthTracker;
import org.example.service.UserManager;
import java.util.logging.Logger;

/**
 * Ressource REST responsable du suivi de la richesse des utilisateurs.
 * Permet de vérifier l’état du service et de récupérer les données financières d’un utilisateur.
 */
@Path("/wealth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WealthTrackerResource {

    private static final UserManager userManager = new UserManager();
    private static final Logger LOGGER = Logger.getLogger(WealthTrackerResource.class.getName());

    /**
     * Endpoint de vérification du bon fonctionnement du service.
     * Endpoint : GET /api/wealth
     *
     * @return une réponse JSON indiquant que le service est actif
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
     *
     * @param userId identifiant de l’utilisateur
     * @return les données de richesse ou une erreur 404 si l’utilisateur n’existe pas
     */
    @GET
    @Path("/{userId}")
    public Response getWealthByUser(@PathParam("userId") int userId) {
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