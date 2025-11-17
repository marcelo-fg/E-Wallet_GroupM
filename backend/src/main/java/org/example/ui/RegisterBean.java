package org.example.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.logging.Logger;

@SessionScoped
@Named
public class RegisterBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(RegisterBean.class.getName());

    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;

    public RegisterBean() {
        reset();
    }

    public void reset() {
        firstName = null;
        lastName = null;
        email = null;
        username = null;
        password = null;
    }

    public String register() {
        LoginBean.invalidateSession();

        if (firstName == null || lastName == null || email == null || username == null || password == null) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "All fields are required.", null)
            );
            return "Register";
        }

        // Placeholder: simulate successful registration
        FacesContext.getCurrentInstance().addMessage(
                null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Registration successful. Please log in.", null)
        );

        return "Login?faces-redirect=true";
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
