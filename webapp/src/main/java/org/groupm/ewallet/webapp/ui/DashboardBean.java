package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.groupm.ewallet.webapp.service.WebAppService;

import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class DashboardBean implements Serializable {

    private String userId;
    private String userEmail;
    private String username = "Utilisateur";

    private List<String> accounts;   // comptes bancaires
    private double totalWealth;      // richesse totale

    @Inject
    private WebAppService webAppService;

    public void loadDashboard() {
        var context = jakarta.faces.context.FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        if (session != null) {
            this.userId = (String) session.getAttribute("userId");
            this.userEmail = (String) session.getAttribute("userEmail");
        }

        if (userEmail != null) {
            this.username = userEmail;
        }

        if (userId != null) {
            this.accounts =webAppService.getAccountsForUser(userId);
        }
    }

    public String getWelcomeMessage() {
        return "Bienvenue, " + username + " !";
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public double getTotalWealth() {
        return totalWealth;
    }
}