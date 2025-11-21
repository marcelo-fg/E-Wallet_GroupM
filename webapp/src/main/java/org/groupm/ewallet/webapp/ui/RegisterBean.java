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
    private String firstname;
    private String lastname;

    @Inject
    private WebAppService webAppService;

    public String register() {

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
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
}