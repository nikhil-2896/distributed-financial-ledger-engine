package com.ledger.engine.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ledger.engine.entity.LedgerEntry;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByLedgerTransactionId(Long transactionId);

    List<LedgerEntry> findByAccountId(Long accountId);

    @Query("""
            SELECT e FROM LedgerEntry e
            JOIN FETCH e.ledgerTransaction
            WHERE e.account.id = :accountId
            ORDER BY e.ledgerTransaction.createdAt DESC
            """)
    List<LedgerEntry> findAccountHistory(@Param("accountId") Long accountId);
}