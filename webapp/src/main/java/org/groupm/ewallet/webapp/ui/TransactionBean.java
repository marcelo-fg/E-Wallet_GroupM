package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.groupm.ewallet.webapp.service.MockDataService;
import org.groupm.ewallet.webapp.service.WebAppService;

import java.io.Serializable;
import java.util.List;

/**
 * Global Transactions page:
 * Displays all transactions (accounts + portfolios) in a single unified table.
 * No filters - shows everything by default.
 */
@Named
@SessionScoped
public class TransactionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private WebAppService webAppService;

    /**
     * Returns all unified transactions (accounts + portfolios).
     * Data is always fresh from the service to stay in sync with recent operations.
     */
    public List<MockDataService.UnifiedTransaction> getAllTransactions() {
        jakarta.faces.context.FacesContext context = jakarta.faces.context.FacesContext.getCurrentInstance();
        if (context == null)
            return List.of();
        Object user = context.getExternalContext().getSessionMap().get("userId");
        String userId = user != null ? user.toString() : null;

        if (userId == null) {
            return List.of();
        }

        return webAppService.getAllUnifiedTransactions(userId);
    }
}
