package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.groupm.ewallet.webapp.service.WebAppService;

import java.io.Serializable;

/**
 * Bean de présentation pour la gestion de l'authentification.
 * Responsable du login/logout et du stockage minimal d'informations utilisateur en session.
 */
@Named
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Identifiant de connexion (email utilisateur). */
    private String email;

    /** Mot de passe saisi dans le formulaire. */
    private String password;

    @Inject
    private WebAppService webAppService;

    /**
     * Tente de connecter l'utilisateur avec les identifiants saisis.
     * En cas de succès, initialise la session HTTP et redirige vers le dashboard.
     * En cas d'échec, renvoie vers la page de login avec un indicateur d'erreur.
     *
     * @return outcome de navigation JSF
     */
    public String login() {
        // TODO : renforcer l'authentification via le backend (hash, rôles, etc.)
        String userId = webAppService.login(email, password);
        boolean success = (userId != null && !userId.isBlank());

        if (!success) {
            return "login.xhtml?error=true";
        }

        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) {
            // Cas limite : pas de contexte JSF disponible
            return "login.xhtml?error=true";
        }

        HttpSession session = (HttpSession) context
                .getExternalContext()
                .getSession(true);

        // Stocker uniquement le vrai userId backend (clé fonctionnelle)
        session.setAttribute("userId", userId);

        // Stocker l'email uniquement pour affichage éventuel dans l'UI
        session.setAttribute("userEmail", email);

        return "dashboard.xhtml?faces-redirect=true";
    }

    /**
     * Déconnecte l'utilisateur courant en invalidant la session HTTP,
     * puis redirige vers la page de login.
     *
     * @return outcome de navigation JSF
     */
    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            HttpSession session = (HttpSession) context
                    .getExternalContext()
                    .getSession(false);
            if (session != null) {
                session.invalidate();
            }
        }
        return "login.xhtml?faces-redirect=true";
    }

    // ------------------------------------------------------------------
    // Getters / Setters utilisés par les pages JSF
    // ------------------------------------------------------------------

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

    /**
     * Nom affiché dans l'UI (par exemple dans le header).
     * Pour l'instant, on se contente de renvoyer l'email.
     */
    public String getUsername() {
        return (email != null) ? email : "";
    }
}
