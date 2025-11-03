package org.example.webservice;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

import org.example.model.Account;
import org.example.service.AccountManager;

@Path("/accounts")
public class AccountResource {
    private AccountManager accountManager = new AccountManager();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Account> getAllAccounts() {
        return accountManager.getAllAccounts();
    }

    // Optional: Enable if AccountManager supports account creation
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(Account account) {
        Account created = accountManager.createAccount(account);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
}
