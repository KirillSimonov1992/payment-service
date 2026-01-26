package com.iprody.payment.service.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Payment {
    private long id;
    private double value;
}
