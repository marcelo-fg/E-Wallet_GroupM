package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import jakarta.faces.context.FacesContext;

import org.groupm.ewallet.webapp.service.WebAppService;

import java.util.List;

@Named
@RequestScoped
public class PortfolioBean {

    @Inject
    private WebAppService webAppService;

    public List<String> getAccounts() {

        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        if (session == null) {
            return List.of(); // aucun compte si non connecté
        }

        String userEmail = (String) session.getAttribute("userEmail");

        // Appel du service webapp
        return webAppService.getAccountsForUser(userEmail);
    }
}