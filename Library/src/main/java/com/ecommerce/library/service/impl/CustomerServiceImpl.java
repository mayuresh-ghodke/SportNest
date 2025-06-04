package com.ecommerce.library.service.impl;

import com.ecommerce.library.dto.CustomerDto;
import com.ecommerce.library.exception.ResourceNotFoundException;
import com.ecommerce.library.model.Customer;
import com.ecommerce.library.repository.CustomerRepository;
import com.ecommerce.library.repository.RoleRepository;
import com.ecommerce.library.service.CustomerService;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;

    @Override
    public Customer save(CustomerDto customerDto) {
        Customer customer = new Customer();
        customer.setFirstName(customerDto.getFirstName());
        customer.setLastName(customerDto.getLastName());
        customer.setPassword(customerDto.getPassword());
        customer.setUsername(customerDto.getUsername());
        customer.setPhoneNumber(customerDto.getPhoneNumber());
        customer.setRoles(Arrays.asList(roleRepository.findByName("CUSTOMER")));
        return customerRepository.save(customer);
    }

    @Override
    public Customer findByUsername(String username) {
        Customer customer = customerRepository.findByUsername(username);
        return customer;
    }

    @Override
    public CustomerDto getCustomer(String username) {
        CustomerDto customerDto = new CustomerDto();
        Customer customer = customerRepository.findByUsername(username);
        customerDto.setFirstName(customer.getFirstName());
        customerDto.setLastName(customer.getLastName());
        customerDto.setUsername(customer.getUsername());
        customerDto.setPassword(customer.getPassword());
        //customerDto.setAddress(customer.getAddress());
        customerDto.setPhoneNumber(customer.getPhoneNumber());
        return customerDto;
    }

    @Override
    public Customer changePass(CustomerDto customerDto) {
        Customer customer = customerRepository.findByUsername(customerDto.getUsername());
        customer.setPassword(customerDto.getPassword());
        return customerRepository.save(customer);
    }

    @Override
    public Customer update(CustomerDto dto) {
        Customer customer = customerRepository.findByUsername(dto.getUsername());
        customer.setPhoneNumber(dto.getPhoneNumber());
        return customerRepository.save(customer);
    }

    @Override
    public List<Customer> getAllCustomers() {
        List<Customer> customerList = customerRepository.findAll();
        return customerList;
    }

    @Override
    public Long getCustomerId(String username) {
        
       Customer customer = customerRepository.findByUsername(username);
       Long customerId = customer.getId();
       return customerId;
    }

    @Override
    public Customer getCustomerById(Long id) {
        Customer customer = customerRepository.findCustomerById(id);
        if(customer==null){
            throw new ResourceNotFoundException("Customer with ID-"+id+" not found.");
        }
        return customer;
    }

    @Override
    public boolean deleteById(Long Id) {
        getCustomerById(Id);
        customerRepository.deleteById(Id);
        return true;
    }
}
