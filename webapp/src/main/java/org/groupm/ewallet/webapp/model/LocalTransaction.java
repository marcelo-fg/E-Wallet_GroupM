package org.groupm.ewallet.webapp.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple in-memory bank transaction, used to feed the account history UI.
 */
public class LocalTransaction {
    private final String accountId;
    private final LocalDateTime dateTime;
    private final double amount;
    private final String type;
    private final String category;
    private final String description;

    public LocalTransaction(String accountId,
            LocalDateTime dateTime,
            double amount,
            String type,
            String category,
            String description) {
        this.accountId = accountId;
        this.dateTime = dateTime;
        this.amount = amount;
        this.type = type;
        this.category = category;
        this.description = description;
    }

    public String getAccountId() {
        return accountId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Returns a user-friendly formatted date without time.
     */
    public String getFormattedDateTime() {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTime.format(formatter);
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }
}
