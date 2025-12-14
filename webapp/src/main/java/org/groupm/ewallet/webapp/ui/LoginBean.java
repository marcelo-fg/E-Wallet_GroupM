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
 * Responsable du login/logout et du stockage minimal d'informations utilisateur
 * en session.
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
        // Validation du format email
        if (email == null || email.isBlank()) {
            addErrorMessage("Email required", "Please enter your email address.");
            return null;
        }

        if (!isValidEmail(email)) {
            addErrorMessage("Invalid email", "The email format is not valid.");
            return null;
        }

        if (password == null || password.isBlank()) {
            addErrorMessage("Password required", "Please enter your password.");
            return null;
        }

        // TODO : renforcer l'authentification via le backend (hash, rôles, etc.)
        String userId = webAppService.login(email, password);
        boolean success = (userId != null && !userId.isBlank());

        if (!success) {
            addErrorMessage("Wrong credentials",
                    "Invalid email or password. Please try again.");
            return null;
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
     * Validation simple du format email.
     * 
     * @param email L'adresse email à valider
     * @return true si le format est valide, false sinon
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        // Pattern simple et robuste pour validation email
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Ajoute un message d'erreur au contexte JSF.
     * 
     * @param summary Titre du message
     * @param detail  Description détaillée
     */
    private void addErrorMessage(String summary, String detail) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            context.addMessage(null,
                    new jakarta.faces.application.FacesMessage(
                            jakarta.faces.application.FacesMessage.SEVERITY_ERROR,
                            summary,
                            detail));
        }
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
