package org.example.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.model.User;
import org.example.service.UserManager;

import java.util.List;

/**
 * Ressource REST responsable de la gestion des utilisateurs.
 * Fournit les opérations CRUD ainsi que les endpoints d’enregistrement et d’authentification.
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final UserManager userManager = new UserManager();

    /**
     * Récupère la liste de tous les utilisateurs.
     * Endpoint : GET /api/users
     *
     * @return la liste des utilisateurs
     */
    @GET
    public Response getAllUsers() {
        List<User> users = userManager.getAllUsers();
        return Response.ok(users).build();
    }

    /**
     * Enregistre un nouvel utilisateur.
     * Endpoint : POST /api/users/register
     *
     * @param user nouvel utilisateur à enregistrer
     * @return l’utilisateur créé avec le statut HTTP 201
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
     * Authentifie un utilisateur existant.
     * Endpoint : POST /api/users/login
     *
     * @param user utilisateur avec email et mot de passe
     * @return l’utilisateur authentifié ou une erreur 401 si les informations sont incorrectes
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
     *
     * @param id identifiant de l’utilisateur
     * @return l’utilisateur correspondant ou une erreur 404 s’il n’existe pas
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
     * Supprime un utilisateur par identifiant.
     * Endpoint : DELETE /api/users/{id}
     *
     * @param id identifiant de l’utilisateur à supprimer
     * @return réponse 204 si suppression réussie, 404 sinon
     */
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") int id) {
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
     *
     * @param id identifiant de l’utilisateur à mettre à jour
     * @param updatedUser nouvel utilisateur avec les données à mettre à jour
     * @return réponse 200 si mise à jour réussie, 404 sinon
     */
    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") int id, User updatedUser) {
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