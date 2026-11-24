package com.ledger.engine.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ledger.engine.entity.LedgerTransaction;

public interface LedgerTransactionRepository
        extends JpaRepository<LedgerTransaction, Long> {

    Optional<LedgerTransaction> findByReferenceCode(
            String referenceCode);

    boolean existsByReferenceCode(String referenceCode);

    Optional<LedgerTransaction> findByIdempotencyKey(
            String idempotencyKey);
}