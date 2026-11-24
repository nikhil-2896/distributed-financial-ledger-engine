package com.ledger.engine.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ledger.engine.dto.CustomerRequest;
import com.ledger.engine.dto.CustomerResponse;
import com.ledger.engine.entity.Customer;
import com.ledger.engine.exception.DuplicateResourceException;
import com.ledger.engine.repository.CustomerRepository;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional
    public CustomerResponse registerCustomer(CustomerRequest request) {

        if (customerRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered");
        }

        Customer customer = new Customer();
        customer.setFullName(request.fullName());
        customer.setEmail(request.email());

        Customer savedCustomer = customerRepository.save(customer);

        return new CustomerResponse(
                savedCustomer.getId(),
                savedCustomer.getFullName(),
                savedCustomer.getEmail()
        );
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customer -> new CustomerResponse(
                        customer.getId(),
                        customer.getFullName(),
                        customer.getEmail()
                ))
                .toList();
    }
}