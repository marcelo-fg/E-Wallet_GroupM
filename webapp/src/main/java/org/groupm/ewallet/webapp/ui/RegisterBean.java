package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
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
                            "Password required",
                            "Please enter a password."));
            return null;
        }

        if (confirmPassword == null || !password.equals(confirmPassword)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Passwords do not match",
                            "Please make sure both passwords are identical."));
            return null;
        }

        // Validate password strength (minimum 6 characters)
        if (password.length() < 6) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Password too short",
                            "Password must be at least 6 characters long."));
            return null;
        }

        boolean success = webAppService.registerUser(firstname, lastname, email, password);

        if (success) {
            // Automatic login after successful registration
            String userId = webAppService.login(email, password);

            if (userId != null && !userId.isBlank()) {
                // Create user session
                FacesContext context = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) context
                        .getExternalContext()
                        .getSession(true);

                session.setAttribute("userId", userId);
                session.setAttribute("userEmail", email);

                // Redirect directly to dashboard
                return "dashboard.xhtml?faces-redirect=true";
            }

            // Fallback: if automatic login fails, go to login page
            return "login.xhtml?faces-redirect=true";
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Registration failed",
                        "Unable to create the account. Please try again."));

        return null; // stay on page
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