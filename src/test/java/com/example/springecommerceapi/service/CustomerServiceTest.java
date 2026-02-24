package com.example.springecommerceapi.service;

import com.example.springecommerceapi.domain.Customer;
import com.example.springecommerceapi.dto.CustomerRequest;
import com.example.springecommerceapi.dto.CustomerResponse;
import com.example.springecommerceapi.exception.NotFoundException;
import com.example.springecommerceapi.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer customer;
    private CustomerRequest customerRequest;

    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("1234567890")
                .build();

        customerRequest = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("1234567890")
                .build();
    }

    @Test
    void create_ShouldReturnCustomerResponse() {
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerResponse response = customerService.create(customerRequest);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("John");
        assertThat(response.getEmail()).isEqualTo("john@example.com");
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void getById_WhenCustomerExists_ShouldReturnCustomerResponse() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.getById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getFirstName()).isEqualTo("John");
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    void getById_WhenCustomerDoesNotExist_ShouldThrowNotFoundException() {
        when(customerRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getById(2L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer not found with id: 2");
    }

    @Test
    void getAll_ShouldReturnListOfCustomerResponse() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<CustomerResponse> responses = customerService.getAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getFirstName()).isEqualTo("John");
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void update_WhenCustomerExists_ShouldReturnUpdatedCustomerResponse() {
        CustomerRequest updateRequest = CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("0987654321")
                .build();

        Customer updatedCustomer = Customer.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .phone("0987654321")
                .build();

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(updatedCustomer);

        CustomerResponse response = customerService.update(1L, updateRequest);

        assertThat(response.getFirstName()).isEqualTo("Jane");
        assertThat(response.getEmail()).isEqualTo("jane@example.com");
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void delete_WhenCustomerExists_ShouldDeleteCustomer() {
        when(customerRepository.existsById(1L)).thenReturn(true);

        customerService.delete(1L);

        verify(customerRepository, times(1)).existsById(1L);
        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    void delete_WhenCustomerDoesNotExist_ShouldThrowNotFoundException() {
        when(customerRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> customerService.delete(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer not found with id: 1");

        verify(customerRepository, never()).deleteById(anyLong());
    }
}
