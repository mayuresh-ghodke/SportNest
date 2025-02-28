package com.ecommerce.library.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.library.model.Order;
import com.ecommerce.library.repository.DeliveryPersonRepository;
import com.ecommerce.library.repository.OrderRepository;
import com.ecommerce.library.service.DPOrderService;
import org.springframework.web.bind.annotation.RequestMapping;

@Service
public class DPOrderServiceImpl implements DPOrderService {

    private final DeliveryPersonRepository deliveryPersonRepository;
    private final OrderRepository orderRepository;

    // Constructor-based dependency injection
    @Autowired
    public DPOrderServiceImpl(DeliveryPersonRepository deliveryPersonRepository, OrderRepository orderRepository) {
        this.deliveryPersonRepository = deliveryPersonRepository;
        this.orderRepository = orderRepository;
    }

    @Override
public List<Order> getAllAssignedOrders(Long deliveryPersonId) {
    // Fetch all orders assigned to the delivery person with status "Accepted"
    List<Order> assignedOrders = orderRepository.findByDeliveryPerson_IdAndOrderStatus(deliveryPersonId, "Accepted");

    // Check if the list is empty and return an empty list if no orders are found
    if (assignedOrders == null || assignedOrders.isEmpty()) {
        return List.of(); 
    }
    
    return assignedOrders; 
}

    @Override
    public List<Order> getAllDeliveredOrders(Long deliveryPersonId) {
        return orderRepository.findByDeliveryPerson_IdAndOrderStatus(deliveryPersonId, "Delivered");
    }

}
