package com.example.springecommerceapi.service;

import com.example.springecommerceapi.domain.Customer;
import com.example.springecommerceapi.dto.CustomerRequest;
import com.example.springecommerceapi.dto.CustomerResponse;
import com.example.springecommerceapi.exception.NotFoundException;
import com.example.springecommerceapi.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .build();
    }

    public CustomerResponse create(CustomerRequest request) {
        Customer customer = Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();

        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    public CustomerResponse getById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + id));
        return mapToResponse(customer);
    }

    public List<CustomerResponse> getAll() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Customer not found with id: " + id));

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());

        Customer updated = customerRepository.save(customer);
        return mapToResponse(updated);
    }

    public void delete(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new NotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
    }
}
