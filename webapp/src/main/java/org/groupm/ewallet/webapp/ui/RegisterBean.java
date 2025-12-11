package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Inject;
import org.groupm.ewallet.webapp.service.WebAppService;

@Named
@RequestScoped
public class RegisterBean {

    private String email;
    private String password;
    private String confirmPassword;
    private String firstname;
    private String lastname;

    @Inject
    private WebAppService webAppService;

    public String register() {
        // Validate password confirmation
        if (password == null || password.isBlank()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Mot de passe requis",
                            "Veuillez entrer un mot de passe."));
            return null;
        }

        if (confirmPassword == null || !password.equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Les mots de passe ne correspondent pas",
                            "Veuillez vérifier que les deux mots de passe sont identiques."));
            return null;
        }

        // Validate password strength (minimum 6 characters)
        if (password.length() < 6) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Mot de passe trop court",
                            "Le mot de passe doit contenir au moins 6 caractères."));
            return null;
        }

        boolean success = webAppService.registerUser(firstname, lastname, email, password);

        if (success) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Inscription réussie !",
                            "Vous pouvez maintenant vous connecter."));
            return "login.xhtml?faces-redirect=true";
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Erreur",
                        "Impossible de créer le compte."));

        return null; // rester sur la page
    }

    // GETTERS / SETTERS
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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }
}