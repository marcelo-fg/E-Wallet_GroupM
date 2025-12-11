package org.groupm.ewallet.webapp.model;

/**
 * Frontend model for Portfolio (minimal version for name persistence).
 */
public class Portfolio {
    private int id;
    private String userId;
    private String name;

    public Portfolio() {
    }

    public Portfolio(int id, String userId, String name) {
        this.id = id;
        this.userId = userId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
