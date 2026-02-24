package com.iprody.payment.service.app.persistency;

import com.iprody.payment.service.app.persistence.entity.Payment;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class PaymentFilterFactory {

    public static Specification<Payment> fromFilter(PaymentFilter filter) {
        Specification<Payment> spec = Specification.unrestricted();

        if (StringUtils.hasText(filter.getCurrency())) {
            spec = spec.and(PaymentSpecifications.hasCurrency(filter.getCurrency()));
        }

        if (filter.getMinAmount() != null) {
            spec = spec.and(PaymentSpecifications.amountGreater(filter.getMinAmount()));
        }

        if (filter.getMaxAmount() != null) {
            spec = spec.and(PaymentSpecifications.amountLess(filter.getMaxAmount()));
        }

        if (filter.getCreatedAfter() != null) {
            spec = spec.and(PaymentSpecifications.createdGreater(filter.getCreatedAfter()));
        }

        if (filter.getCreatedBefore() != null) {
            spec = spec.and(PaymentSpecifications.createdLess(filter.getCreatedBefore()));
        }

        if (filter.getStatus() != null) {
            spec = spec.and(PaymentSpecifications.equalStatus(filter.getStatus()));
        }

        return spec;
    }
}