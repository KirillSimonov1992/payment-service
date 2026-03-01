package com.iprody.payment.service.app.controller;

import com.iprody.payment.service.app.controller.errorhandle.ErrorMessageDto;
import com.iprody.payment.service.app.controller.errorhandle.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler
    public ErrorMessageDto handleIllegalArgumentException(IllegalArgumentException ex) {
        return new ErrorMessageDto(ex.getMessage(), Instant.now(), null, null);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public ErrorMessageDto handleNotFoundException(EntityNotFoundException ex) {
        return new ErrorMessageDto(ex.getMessage(), Instant.now(), ex.getOperation(), ex.getEntityId());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler
    public ErrorMessageDto handleOther(Exception ex) {
        return new ErrorMessageDto(ex.getMessage(), Instant.now(), null, null);
    }
}