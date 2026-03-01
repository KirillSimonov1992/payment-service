package com.iprody.payment.service.app.controller.errorhandle;

import lombok.Getter;

import java.util.UUID;

@Getter
public class EntityNotFoundException extends RuntimeException {

    private final TypeMethod operation;
    private final UUID entityId;

    public EntityNotFoundException(String message, TypeMethod operation, UUID entityId) {
        super(message);
        this.operation = operation;
        this.entityId = entityId;
    }

}