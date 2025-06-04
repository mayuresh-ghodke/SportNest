package com.ecommerce.library.service.impl;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ecommerce.library.model.DeliveryPerson;
import com.ecommerce.library.repository.DeliveryPersonRepository;
import com.ecommerce.library.repository.RoleRepository;
import com.ecommerce.library.service.DeliveryPersonService;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeliveryPersonServiceImpl implements DeliveryPersonService{

    private final DeliveryPersonRepository deliveryPersonRepository;
    private final RoleRepository roleRepository;

    @Override
    public DeliveryPerson createDeliveryPerson(DeliveryPerson deliveryPerson) {
        
        deliveryPerson.setRoles(Arrays.asList(roleRepository.findByName("DELIVERY_PERSON")));
        DeliveryPerson createdDeliveryPerson =  deliveryPersonRepository.save(deliveryPerson);
        return createdDeliveryPerson;
    }

    @Override
    public DeliveryPerson getDeliveryPerson(Long deliveryPersonId) {
        return deliveryPersonRepository.findDeliveryPersonById(deliveryPersonId);
    }

    @Override
    public DeliveryPerson updateDeliveryPerson(DeliveryPerson deliveryPerson) {
        return deliveryPersonRepository.save(deliveryPerson);
    }

    @Override
    public boolean deleteDeliveryPersonById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteDeliveryPersonById'");
    }

    @Override
    public List<DeliveryPerson> getAllDeliveryPersons() {
        
        List<DeliveryPerson> deliveryPersonsList = deliveryPersonRepository.findAll();

        return deliveryPersonsList;
    }

    @Override
    public DeliveryPerson getDeliveryPersonByEmail(String dpUsername){
       
        DeliveryPerson deliveryPerson= deliveryPersonRepository.findByEmail(dpUsername);
        return deliveryPerson;
    }
    
}
