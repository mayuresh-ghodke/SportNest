package com.ecommerce.library.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.library.model.Order;
import com.ecommerce.library.model.OrderStatus;
import com.ecommerce.library.repository.DeliveryPersonRepository;
import com.ecommerce.library.repository.OrderRepository;
import com.ecommerce.library.service.DPOrderService;

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
    // Fetch all orders assigned to the delivery person with status "Assigned"
    List<Order> assignedOrders = orderRepository.findByDeliveryPerson_IdAndOrderStatus(deliveryPersonId, OrderStatus.ASSIGNED);

    // Check if the list is empty and return an empty list if no orders are found
    if (assignedOrders == null || assignedOrders.isEmpty()) {
        return List.of(); 
    }
    
    return assignedOrders; 
}

    @Override
    public List<Order> getAllDeliveredOrders(Long deliveryPersonId) {
        return orderRepository.findByDeliveryPerson_IdAndOrderStatus(deliveryPersonId, OrderStatus.DELIVERED);
    }

    @Override
    public List<Order> getAllShippedOrders(Long id) {
        return orderRepository.findByDeliveryPerson_IdAndOrderStatus(id, OrderStatus.SHIPPED);
    }

}
