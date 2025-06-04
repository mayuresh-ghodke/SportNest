package com.ecommerce.library.repository;

import com.ecommerce.library.model.Order;
import com.ecommerce.library.model.OrderStatus;

import jakarta.transaction.Transactional;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean deleteOrderById(Long orderId);

    @Transactional
    @Modifying
    @Query("UPDATE Order o SET o.paymentId = :paymentId, o.status = :status WHERE o.id = :orderId")
    int updatePaymentIdAndStatus(Long orderId, String paymentId, String status);

    Order findOrderById(Long id);
 
    List<Order> findOrdersByCustomerIdAndOrderStatus(Long customerId, OrderStatus orderStatus);

    // Find all orders assigned to a specific delivery person
    List<Order> findByDeliveryPerson_IdAndOrderStatus(Long deliveryPersonId, OrderStatus orderStatus);


}
