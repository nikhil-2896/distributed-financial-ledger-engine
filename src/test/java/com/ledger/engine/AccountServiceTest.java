
package com.ledger.engine.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ledger.engine.dto.AccountRequest;
import com.ledger.engine.dto.AccountResponse;
import com.ledger.engine.entity.Account;
import com.ledger.engine.entity.CurrencyCode;
import com.ledger.engine.entity.Customer;
import com.ledger.engine.exception.ResourceNotFoundException;
import com.ledger.engine.repository.AccountRepository;
import com.ledger.engine.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldCreateAccountSuccessfully() {

        Customer customer = new Customer();

        customer.setFullName("Rahul Sharma");
        customer.setEmail("rahul@gmail.com");

        ReflectionTestUtils.setField(customer, "id", 1L);

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(accountRepository.existsByAccountNumber(anyString()))
                .thenReturn(false);

        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountRequest request = new AccountRequest(
                1L,
                CurrencyCode.INR
        );

        AccountResponse response =
                accountService.createAccount(request);

        assertNotNull(response.accountNumber());
        assertEquals(CurrencyCode.INR, response.currency());
        assertEquals(BigDecimal.ZERO, response.balance());
        assertEquals(1L, response.customerId());

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {

        when(customerRepository.findById(1L))
                .thenReturn(Optional.empty());

        AccountRequest request = new AccountRequest(
                1L,
                CurrencyCode.INR
        );

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.createAccount(request)
        );

        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldGetAccountByIdSuccessfully() {

        Customer customer = new Customer();

        Account account = new Account();
        account.setAccountNumber("ACC123456789");
        account.setCurrency(CurrencyCode.INR);
        account.setBalance(new BigDecimal("5000.00"));
        account.setCustomer(customer);

        when(accountRepository.findById(1L))
                .thenReturn(Optional.of(account));

        AccountResponse response =
                accountService.getAccountById(1L);

        assertEquals("ACC123456789", response.accountNumber());
        assertEquals(CurrencyCode.INR, response.currency());
        assertEquals(new BigDecimal("5000.00"), response.balance());
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFound() {

        when(accountRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.getAccountById(1L)
        );
    }

    @Test
    void shouldGetAccountsByCustomerIdSuccessfully() {

        Customer customer = new Customer();

        Account account1 = new Account();
        account1.setAccountNumber("ACC111111111");
        account1.setCurrency(CurrencyCode.INR);
        account1.setBalance(new BigDecimal("1000.00"));
        account1.setCustomer(customer);

        Account account2 = new Account();
        account2.setAccountNumber("ACC222222222");
        account2.setCurrency(CurrencyCode.USD);
        account2.setBalance(new BigDecimal("2000.00"));
        account2.setCustomer(customer);

        when(customerRepository.existsById(1L))
                .thenReturn(true);

        when(accountRepository.findByCustomerId(1L))
                .thenReturn(List.of(account1, account2));

        List<AccountResponse> responses =
                accountService.getAccountsByCustomerId(1L);

        assertEquals(2, responses.size());
        assertEquals("ACC111111111", responses.get(0).accountNumber());
        assertEquals("ACC222222222", responses.get(1).accountNumber());
    }
}
