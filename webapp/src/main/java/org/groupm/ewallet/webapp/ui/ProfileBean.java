package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class ProfileBean implements Serializable {

    private String username = "Utilisateur";
    private String email = "user@example.com";

    public String getUsername() { return username; }
    public String getEmail() { return email; }
}