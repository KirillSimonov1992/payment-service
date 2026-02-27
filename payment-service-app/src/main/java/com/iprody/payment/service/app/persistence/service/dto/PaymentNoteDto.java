package com.iprody.payment.service.app.persistence.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

@Data
@AllArgsConstructor
public class PaymentNoteDto {
    @NotNull
    private String note;
}