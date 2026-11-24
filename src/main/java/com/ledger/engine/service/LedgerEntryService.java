package com.ledger.engine.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ledger.engine.dto.LedgerEntryResponse;
import com.ledger.engine.entity.LedgerEntry;
import com.ledger.engine.exception.ResourceNotFoundException;
import com.ledger.engine.repository.AccountRepository;
import com.ledger.engine.repository.LedgerEntryRepository;

@Service
public class LedgerEntryService {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerEntryService(
            AccountRepository accountRepository,
            LedgerEntryRepository ledgerEntryRepository) {
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> getAccountHistory(Long accountId) {

        if (!accountRepository.existsById(accountId)) {
            throw new ResourceNotFoundException("Account not found");
        }

        List<LedgerEntry> entries =
                ledgerEntryRepository.findAccountHistory(accountId);

        return entries.stream()
                .map(entry -> new LedgerEntryResponse(
                        entry.getId(),
                        entry.getLedgerTransaction().getReferenceCode(),
                        entry.getLedgerTransaction().getTransactionType(),
                        entry.getLedgerTransaction().getStatus(),
                        entry.getEntryType(),
                        entry.getAmount(),
                        entry.getLedgerTransaction().getCurrency(),
                        entry.getLedgerTransaction().getCreatedAt()
                ))
                .toList();
    }
}