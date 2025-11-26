package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.inject.Inject;
import org.groupm.ewallet.webapp.service.WebAppService;
import java.io.Serializable;
import jakarta.servlet.http.HttpSession;

@Named
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String email;
    private String password;

    @Inject
    private WebAppService webAppService;

    // Injecter plus tard ton UserManager ou WebAppService
    // private AuthenticationService authService;

    public String login() {

        // TODO : remplacer par authentification backend
        String userId = webAppService.login(email, password);
        boolean success = (userId != null);

        if (success) {
            var context = jakarta.faces.context.FacesContext.getCurrentInstance();
            var external = context.getExternalContext();
            HttpSession session = (HttpSession) external.getSession(true);

            // Empêche d'utiliser l'email comme userId
            if (userId == null || userId.isBlank()) {
                return "login.xhtml?error=true";
            }

            // Stocker uniquement le vrai userId backend
            session.setAttribute("userId", userId);

            // Stocker l'email juste pour affichage
            session.setAttribute("userEmail", email);

            return "dashboard.xhtml?faces-redirect=true";
        }

        return "login.xhtml?error=true";
    }

    public String logout() {
        var context = jakarta.faces.context.FacesContext.getCurrentInstance();
        var external = context.getExternalContext();
        HttpSession session = (HttpSession) external.getSession(false);

        if (session != null) {
            session.invalidate(); // déconnexion
        }

        return "login.xhtml?faces-redirect=true";
    }

    // -- GETTERS & SETTERS --

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}