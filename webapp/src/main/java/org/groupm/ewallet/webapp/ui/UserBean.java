package org.groupm.ewallet.webapp.ui;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.groupm.ewallet.webapp.service.WebAppService;
import java.io.Serializable;
import jakarta.json.JsonObject;

@Named
@SessionScoped
public class UserBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String firstname;
    private String lastname;
    private String email;
    private String password;

    @Inject
    private WebAppService webAppService;

    @PostConstruct
    public void init() {
        loadUserData();
    }

    public void loadUserData() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null)
            return;

        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
        if (session != null) {
            this.userId = (String) session.getAttribute("userId");
            if (this.userId != null) {
                // Fetch fresh data from backend
                JsonObject user = webAppService.getUserDetails(this.userId);
                if (user != null) {
                    this.firstname = user.getString("firstName", "");
                    this.lastname = user.getString("lastName", "");
                    this.email = user.getString("email", "");
                    // We don't populate password for security, keeps field empty
                    this.password = "";
                }
            } else {
                // Try getting email from session as fallback
                this.email = (String) session.getAttribute("userEmail");
            }
        }
    }

    public String getInitials() {
        String f = (firstname != null && !firstname.isEmpty()) ? firstname.substring(0, 1) : "";
        String l = (lastname != null && !lastname.isEmpty()) ? lastname.substring(0, 1) : "";
        String initials = (f + l).toUpperCase();
        return initials.isEmpty() ? "U" : initials;
    }

    public String getFullName() {
        String f = firstname != null ? firstname : "";
        String l = lastname != null ? lastname : "";
        String full = (f + " " + l).trim();
        return full.isEmpty() ? "User" : full;
    }

    private String confirmPassword;

    public void updateProfile() {
        if (userId == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "User not identified"));
            return;
        }

        // Logic for password update
        String pwdToSend = null;
        if (password != null && !password.isBlank()) {
            if (password.length() < 6) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password too short",
                                "Min 6 characters required"));
                return;
            }
            if (!password.equals(confirmPassword)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password mismatch", "Passwords do not match"));
                return;
            }
            pwdToSend = password;
        }

        boolean success = webAppService.updateUser(userId, firstname, lastname, email, pwdToSend);

        if (success) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Profile updated successfully"));

            // Clear passwords after successful update
            this.password = "";
            this.confirmPassword = "";

            // Reload to reflect any server-side normalization (though we updated locally)
            loadUserData();
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Failed to update profile"));
        }
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String logout() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
            if (session != null) {
                session.invalidate();
            }
        }
        return "login.xhtml?faces-redirect=true";
    }

    // Getters and Setters

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
