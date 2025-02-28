package com.ecommerce.library.service.impl;

import com.ecommerce.library.model.*;
import com.ecommerce.library.repository.CustomerRepository;
import com.ecommerce.library.repository.OrderDetailRepository;
import com.ecommerce.library.repository.OrderRepository;
import com.ecommerce.library.service.OrderService;
import com.ecommerce.library.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository detailRepository;
    private final CustomerRepository customerRepository;
    private final ShoppingCartService cartService;

    @Override
    @Transactional
    public Order save(ShoppingCart shoppingCart) {
        Order order = new Order();
        order.setOrderDate(new Date());
        order.setCustomer(shoppingCart.getCustomer());
        order.setTax(0);
        order.setTotalPrice(shoppingCart.getTotalPrice());
        order.setAccept(false);
        order.setPaymentMethod("Online");
        order.setOrderStatus(OrderStatus.PENDING);
        order.setQuantity(shoppingCart.getTotalItems());
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartItem item : shoppingCart.getCartItems()) {
            // order.setProdId(item.getProduct().getId());
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setProduct(item.getProduct());

            int currentquantity = item.getProduct().getCurrentQuantity();
            int quantity = item.getQuantity();// Quantity in cart item of pertiular product
            int result = currentquantity - quantity;

            item.getProduct().setCurrentQuantity(result);

            // here we set quantity of products in cart into order detail table
            orderDetail.setProductQuantity(item.getQuantity());

            detailRepository.save(orderDetail);
            orderDetailList.add(orderDetail);
        }
        order.setOrderDetailList(orderDetailList);
        cartService.deleteCartById(shoppingCart.getId());
        return orderRepository.save(order);
    }

    @Override
    public List<Order> findAll(String username) {
        Customer customer = customerRepository.findByUsername(username);
        List<Order> orders = customer.getOrders();
        return orders;
    }

    @Override
    public List<Order> findALlOrders() {
        List<Order> ordersList = orderRepository.findAll();
        return ordersList;
    }

    @Override
    public Order acceptOrder(Long id) {
    Order order = orderRepository.findOrderById(id);
    order.setOrderStatus(OrderStatus.CONFIRMED);
    order.setAccept(true);

    // Calculate a random delivery date within the next 8 days
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    Random random = new Random();
    int daysToAdd = random.nextInt(8) + 1; // Add 1 to ensure minimum 1 day
    calendar.add(Calendar.DAY_OF_MONTH, daysToAdd);
    //Date deliveryDate = calendar.getTime();
    //order.setDeliveryDate(deliveryDate);
    return orderRepository.save(order);
    }

    @Transactional
@Override
public Order cancelOrder(Long id) {
    Order order = orderRepository.findOrderById(id);
    if (order == null) {
        throw new IllegalArgumentException("Order with ID " + id + " not found.");
    }
    order.setOrderStatus(OrderStatus.CANCELLED);
    orderRepository.save(order);
    return order;
}


    @Override
    public Order getOrderByOrderId(Long id) {
        Order order = orderRepository.findOrderById(id);
        return order;
    }

    @Override
    public Order assignDeliveryPerson(Long orderId, DeliveryPerson deliveryPerson) {
        Order order = orderRepository.findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order with ID " + orderId + " not found.");
        }
        order.setDeliveryPerson(deliveryPerson);
        order.setOrderStatus(OrderStatus.ASSIGNED);
        return orderRepository.save(order);
    }

    // to set delivery status to delivered
    @Override
    public OrderStatus updateDeliveryStatusOnDelivery(Long orderId, boolean isDelivered){
        isDelivered = true;
        Order order = orderRepository.findOrderById(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order with ID " + orderId + " not found.");
        }
        order.setDelivered(isDelivered);
        order.setOrderStatus(OrderStatus.DELIVERED);
        order.setDeliveryDate(new Date());
        orderRepository.save(order);
        return OrderStatus.DELIVERED;
    }

    @Override
    public List<Order> getOrdersByCustomerIdAndOrderStatus(Long customerId, OrderStatus orderStatus) {
        return orderRepository.findOrdersByCustomerIdAndOrderStatus(customerId, orderStatus);
        
    }

    public List<Order> getAllPendingOrders(){
        List<Order> allOrders = orderRepository.findAll();
        List<Order> pendingOrders = new ArrayList<>();
        for(Order order: allOrders){
            if(order.getOrderStatus()==OrderStatus.PENDING){
                pendingOrders.add(order);
            }
        }
        return pendingOrders;
    }
    public List<Order> getAllAcceptedOrders(){
        List<Order> allOrders = orderRepository.findAll();
        List<Order> acceptedOrders = new ArrayList<>();
        for(Order order: allOrders){
            if(order.getOrderStatus()==OrderStatus.CONFIRMED){
                acceptedOrders.add(order);
            }
        }
        return acceptedOrders;
    }

    public List<Order> getAllDeliveredOrders(){
        List<Order> allOrders = orderRepository.findAll();
        List<Order> deliveredOrders = new ArrayList<>();
        for(Order order: allOrders){
            if(order.getOrderStatus()==OrderStatus.DELIVERED){
                deliveredOrders.add(order);
            }
        }
        return deliveredOrders;
    }
}
