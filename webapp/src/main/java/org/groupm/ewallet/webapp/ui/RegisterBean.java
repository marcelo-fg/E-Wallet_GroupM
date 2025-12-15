package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.application.FacesMessage;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpSession;
import org.groupm.ewallet.webapp.service.WebAppService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * RegisterBean - User registration with OWASP 2024 + NIST SP800-63B compliant
 * password validation.
 */
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

    // Password validation constants (OWASP 2024 + NIST SP800-63B)
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 64;
    private static final int RECOMMENDED_PASSWORD_LENGTH = 12;

    // Common sequences to block
    private static final String[] BLOCKED_SEQUENCES = {
            "abc", "bcd", "cde", "def", "efg", "fgh", "ghi", "hij", "ijk", "jkl",
            "klm", "lmn", "mno", "nop", "opq", "pqr", "qrs", "rst", "stu", "tuv",
            "uvw", "vwx", "wxy", "xyz",
            "012", "123", "234", "345", "456", "567", "678", "789", "890",
            "qwerty", "azerty", "qwertz", "asdf", "zxcv", "password", "passwd"
    };

    // Blacklist of common compromised passwords
    private static final Set<String> BLACKLISTED_PASSWORDS = new HashSet<>(Arrays.asList(
            "password", "password1", "password123", "123456", "12345678", "123456789",
            "1234567890", "qwerty", "qwerty123", "abc123", "111111", "123123",
            "admin", "admin123", "letmein", "welcome", "welcome1", "monkey",
            "dragon", "master", "login", "princess", "starwars", "passw0rd",
            "shadow", "sunshine", "trustno1", "iloveyou", "football", "baseball",
            "soccer", "hockey", "batman", "superman", "michael", "jennifer",
            "jessica", "ashley", "amanda", "nicole", "daniel", "andrew",
            "joshua", "matthew", "cheese", "pepper", "summer", "winter",
            "spring", "autumn", "january", "february", "monday", "friday"));

    // Regex patterns for validation
    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?/~`'\"\\\\]");
    private static final Pattern HAS_REPETITION = Pattern.compile("(.)\\1{3,}");

    public String register() {
        // Validate email format (must contain @)
        if (email == null || email.isBlank() || !email.contains("@")) {
            addErrorMessage("Invalid email address",
                    "Please enter a valid email address (must contain @).");
            return null;
        }

        // Validate password is present
        if (password == null || password.isBlank()) {
            addErrorMessage("Password required", "Please enter a password.");
            return null;
        }

        // Validate password confirmation matches
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            addErrorMessage("Passwords do not match",
                    "Please make sure both passwords are identical.");
            return null;
        }

        // ===== OWASP 2024 + NIST SP800-63B Password Validation =====

        // 1. Check minimum length (8 characters - NIST requirement)
        if (password.length() < MIN_PASSWORD_LENGTH) {
            addErrorMessage("Password too short",
                    "Password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
            return null;
        }

        // 2. Check maximum length (64 characters - for passphrases)
        if (password.length() > MAX_PASSWORD_LENGTH) {
            addErrorMessage("Password too long",
                    "Password cannot exceed " + MAX_PASSWORD_LENGTH + " characters.");
            return null;
        }

        // 3. Check for uppercase letter
        if (!HAS_UPPERCASE.matcher(password).find()) {
            addErrorMessage("Missing uppercase letter",
                    "Password must contain at least one uppercase letter (A-Z).");
            return null;
        }

        // 4. Check for lowercase letter
        if (!HAS_LOWERCASE.matcher(password).find()) {
            addErrorMessage("Missing lowercase letter",
                    "Password must contain at least one lowercase letter (a-z).");
            return null;
        }

        // 5. Check for digit
        if (!HAS_DIGIT.matcher(password).find()) {
            addErrorMessage("Missing digit",
                    "Password must contain at least one digit (0-9).");
            return null;
        }

        // 6. Check for special character
        if (!HAS_SPECIAL.matcher(password).find()) {
            addErrorMessage("Missing special character",
                    "Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?).");
            return null;
        }

        // 7. Check for character repetition (e.g., aaaa, 1111)
        if (HAS_REPETITION.matcher(password).find()) {
            addErrorMessage("Character repetition detected",
                    "Password cannot contain 4 or more repeated characters (e.g., aaaa, 1111).");
            return null;
        }

        // 8. Check for common sequences
        String lowerPassword = password.toLowerCase();
        for (String sequence : BLOCKED_SEQUENCES) {
            if (lowerPassword.contains(sequence)) {
                addErrorMessage("Common sequence detected",
                        "Password cannot contain common sequences like '" + sequence + "'.");
                return null;
            }
        }

        // 9. Check against blacklist
        if (BLACKLISTED_PASSWORDS.contains(lowerPassword)) {
            addErrorMessage("Compromised password",
                    "This password is too common and has been found in data breaches. Please choose a different password.");
            return null;
        }

        // 10. Check if password contains email or username
        String emailPrefix = email.contains("@") ? email.substring(0, email.indexOf("@")).toLowerCase() : "";
        if (!emailPrefix.isBlank() && emailPrefix.length() >= 3 && lowerPassword.contains(emailPrefix)) {
            addErrorMessage("Email in password",
                    "Password cannot contain your email address or username.");
            return null;
        }

        // Check if password contains first or last name
        if (firstname != null && !firstname.isBlank() && firstname.length() >= 3
                && lowerPassword.contains(firstname.toLowerCase())) {
            addErrorMessage("Name in password",
                    "Password cannot contain your first name.");
            return null;
        }
        if (lastname != null && !lastname.isBlank() && lastname.length() >= 3
                && lowerPassword.contains(lastname.toLowerCase())) {
            addErrorMessage("Name in password",
                    "Password cannot contain your last name.");
            return null;
        }

        // ===== End Password Validation =====

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

        addErrorMessage("Registration failed",
                "Unable to create the account. Please try again.");

        return null; // stay on page
    }

    /**
     * Helper method to add error messages to FacesContext.
     */
    private void addErrorMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
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