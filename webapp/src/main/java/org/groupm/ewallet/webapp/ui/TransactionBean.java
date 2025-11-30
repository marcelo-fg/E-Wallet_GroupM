package org.groupm.ewallet.webapp.ui;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.groupm.ewallet.webapp.service.WebAppService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global Transactions page:
 * - Displays all transactions (accounts / portfolios) in a single table.
 * - Allows filtering by source type and by subset of accounts/portfolios.
 *
 * For now, only account transactions are populated (source = "ACCOUNT").
 * Portfolio support is prepared for future evolution.
 */
@Named
@SessionScoped
public class TransactionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private WebAppService webAppService;

    /**
     * Source type filter:
     * - "ALL"       : accounts + portfolios
     * - "ACCOUNT"   : only bank account transactions
     * - "PORTFOLIO" : only portfolio transactions (future)
     */
    private String filterSourceType = "ALL";

    /**
     * Selected account ids for filtering.
     * When the list is empty, all accounts are included.
     */
    private List<String> selectedAccountIds = new ArrayList<>();

    /**
     * Selected portfolio ids for filtering.
     * When the list is empty, all portfolios are included.
     */
    private List<String> selectedPortfolioIds = new ArrayList<>();

    /**
     * Reset all filters to their default state:
     * - type = ALL
     * - no account / portfolio restriction
     */
    public void resetFilters() {
        filterSourceType = "ALL";
        selectedAccountIds.clear();
        selectedPortfolioIds.clear();
    }

    /**
     * Returns the list of unified transactions filtered according to
     * the current filter criteria (source type + selected ids).
     * The underlying data is always read from WebAppService to keep
     * the view in sync with recent operations.
     */
    public List<WebAppService.UnifiedTransaction> getFilteredTransactions() {
        List<WebAppService.UnifiedTransaction> allTransactions =
                webAppService.getAllUnifiedTransactions();

        return allTransactions.stream()
                .filter(tx -> {
                    // Filter by source type
                    if ("ACCOUNT".equalsIgnoreCase(filterSourceType)
                            && !"ACCOUNT".equals(tx.getSource())) {
                        return false;
                    }
                    if ("PORTFOLIO".equalsIgnoreCase(filterSourceType)
                            && !"PORTFOLIO".equals(tx.getSource())) {
                        return false;
                    }

                    // Filter by selected accounts (if any)
                    if ("ACCOUNT".equalsIgnoreCase(tx.getSource())
                            && !selectedAccountIds.isEmpty()) {
                        return selectedAccountIds.contains(tx.getSourceId());
                    }

                    // Filter by selected portfolios (if any)
                    if ("PORTFOLIO".equalsIgnoreCase(tx.getSource())
                            && !selectedPortfolioIds.isEmpty()) {
                        return selectedPortfolioIds.contains(tx.getSourceId());
                    }

                    // No restriction for this transaction
                    return true;
                })
                .collect(Collectors.toList());
    }

    /* ========= Data for filters ========= */

    public List<WebAppService.LocalAccount> getAllAccounts() {
        return webAppService.getAccounts();
    }

    public List<String> getAllPortfolioIds() {
        // TODO: adapt when you have a real user context
        return webAppService.getPortfoliosForUser("1").stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    /* ========= Getters / setters ========= */

    public String getFilterSourceType() {
        return filterSourceType;
    }

    public void setFilterSourceType(String filterSourceType) {
        this.filterSourceType = filterSourceType;
    }

    public List<String> getSelectedAccountIds() {
        return selectedAccountIds;
    }

    public void setSelectedAccountIds(List<String> selectedAccountIds) {
        this.selectedAccountIds = selectedAccountIds;
    }

    public List<String> getSelectedPortfolioIds() {
        return selectedPortfolioIds;
    }

    public void setSelectedPortfolioIds(List<String> selectedPortfolioIds) {
        this.selectedPortfolioIds = selectedPortfolioIds;
    }
}
