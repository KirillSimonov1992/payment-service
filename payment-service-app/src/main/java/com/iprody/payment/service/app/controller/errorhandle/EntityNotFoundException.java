package com.iprody.payment.service.app.controller.errorhandle;

import lombok.Getter;

import java.util.UUID;

@Getter
public class EntityNotFoundException extends RuntimeException {

    private final TypeOperation operation;
    private final UUID entityId;

    public EntityNotFoundException(String message, TypeOperation operation, UUID entityId) {
        super(message);
        this.operation = operation;
        this.entityId = entityId;
    }

}