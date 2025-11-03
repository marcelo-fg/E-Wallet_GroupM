package org.example.webservice;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.example.model.User;
import org.example.service.UserManager;

import java.util.List;

@Path("/users")
public class UserResource {

    private static final UserManager userManager = new UserManager();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> getAllUsers() {
        return userManager.getAllUsers();
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User register(User user) {
        return userManager.registerUser(
                user.getUserID(),
                user.getEmail(),
                user.getPassword(),
                user.getFirstName(),
                user.getLastName()
        );
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User login(User user) {
        return userManager.login(user.getEmail(), user.getPassword());
    }
}