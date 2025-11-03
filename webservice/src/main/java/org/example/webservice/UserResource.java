package org.example.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.model.User;
import org.example.service.UserManager;

import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final UserManager userManager = new UserManager();

    /**
     * Récupère la liste de tous les utilisateurs
     * Endpoint : GET /api/users
     */
    @GET
    public Response getAllUsers() {
        List<User> users = userManager.getAllUsers();
        return Response.ok(users).build();
    }

    /**
     * Enregistre un nouvel utilisateur
     * Endpoint : POST /api/users/register
     */
    @POST
    @Path("/register")
    public Response register(User user) {
        try {
            User createdUser = userManager.registerUser(
                    user.getUserID(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getFirstName(),
                    user.getLastName()
            );
            return Response.status(Response.Status.CREATED).entity(createdUser).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erreur lors de l'enregistrement : " + e.getMessage())
                    .build();
        }
    }

    /**
     * Authentifie un utilisateur
     * Endpoint : POST /api/users/login
     */
    @POST
    @Path("/login")
    public Response login(User user) {
        User loggedUser = userManager.login(user.getEmail(), user.getPassword());
        if (loggedUser != null) {
            return Response.ok(loggedUser).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Email ou mot de passe incorrect")
                    .build();
        }
    }

    /**
     * Récupère un utilisateur par ID
     * Endpoint : GET /api/users/{id}
     */
    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") int id) {
        User user = userManager.getUserById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Utilisateur non trouvé")
                    .build();
        }
        return Response.ok(user).build();
    }

    /**
     * Supprime un utilisateur par ID
     * Endpoint : DELETE /api/users/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") int id) {
        boolean deleted = userManager.deleteUser(id);
        if (!deleted) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Utilisateur non trouvé")
                    .build();
        }
        return Response.noContent().build();
    }
}