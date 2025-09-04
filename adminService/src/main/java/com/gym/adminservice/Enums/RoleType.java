package com.gym.adminservice.Enums;

public enum RoleType {
    ADMIN,
    MEMBER,
    TRAINER,
    TRAINER_PENDING,
    TRAINER_ADMIN, ADMIN_ADMIN;
    public boolean isTrainerRole() {
        return this == TRAINER || this == TRAINER_PENDING || this == TRAINER_ADMIN;
    }
}
