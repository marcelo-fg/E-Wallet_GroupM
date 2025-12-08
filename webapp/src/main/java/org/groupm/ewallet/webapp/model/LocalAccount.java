package org.groupm.ewallet.webapp.model;

/**
 * Simple in-memory bank account representation.
 * In a real project, this would be backed by a persistent backend.
 */
public class LocalAccount {
    private final String id;
    private final String type;
    private final String name;
    private double balance;

    public LocalAccount(String id, String type, String name, double balance) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    /**
     * For now we reuse the id as display number.
     */
    public String getNumber() {
        return id;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
