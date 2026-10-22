package com.cinema.ebook.model.enums;

/**
 * User account status enumeration for Sprint 2
 * Controls user login eligibility and account state
 */
public enum UserStatus {
    ACTIVE("Account is active and can login"),
    INACTIVE("Account is inactive, requires email verification"),
    SUSPENDED("Account is suspended, cannot login");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean canLogin() {
        return this == ACTIVE;
    }
}
