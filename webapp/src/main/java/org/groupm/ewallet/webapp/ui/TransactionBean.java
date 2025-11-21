package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;
import jakarta.faces.application.FacesMessage;

import org.groupm.ewallet.webapp.service.WebAppService;

@Named
@RequestScoped
public class TransactionBean {

    private String fromAccount;
    private String toAccount;
    private double amount;

    @Inject
    private WebAppService webAppService;

    public String makeTransfer() {

        boolean success = webAppService.makeTransfer(fromAccount, toAccount, amount);

        if (success) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Transfert effectué avec succès !",
                            "Montant : " + amount + " CHF"));
            return null;
        }

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Échec du transfert",
                        "Veuillez vérifier les informations saisies."));

        return null;
    }

    // GETTERS & SETTERS
    public String getFromAccount() { return fromAccount; }
    public void setFromAccount(String fromAccount) { this.fromAccount = fromAccount; }

    public String getToAccount() { return toAccount; }
    public void setToAccount(String toAccount) { this.toAccount = toAccount; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
}