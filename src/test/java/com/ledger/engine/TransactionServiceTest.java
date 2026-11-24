
package com.ledger.engine.service;

import com.ledger.engine.dto.TransactionRequest;
import com.ledger.engine.dto.TransactionResponse;
import com.ledger.engine.entity.Account;
import com.ledger.engine.entity.AccountStatus;
import com.ledger.engine.entity.CurrencyCode;
import com.ledger.engine.entity.EntryType;
import com.ledger.engine.entity.LedgerTransaction;
import com.ledger.engine.entity.TransactionStatus;
import com.ledger.engine.entity.TransactionType;
import com.ledger.engine.exception.DuplicateResourceException;
import com.ledger.engine.repository.AccountRepository;
import com.ledger.engine.repository.LedgerTransactionRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LedgerTransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Account createAccount(
            Long id,
            CurrencyCode currency,
            String balance) {

        Account account = new Account();

        ReflectionTestUtils.setField(account, "id", id);

        account.setAccountNumber("ACC" + id);
        account.setCurrency(currency);
        account.setBalance(new BigDecimal(balance));
        account.setStatus(AccountStatus.ACTIVE);

        return account;
    }

    @Test
    void shouldProcessDepositSuccessfully() {

        Account account = createAccount(
                1L, CurrencyCode.INR, "500.00");

        when(transactionRepository.findByIdempotencyKey("deposit-001"))
                .thenReturn(Optional.empty());

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(transactionRepository.save(any(LedgerTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest request = new TransactionRequest(
                TransactionType.DEPOSIT,
                1L,
                null,
                new BigDecimal("250.00")
        );

        TransactionResponse response =
                transactionService.processTransaction(
                        request, "deposit-001");

        assertEquals(
                new BigDecimal("750.00"),
                account.getBalance());

        assertEquals(TransactionType.DEPOSIT, response.transactionType());
        assertEquals(TransactionStatus.COMPLETED, response.status());

        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(LedgerTransaction.class));
    }

    @Test
    void shouldProcessWithdrawalSuccessfully() {

        Account account = createAccount(
                1L, CurrencyCode.INR, "500.00");

        when(transactionRepository.findByIdempotencyKey("withdrawal-001"))
                .thenReturn(Optional.empty());

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(transactionRepository.save(any(LedgerTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest request = new TransactionRequest(
                TransactionType.WITHDRAWAL,
                1L,
                null,
                new BigDecimal("200.00")
        );

        TransactionResponse response =
                transactionService.processTransaction(
                        request, "withdrawal-001");

        assertEquals(
                new BigDecimal("300.00"),
                account.getBalance());

        assertEquals(TransactionType.WITHDRAWAL, response.transactionType());
        assertEquals(TransactionStatus.COMPLETED, response.status());

        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(LedgerTransaction.class));
    }

    @Test
    void shouldProcessTransferSuccessfully() {

        Account source = createAccount(
                1L, CurrencyCode.INR, "1000.00");

        Account destination = createAccount(
                2L, CurrencyCode.INR, "200.00");

        when(transactionRepository.findByIdempotencyKey("transfer-001"))
                .thenReturn(Optional.empty());

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(source));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(destination));

        when(transactionRepository.save(any(LedgerTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionRequest request = new TransactionRequest(
                TransactionType.TRANSFER,
                1L,
                2L,
                new BigDecimal("300.00")
        );

        TransactionResponse response =
                transactionService.processTransaction(
                        request, "transfer-001");

        assertEquals(
                new BigDecimal("700.00"),
                source.getBalance());

        assertEquals(
                new BigDecimal("500.00"),
                destination.getBalance());

        assertEquals(TransactionType.TRANSFER, response.transactionType());
        assertEquals(TransactionStatus.COMPLETED, response.status());

        verify(accountRepository).save(source);
        verify(accountRepository).save(destination);
    }

    @Test
    void shouldRejectWithdrawalWithInsufficientBalance() {

        Account account = createAccount(
                1L, CurrencyCode.INR, "50.00");

        when(transactionRepository.findByIdempotencyKey("withdrawal-002"))
                .thenReturn(Optional.empty());

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        TransactionRequest request = new TransactionRequest(
                TransactionType.WITHDRAWAL,
                1L,
                null,
                new BigDecimal("100.00")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction(
                        request, "withdrawal-002")
        );

        assertEquals(
                new BigDecimal("50.00"),
                account.getBalance());

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(LedgerTransaction.class));
    }

    @Test
    void shouldRejectTransferBetweenDifferentCurrencies() {

        Account source = createAccount(
                1L, CurrencyCode.INR, "1000.00");

        Account destination = createAccount(
                2L, CurrencyCode.USD, "500.00");

        when(transactionRepository.findByIdempotencyKey("transfer-002"))
                .thenReturn(Optional.empty());

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(source));

        when(accountRepository.findById(2L))
                .thenReturn(Optional.of(destination));

        TransactionRequest request = new TransactionRequest(
                TransactionType.TRANSFER,
                1L,
                2L,
                new BigDecimal("100.00")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.processTransaction(
                        request, "transfer-002")
        );

        assertEquals(
                new BigDecimal("1000.00"),
                source.getBalance());

        assertEquals(
                new BigDecimal("500.00"),
                destination.getBalance());

        verify(transactionRepository, never())
                .save(any(LedgerTransaction.class));
    }

    @Test
    void shouldReturnPreviousTransactionForSameIdempotencyKey() {

        Account account = createAccount(
                1L, CurrencyCode.INR, "500.00");

        AtomicReference<LedgerTransaction> savedTransaction =
                new AtomicReference<>();

        when(transactionRepository.findByIdempotencyKey("deposit-003"))
                .thenAnswer(invocation ->
                        Optional.ofNullable(savedTransaction.get()));

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        when(transactionRepository.save(any(LedgerTransaction.class)))
                .thenAnswer(invocation -> {
                    LedgerTransaction transaction =
                            invocation.getArgument(0);

                    savedTransaction.set(transaction);

                    return transaction;
                });

        TransactionRequest request = new TransactionRequest(
                TransactionType.DEPOSIT,
                1L,
                null,
                new BigDecimal("100.00")
        );

        TransactionResponse firstResponse =
                transactionService.processTransaction(
                        request, "deposit-003");

        TransactionResponse secondResponse =
                transactionService.processTransaction(
                        request, "deposit-003");

        assertEquals(
                firstResponse.referenceCode(),
                secondResponse.referenceCode());

        assertEquals(
                new BigDecimal("600.00"),
                account.getBalance());

        verify(transactionRepository, times(1))
                .save(any(LedgerTransaction.class));

        verify(accountRepository, times(1))
                .save(account);
    }

    @Test
    void shouldRejectSameIdempotencyKeyWithDifferentRequest() {

        LedgerTransaction existingTransaction =
                new LedgerTransaction();

        existingTransaction.setRequestHash("previous-request-hash");

        when(transactionRepository.findByIdempotencyKey("deposit-004"))
                .thenReturn(Optional.of(existingTransaction));

        TransactionRequest request = new TransactionRequest(
                TransactionType.DEPOSIT,
                1L,
                null,
                new BigDecimal("500.00")
        );

        assertThrows(
                DuplicateResourceException.class,
                () -> transactionService.processTransaction(
                        request, "deposit-004")
        );

        verify(transactionRepository, never())
                .save(any(LedgerTransaction.class));
    }

    @Test
    void shouldGetTransactionByReferenceSuccessfully() {

        LedgerTransaction transaction =
                new LedgerTransaction();

        transaction.setReferenceCode("TXN-12345");
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setCurrency(CurrencyCode.INR);

        when(transactionRepository.findByReferenceCode("TXN-12345"))
                .thenReturn(Optional.of(transaction));

        TransactionResponse response =
                transactionService.getTransactionByReference("TXN-12345");

        assertEquals("TXN-12345", response.referenceCode());
        assertEquals(TransactionType.DEPOSIT, response.transactionType());
        assertEquals(TransactionStatus.COMPLETED, response.status());
        assertEquals(new BigDecimal("1000.00"), response.amount());
    }
}
