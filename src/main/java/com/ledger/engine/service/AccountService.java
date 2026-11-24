package com.ledger.engine.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ledger.engine.dto.AccountRequest;
import com.ledger.engine.dto.AccountResponse;
import com.ledger.engine.entity.Account;
import com.ledger.engine.entity.AccountStatus;
import com.ledger.engine.entity.Customer;
import com.ledger.engine.exception.ResourceNotFoundException;
import com.ledger.engine.repository.AccountRepository;
import com.ledger.engine.repository.CustomerRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public AccountResponse createAccount(AccountRequest request) {

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        String accountNumber;

        do {
            accountNumber = UUID.randomUUID().toString()
                    .replace("-", "").substring(0, 12).toUpperCase();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setCurrency(request.currency());
        account.setBalance(BigDecimal.ZERO);
        account.setCustomer(customer);

        return convertToResponse(accountRepository.save(account));
    }

    @Cacheable(cacheNames = "accounts", key = "#p0")
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        return convertToResponse(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByCustomerId(Long customerId) {

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found");
        }

        return accountRepository.findByCustomerId(customerId)
                .stream().map(this::convertToResponse).toList();
    }

    @CacheEvict(value = "accounts", key = "#accountId")
    @Transactional
    public AccountResponse freezeAccount(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active accounts can be frozen");
        }

        account.setStatus(AccountStatus.FROZEN);
        return convertToResponse(accountRepository.save(account));
    }

    @CacheEvict(value = "accounts", key = "#accountId")
    @Transactional
    public AccountResponse unfreezeAccount(Long accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new IllegalArgumentException("Only frozen accounts can be activated");
        }

        account.setStatus(AccountStatus.ACTIVE);
        return convertToResponse(accountRepository.save(account));
    }

    private AccountResponse convertToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus(),
                account.getCustomer().getId()
        );
    }
}