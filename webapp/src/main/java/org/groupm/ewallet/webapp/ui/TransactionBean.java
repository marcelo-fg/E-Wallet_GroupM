package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
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
    public List<WebAppService.UnifiedTransaction> getAllTransactions() {
        return webAppService.getAllUnifiedTransactions();
    }
}
