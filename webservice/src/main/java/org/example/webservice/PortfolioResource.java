package org.example.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.model.Portfolio;
import org.example.service.UserManager;
import java.util.List;

@Path("/portfolios")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PortfolioResource {

    private static final UserManager userManager = new UserManager();

    @GET
    public Response getAllPortfolios() {
        List<Portfolio> portfolios = userManager.getAllPortfolios();
        return Response.ok(portfolios).build();
    }

    @POST
    public Response createPortfolio(Portfolio portfolio) {
        Portfolio created = userManager.addPortfolio(portfolio);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/{id}")
    public Response getPortfolioById(@PathParam("id") int id) {
        Portfolio portfolio = userManager.getPortfolioById(id);
        if (portfolio == null)
            return Response.status(Response.Status.NOT_FOUND).entity("Portfolio non trouvé").build();
        return Response.ok(portfolio).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deletePortfolio(@PathParam("id") int id) {
        boolean deleted = userManager.deletePortfolio(id);
        if (!deleted)
            return Response.status(Response.Status.NOT_FOUND).entity("Portfolio non trouvé").build();
        return Response.noContent().build();
    }
}