package com.cinema.ebook.model.enums;

/**
 * UserStatus Enum
 * Defines user lifecycle state
 */
public enum UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED;

    public boolean canLogin() {
        return this == ACTIVE;
    }
}
