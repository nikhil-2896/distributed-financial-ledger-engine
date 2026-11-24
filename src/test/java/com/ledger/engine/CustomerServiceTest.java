
package com.ledger.engine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ledger.engine.dto.CustomerRequest;
import com.ledger.engine.dto.CustomerResponse;
import com.ledger.engine.entity.Customer;
import com.ledger.engine.exception.DuplicateResourceException;
import com.ledger.engine.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldRegisterCustomerSuccessfully() {

        CustomerRequest request = new CustomerRequest(
                "Rahul Sharma",
                "rahul@gmail.com"
        );

        when(customerRepository.existsByEmail(request.email()))
                .thenReturn(false);

        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response =
                customerService.registerCustomer(request);

        assertEquals("Rahul Sharma", response.fullName());
        assertEquals("rahul@gmail.com", response.email());

        verify(customerRepository).existsByEmail("rahul@gmail.com");
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {

        CustomerRequest request = new CustomerRequest(
                "Rahul Sharma",
                "rahul@gmail.com"
        );

        when(customerRepository.existsByEmail(request.email()))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> customerService.registerCustomer(request)
        );

        verify(customerRepository, never()).save(any(Customer.class));
    }
}
