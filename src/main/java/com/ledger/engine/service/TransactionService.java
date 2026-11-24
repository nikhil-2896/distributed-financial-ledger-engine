package com.ledger.engine.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ledger.engine.dto.TransactionRequest;
import com.ledger.engine.dto.TransactionResponse;
import com.ledger.engine.entity.Account;
import com.ledger.engine.entity.AccountStatus;
import com.ledger.engine.entity.EntryType;
import com.ledger.engine.entity.LedgerEntry;
import com.ledger.engine.entity.LedgerTransaction;
import com.ledger.engine.entity.TransactionStatus;
import com.ledger.engine.exception.DuplicateResourceException;
import com.ledger.engine.exception.ResourceNotFoundException;
import com.ledger.engine.repository.AccountRepository;
import com.ledger.engine.repository.LedgerTransactionRepository;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final LedgerTransactionRepository transactionRepository;

    public TransactionService(
            AccountRepository accountRepository,
            LedgerTransactionRepository transactionRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransactionResponse processTransaction(
            TransactionRequest request,
            String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency-Key header is required");
        }

        if (request.amount() == null ||
                request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Transaction amount must be greater than zero");
        }

        if (request.transactionType() == null) {
            throw new IllegalArgumentException(
                    "Transaction type is required");
        }

        String requestHash = generateRequestHash(request);

        // Check whether this request was already processed
        var existingTransaction = transactionRepository
                .findByIdempotencyKey(idempotencyKey);

        if (existingTransaction.isPresent()) {

            LedgerTransaction previous = existingTransaction.get();

            if (!previous.getRequestHash().equals(requestHash)) {
                throw new DuplicateResourceException(
                        "Idempotency key was already used with different request data");
            }

            return convertToResponse(previous);
        }

        Account sourceAccount = accountRepository
                .findById(request.accountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Account not found"));

        validateActiveAccount(sourceAccount);

        LedgerTransaction transaction = new LedgerTransaction();

        transaction.setReferenceCode(UUID.randomUUID().toString());
        transaction.setIdempotencyKey(idempotencyKey);
        transaction.setRequestHash(requestHash);
        transaction.setTransactionType(request.transactionType());
        transaction.setAmount(request.amount());
        transaction.setCurrency(sourceAccount.getCurrency());

        switch (request.transactionType()) {

            case DEPOSIT -> processDeposit(
                    sourceAccount, transaction, request.amount());

            case WITHDRAWAL -> processWithdrawal(
                    sourceAccount, transaction, request.amount());

            case TRANSFER -> processTransfer(
                    sourceAccount, transaction, request);
        }

        validateBalancedEntries(transaction);

        transaction.setStatus(TransactionStatus.COMPLETED);

        LedgerTransaction savedTransaction =
                transactionRepository.save(transaction);

        return convertToResponse(savedTransaction);
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionByReference(
            String referenceCode) {

        LedgerTransaction transaction = transactionRepository
                .findByReferenceCode(referenceCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Transaction not found"));

        return convertToResponse(transaction);
    }

    private String generateRequestHash(TransactionRequest request) {

        String requestData =
                request.transactionType().name() + "|" +
                request.accountId() + "|" +
                request.destinationAccountId() + "|" +
                request.amount().stripTrailingZeros().toPlainString();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    requestData.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "Unable to generate request hash", exception);
        }
    }

    private TransactionResponse convertToResponse(
            LedgerTransaction transaction) {

        return new TransactionResponse(
                transaction.getId(),
                transaction.getReferenceCode(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getCreatedAt()
        );
    }

    private void processDeposit(
            Account account,
            LedgerTransaction transaction,
            BigDecimal amount) {

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        addSystemEntry(
                transaction, "CASH_CLEARING", EntryType.DEBIT, amount);

        addAccountEntry(
                transaction, account, EntryType.CREDIT, amount);
    }

    private void processWithdrawal(
            Account account,
            LedgerTransaction transaction,
            BigDecimal amount) {

        validateSufficientBalance(account, amount);

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        addAccountEntry(
                transaction, account, EntryType.DEBIT, amount);

        addSystemEntry(
                transaction, "CASH_CLEARING", EntryType.CREDIT, amount);
    }

    private void processTransfer(
            Account sourceAccount,
            LedgerTransaction transaction,
            TransactionRequest request) {

        if (request.destinationAccountId() == null) {
            throw new IllegalArgumentException(
                    "Destination account ID is required for transfer");
        }

        if (sourceAccount.getId().equals(
                request.destinationAccountId())) {
            throw new IllegalArgumentException(
                    "Source and destination accounts cannot be the same");
        }

        Account destinationAccount = accountRepository
                .findById(request.destinationAccountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Destination account not found"));

        validateActiveAccount(destinationAccount);

        if (sourceAccount.getCurrency() !=
                destinationAccount.getCurrency()) {
            throw new IllegalArgumentException(
                    "Both accounts must have the same currency");
        }

        BigDecimal amount = request.amount();

        validateSufficientBalance(sourceAccount, amount);

        sourceAccount.setBalance(
                sourceAccount.getBalance().subtract(amount));

        destinationAccount.setBalance(
                destinationAccount.getBalance().add(amount));

        accountRepository.save(sourceAccount);
        accountRepository.save(destinationAccount);

        addAccountEntry(
                transaction, sourceAccount, EntryType.DEBIT, amount);

        addAccountEntry(
                transaction, destinationAccount, EntryType.CREDIT, amount);
    }

    private void validateBalancedEntries(
            LedgerTransaction transaction) {

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (LedgerEntry entry : transaction.getEntries()) {

            if (entry.getEntryType() == EntryType.DEBIT) {
                totalDebit = totalDebit.add(entry.getAmount());
            } else if (entry.getEntryType() == EntryType.CREDIT) {
                totalCredit = totalCredit.add(entry.getAmount());
            }
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalStateException(
                    "Transaction is not balanced");
        }
    }

    private void validateActiveAccount(Account account) {

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Account is not active");
        }
    }

    private void validateSufficientBalance(
            Account account,
            BigDecimal amount) {

        if (account.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient account balance");
        }
    }

    private void addAccountEntry(
            LedgerTransaction transaction,
            Account account,
            EntryType entryType,
            BigDecimal amount) {

        LedgerEntry entry = new LedgerEntry();

        entry.setAccount(account);
        entry.setEntryType(entryType);
        entry.setAmount(amount);

        transaction.addEntry(entry);
    }

    private void addSystemEntry(
            LedgerTransaction transaction,
            String systemAccountCode,
            EntryType entryType,
            BigDecimal amount) {

        LedgerEntry entry = new LedgerEntry();

        entry.setSystemAccountCode(systemAccountCode);
        entry.setEntryType(entryType);
        entry.setAmount(amount);

        transaction.addEntry(entry);
    }
}