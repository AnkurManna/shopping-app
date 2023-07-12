package com.ankur.paymentservice.repository;

import com.ankur.paymentservice.entity.TransactionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionDetailsRepository extends JpaRepository<TransactionDetails,Long> {

    Optional<TransactionDetails> findByOrderId(long OrderId);
}
