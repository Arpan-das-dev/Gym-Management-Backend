package com.gym.adminservice.Enums;

/**
 * Enum representing different types of user roles in the gym management system.
 */
public enum RoleType {
    ADMIN,
    MEMBER,
    TRAINER,
    TRAINER_PENDING,
    TRAINER_ADMIN, ADMIN_ADMIN, MEMBER_ADMIN;
    public boolean isTrainerRole() {
        return this == TRAINER || this == TRAINER_PENDING || this == TRAINER_ADMIN;
    }
}
