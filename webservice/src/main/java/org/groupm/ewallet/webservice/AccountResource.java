package org.groupm.ewallet.webservice;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.groupm.ewallet.model.Account;
import org.groupm.ewallet.model.User;
import org.groupm.ewallet.service.business.AccountManager;
import org.groupm.ewallet.service.business.UserManager;

import java.util.List;

/**
 * Ressource REST pour la gestion des comptes utilisateurs.
 * Fournit les endpoints CRUD pour créer, lire, mettre à jour et supprimer des
 * comptes.
 */
@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class AccountResource {

    @Inject
    private AccountManager accountManager;

    @Inject
    private UserManager userManager;

    /**
     * Récupère la liste de tous les comptes.
     * Endpoint : GET /api/accounts
     */
    @GET
    public Response getAllAccounts() {
        List<Account> accounts = accountManager.getAllAccounts();
        return Response.ok(accounts).build();
    }

    /**
     * Crée un nouveau compte sans utilisateur associé.
     * Endpoint : POST /api/accounts
     */
    @POST
    public Response createAccount(Account account) {
        // Validation préliminaire
        if (account == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Requête invalide : aucun compte reçu.")
                    .build();
        }
        if (account.getAccountID() == null || account.getAccountID().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Le champ 'accountID' est obligatoire.")
                    .build();
        }
        if (account.getType() == null || account.getType().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Le champ 'type' est obligatoire.")
                    .build();
        }
        System.out.println("[DEBUG] Création de compte : " + account.getAccountID() + ", type=" + account.getType());
        Account created = accountManager.addAccount(account);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    /**
     * Crée un nouveau compte associé à un utilisateur existant.
     * Endpoint : POST /api/accounts/user/{userId}
     */
    @POST
    @Path("/user/{userId}")
    public Response createAccountForUser(@PathParam("userId") String userId, Account account) {
        // Validation initiale
        if (account == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Requête invalide : aucun compte reçu.")
                    .build();
        }
        if (account.getAccountID() == null || account.getAccountID().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Le champ 'accountID' est obligatoire.")
                    .build();
        }
        if (account.getType() == null || account.getType().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Le champ 'type' est obligatoire.")
                    .build();
        }

        User user = userManager.getUserById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Utilisateur non trouvé")
                    .build();
        }

        System.out.println("[DEBUG] Création de compte pour utilisateur : userId=" + userId + ", accountId="
                + account.getAccountID());
        // Liaison du compte à l'utilisateur
        account.setUserID(userId);
        account.setUser(user);
        user.addAccount(account);
        accountManager.addAccount(account);

        return Response.status(Response.Status.CREATED).entity(account).build();
    }

    /**
     * Récupère un compte spécifique selon son ID.
     * Endpoint : GET /api/accounts/{id}
     */
    @GET
    @Path("/{id}")
    public Response getAccountById(@PathParam("id") String id) {
        Account account = accountManager.getAccountById(id);
        if (account == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Compte non trouvé").build();
        }
        return Response.ok(account).build();
    }

    /**
     * Supprime un compte selon son ID.
     * Endpoint : DELETE /api/accounts/{id}
     */
    @DELETE
    @Path("/{id}")
    public Response deleteAccount(@PathParam("id") String id) {
        boolean deleted = accountManager.deleteAccount(id);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Compte non trouvé").build();
        }
    }

    /**
     * Met à jour les informations d’un compte existant.
     * Endpoint : PUT /api/accounts/{id}
     */
    @PUT
    @Path("/{id}")
    public Response updateAccount(@PathParam("id") String id, Account newAccount) {
        boolean updated = accountManager.updateAccount(id, newAccount);
        if (updated) {
            return Response.ok()
                    .entity("Compte mis à jour avec succès").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Compte non trouvé").build();
        }
    }
}
