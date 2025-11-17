package org.example.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;

@SessionScoped
@Named
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String password;

    public LoginBean() {
        reset();
    }

    public void reset() {
        this.username = null;
        this.password = null;
    }

    public String login() {
        // Authentification simple pour E-Wallet
        boolean authenticated = "admin".equals(username) && "admin".equals(password);

        if (authenticated) {
            HttpSession session = getSession(true);
            session.setAttribute("username", username);
            session.setAttribute("role", "user");
            return "Home?faces-redirect=true";
        }

        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Invalid login", null)
        );

        reset();
        return "Login";
    }

    public String logout() {
        invalidateSession();
        reset();
        return "Login?faces-redirect=true";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static HttpSession getSession(boolean create) {
        var facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null) return null;

        var externalContext = facesContext.getExternalContext();
        if (externalContext == null) return null;

        return (HttpSession) externalContext.getSession(create);
    }

    public static void invalidateSession() {
        var session = getSession(false);
        if (session != null) session.invalidate();
    }
}