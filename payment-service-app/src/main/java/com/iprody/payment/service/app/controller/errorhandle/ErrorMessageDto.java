package com.iprody.payment.service.app.controller.errorhandle;

import java.time.Instant;
import java.util.UUID;

public record ErrorMessageDto(
        String message,
        Instant timestamp,
        TypeOperation operation,
        UUID entityGuid
) {
    public ErrorMessageDto(String message, Instant timestamp) {
        this(message, timestamp, null, null);
    }
}