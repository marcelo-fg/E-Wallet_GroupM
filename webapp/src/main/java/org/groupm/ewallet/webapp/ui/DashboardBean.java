package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class DashboardBean implements Serializable {

    private String username = "Utilisateur";

    public String getWelcomeMessage() {
        return "Bienvenue, " + username + " !";
    }

    // futur : charger infos utilisateur depuis backend
}