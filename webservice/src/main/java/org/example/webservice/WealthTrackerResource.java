package org.example.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.model.WealthTracker;
import org.example.service.UserManager;
import java.util.logging.Logger;

@Path("/wealth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WealthTrackerResource {

    private static final UserManager userManager = new UserManager();
    private static final Logger LOGGER = Logger.getLogger(WealthTrackerResource.class.getName());

    @GET
    public Response ping() {
        return Response.ok("{\"message\": \"WealthTracker endpoint actif ✅\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("/{userId}")
    public Response getWealthByUser(@PathParam("userId") int userId) {
        LOGGER.info("Appel de l'endpoint GET /wealth/" + userId);
        WealthTracker wealth = userManager.calculateWealthForUser(userId);
        if (wealth == null)
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Utilisateur non trouvé ou aucun portefeuille associé").build();
        return Response.ok(wealth).build();
    }
}