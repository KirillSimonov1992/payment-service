package com.iprody.payment.service.app.persistency;

import com.iprody.payment.service.app.persistence.entity.Payment;
import com.iprody.payment.service.app.persistence.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {

    List<Payment> findByStatus(PaymentStatus status);

    @Modifying
    @Query("update Payment p set p.status = :newStatus where p.guid = :guid")
    void updateStatus(UUID guid, PaymentStatus newStatus);

    @Modifying
    @Query("update Payment p set p.note = :newNote where p.guid = :guid")
    void updateNote(UUID guid, String newNOte);

}