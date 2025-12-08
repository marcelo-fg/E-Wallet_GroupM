package org.groupm.ewallet.webservice;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.groupm.ewallet.model.User;
import org.groupm.ewallet.service.business.UserManager;

import java.util.List;

/**
 * Ressource REST responsable de la gestion des utilisateurs.
 * Fournit les opérations CRUD ainsi que les endpoints d’enregistrement et
 * d’authentification.
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class UserResource {

    @Inject
    private UserManager userManager;

    /**
     * Récupère la liste de tous les utilisateurs.
     * Endpoint : GET /api/users
     */
    @GET
    public Response getAllUsers() {
        List<User> users = userManager.getAllUsers();
        return Response.ok(users).build();
    }

    /**
     * Enregistre un nouvel utilisateur.
     * Endpoint : POST /api/users/register
     */
    @POST
    @Path("/register")
    public Response register(User user) {
        try {
            User createdUser = userManager.registerUser(user);
            return Response.status(Response.Status.CREATED).entity(createdUser).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Erreur lors de l'enregistrement : " + e.getMessage())
                    .build();
        }
    }

    /**
     * Authentifie un utilisateur existant.
     * Endpoint : POST /api/users/login
     */
    @POST
    @Path("/login")
    public Response login(User user) {
        User loggedUser = userManager.login(user.getEmail(), user.getPassword());
        if (loggedUser != null) {
            return Response.ok(loggedUser).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Email ou mot de passe incorrect")
                .build();
    }

    /**
     * Récupère un utilisateur par identifiant.
     * Endpoint : GET /api/users/{id}
     */
    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") String id) {
        User user = userManager.getUserById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Utilisateur non trouvé")
                    .build();
        }
        return Response.ok(user).build();
    }

    /**
     * Supprime un utilisateur par identifiant.
     * Endpoint : DELETE /api/users/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") String id) {
        boolean deleted = userManager.deleteUser(id);
        if (deleted) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Utilisateur non trouvé")
                .build();
    }

    /**
     * Met à jour les informations d’un utilisateur existant.
     * Endpoint : PUT /api/users/{id}
     */
    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") String id, User updatedUser) {
        boolean updated = userManager.updateUser(id, updatedUser);
        if (updated) {
            return Response.ok()
                    .entity("Utilisateur mis à jour avec succès")
                    .build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Utilisateur non trouvé")
                .build();
    }
}
