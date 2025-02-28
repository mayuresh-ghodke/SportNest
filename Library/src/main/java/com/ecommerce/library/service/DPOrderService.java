package com.ecommerce.library.service;

import com.ecommerce.library.model.Order;
import java.util.List;
public interface DPOrderService {
    
    List<Order> getAllAssignedOrders(Long deliveryPersonId);

    List<Order> getAllDeliveredOrders(Long deliveryPersonId);
}
