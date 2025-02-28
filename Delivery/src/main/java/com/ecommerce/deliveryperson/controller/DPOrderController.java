package com.ecommerce.deliveryperson.controller;

import com.ecommerce.library.model.Order;
import com.ecommerce.library.service.AddressService;
import com.ecommerce.library.service.DPOrderService;
import com.ecommerce.library.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.jaxb.SpringDataJaxb.OrderDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;

import com.ecommerce.library.model.Address;
import com.ecommerce.library.model.DeliveryPerson;
import com.ecommerce.library.service.DeliveryPersonService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DPOrderController {

    @Autowired
    private DeliveryPersonService deliveryPersonService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private DPOrderService dpOrderService;

    @Autowired
    private AddressService addressService;

    @RequestMapping("/assigned-orders")
    public String getAllAssignedOrders(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/dp-login";
        }

        String dpUsername = authentication.getName();
        DeliveryPerson deliveryPerson = deliveryPersonService.getDeliveryPersonByEmail(dpUsername);

        List<Order> assignedOrders = dpOrderService.getAllAssignedOrders(deliveryPerson.getId());

        // Creating a Map to store order ID as key and customer details as value
        Map<Long, String> orderCustomerDetailsMap = new HashMap<>();

        // Populating the map with order ID and customer details
        for (Order order : assignedOrders) {
            String customerName = order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName();
            String shippingAddress = order.getCustomer().getAddress();

            // Combine customer details, you could also use a custom object if needed
            String customerDetails = "Name: " + customerName + ", Address: " + shippingAddress;

            // Adding order ID and customer details to the map
            orderCustomerDetailsMap.put(order.getId(), customerDetails);
        }

        // Adding map to the model
        model.addAttribute("orderCustomerDetailsMap", orderCustomerDetailsMap);
        model.addAttribute("assignedOrders", assignedOrders);
        model.addAttribute("title", "Assigned Orders");
        model.addAttribute("size", assignedOrders.size());

        return "assigned-orders"; // Returning Thymeleaf view
    }

    // Endpoint for fetching order details as JSON (AJAX call)
    @GetMapping("/assigned-orders/{id}")
    public ResponseEntity<Order> getOrderDetails(@PathVariable Long id) {
        Order order = orderService.getOrderByOrderId(id);
        return ResponseEntity.ok(order); // Returning JSON response
    }

    // to view an order
    @GetMapping("/view-order/{id}")
    public String viewOrderDetail(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderByOrderId(id);
        Address address = addressService.getAddressByCustomerId(order.getCustomer().getId());
        
        StringBuffer addressStr = new StringBuffer();
        addressStr.append(address.getStreet()+", ");
        addressStr.append(address.getCity()+", ");
        addressStr.append(address.getState()+", ");
        addressStr.append(address.getCountry()+", ");
        addressStr.append(address.getPincode());
        
        model.addAttribute("title", "Order Detail");
        model.addAttribute("order", order);
        model.addAttribute("address", addressStr);
        return "order-detail";
    }


    // delivered orders
    @RequestMapping("/delivered-orders")
    public String getAllDeliveredOrders(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/dp-login";
        }

        String dpUsername = authentication.getName();
        DeliveryPerson deliveryPerson = deliveryPersonService.getDeliveryPersonByEmail(dpUsername);

        List<Order> deliveredOrders = dpOrderService.getAllDeliveredOrders(deliveryPerson.getId());

        // Creating a Map to store order ID as key and shipping address as value
        Map<Long, String> orderShippedAddressMap = new HashMap<>();

        // Populating the map with order ID and address details
        for (Order order : deliveredOrders) {
            String shippedAddress = order.getCustomer().getAddress();

            // Adding order ID and shipping address to the map
            orderShippedAddressMap.put(order.getId(), shippedAddress);
        }

        // Adding map to the model
        model.addAttribute("orderCustomerDetailsMap", orderShippedAddressMap);
        model.addAttribute("deliveredOrders", deliveredOrders);
        model.addAttribute("title", "Delivered Orders");
        model.addAttribute("size", deliveredOrders.size());

        return "delivered-orders"; // Returning Thymeleaf view
    }
}
