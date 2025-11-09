package org.example.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.model.Account;
import org.example.model.User;
import org.example.service.AccountManager;
import org.example.service.UserManager;
import java.util.List;

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource {

    private static final AccountManager accountManager = new AccountManager();
    private static final UserManager userManager = new UserManager();

    @GET
    public Response getAllAccounts() {
        List<Account> accounts = accountManager.getAllAccounts();
        return Response.ok(accounts).build();
    }

    @POST
    public Response createAccount(Account account) {
        Account created = accountManager.addAccount(account);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @POST
    @Path("/user/{userId}")
    public Response createAccountForUser(@PathParam("userId") int userId, Account account) {
        User user = userManager.getUserById(userId);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Utilisateur non trouvé").build();
        }

        account.setUser(user);
        user.addAccount(account);
        accountManager.addAccount(account); // ✅ ajoute aussi à la liste globale

        return Response.status(Response.Status.CREATED).entity(account).build();
    }

    @GET
    @Path("/{id}")
    public Response getAccountById(@PathParam("id") int id) {
        Account account = accountManager.getAccountById(id);
        if (account == null)
            return Response.status(Response.Status.NOT_FOUND).entity("Compte non trouvé").build();
        return Response.ok(account).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteAccount(@PathParam("id") int id) {
        boolean deleted = accountManager.deleteAccount(id);
        if (!deleted)
            return Response.status(Response.Status.NOT_FOUND).entity("Compte non trouvé").build();
        return Response.noContent().build();
    }
}