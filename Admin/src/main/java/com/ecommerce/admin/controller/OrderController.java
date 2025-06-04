package com.ecommerce.admin.controller;

import com.ecommerce.library.model.Customer;
import com.ecommerce.library.model.DeliveryPerson;
import com.ecommerce.library.model.Order;
import com.ecommerce.library.model.OrderDetail;
import com.ecommerce.library.model.OrderStatus;
import com.ecommerce.library.model.Product;
import com.ecommerce.library.service.CustomerService;
import com.ecommerce.library.service.DeliveryPersonService;
import com.ecommerce.library.service.EmailSenderService;
import com.ecommerce.library.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.awt.Color;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Autowired
    private final CustomerService customerService;

    private final DeliveryPersonService deliveryPersonService;

    @Autowired
    private final EmailSenderService emailSenderService;

    @GetMapping("/orders")
    public String getAll(Model model, Principal principal, @RequestParam(required = false) OrderStatus status) {

        if (principal == null) {
            return "redirect:/login";
        } 

        else{
            List<Order> orderList = orderService.findALlOrders();
            List<DeliveryPerson> deliveryPersons = deliveryPersonService.getAllDeliveryPersons();

            if (status != null) {
                orderList = orderList.stream()
                        .filter(order -> order.getOrderStatus().equals(status))
                        .collect(Collectors.toList());
            }

            model.addAttribute("deliveryPersons", deliveryPersons);
            model.addAttribute("orders", orderList);
            model.addAttribute("title", "Manage Orders");
            model.addAttribute("allStatuses", OrderStatus.values());
            model.addAttribute("totalOrders", orderList.size());
            model.addAttribute("allOrders", orderService.findALlOrders().size());
            return "orders";
        }
    }

    @GetMapping("/orders/status")
    public String viewOrders(@RequestParam(required = false) OrderStatus status, Model model) {
        List<Order> orders;
        List<Order> filteredOrders = new ArrayList<Order>();
        if (status != null) {
            orders = orderService.findALlOrders();

            filteredOrders = orders.stream()
            .filter(order->order.getOrderStatus().equals(status))
            .collect(Collectors.toList());
        } else {
            filteredOrders = orderService.findALlOrders();
        }
        model.addAttribute("orders", filteredOrders);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("allStatuses", OrderStatus.values());
        return "redirect:/orders";
    }

    @RequestMapping(value = "/view-order/{id}", method = RequestMethod.GET)
    public String viewOrder(@PathVariable("id") Long orderId, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Order order = orderService.getOrderByOrderId(orderId);
        List<OrderDetail> orderDetailList = order.getOrderDetailList();
        Customer customer = order.getCustomer();
        DeliveryPerson deliveryPerson = order.getDeliveryPerson();

        List<OrderDetail> orderDetails = order.getOrderDetailList();
        HashMap<Product, Integer> hMap = new HashMap<Product, Integer>();
        int totalItemCount=0;
        for(OrderDetail orderDetail: orderDetails){
            hMap.put(orderDetail.getProduct(), orderDetail.getProductQuantity());
            totalItemCount+= orderDetail.getProductQuantity();
        }
        model.addAttribute("order", order);
        model.addAttribute("customer", customer);
        model.addAttribute("address", customer.getAddress());
        model.addAttribute("title", "View Order");
        model.addAttribute("orderDetailList", orderDetailList);
        model.addAttribute("totalItemCount", totalItemCount);
        model.addAttribute("deliveryPerson", deliveryPerson);
         model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("hMapData", hMap);
        return "view-order";
    }


    @RequestMapping(value = "/assign-delivery-person/{id}", method = {RequestMethod.PUT, RequestMethod.GET})
    public String assignDeliveryPerson(@PathVariable("id") Long id, 
        @RequestParam("delivery_person") Long deliveryPersonId, 
        Principal principal,
        Model model){

        if(principal == null){
            return "redirect:/login";
        }
        DeliveryPerson deliveryPerson = deliveryPersonService.getDeliveryPerson(deliveryPersonId);
        Order order = orderService.getOrderByOrderId(id);
        order.setDeliveryPerson(deliveryPerson);
        orderService.assignDeliveryPerson(id, deliveryPerson);

        System.out.println(order.getDeliveryPerson().getFirstName());

        String toEmail = order.getCustomer().getUsername();
        String firstName = order.getCustomer().getFirstName();
        String lastName = order.getCustomer().getLastName();

        String subject = "Delivery Update: Your Order is On the Way!";
        String body = "Dear " + firstName + " " + lastName + ",\n\n" +
                "We are excited to let you know that your order (Order ID: " + id + ") has been assigned to a delivery person and will be on its way shortly.\n\n" +
                "You will receive another notification once the order is out for delivery.\n\n" +
                "Thank you for choosing our service. If you have any questions, feel free to contact our support team.\n\n" +
                "Best regards,\n" +
                "Customer Support Team";

        emailSenderService.sendSimpleEmail(toEmail, subject, body);


        List<Order> orderList = orderService.findALlOrders();
        List<DeliveryPerson> deliveryPersons = deliveryPersonService.getAllDeliveryPersons();
        model.addAttribute("deliveryPersons", deliveryPersons);
        model.addAttribute("orders", orderList);
        model.addAttribute("title", "Manage Orders");
        return "orders";
    }

    @RequestMapping(value = "/accept-order", method = {RequestMethod.PUT, RequestMethod.GET})
    public String acceptOrder(Long id, RedirectAttributes attributes, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        } 
        else {
            orderService.acceptOrder(id);

            Order order = orderService.getOrderByOrderId(id);
            String toEmail = order.getCustomer().getUsername();
            String firstName = order.getCustomer().getFirstName();
            String lastName = order.getCustomer().getLastName();

            String subject = "Your Order Has Been Accepted";
            String body = "Dear " + firstName + " " + lastName + ",\n\n" +
                    "We are pleased to inform you that your order (Order ID: " + id + ") has been successfully accepted and is now being processed.\n" +
                    "You will receive further updates once your order has been shipped.\n\n" +
                    "Thank you for shopping with us.\n" +
                    "If you have any questions or need assistance, please feel free to contact our support team.\n\n" +
                    "Best regards,\n" +
                    "Customer Support Team";

            emailSenderService.sendSimpleEmail(toEmail, subject, body);

            attributes.addFlashAttribute("success", "Order Accepted");
            return "redirect:/orders";
        }
    }

    @RequestMapping(value = "/cancel-order", method = {RequestMethod.PUT, RequestMethod.GET})
    public String cancelOrder(Long id, Principal principal, RedirectAttributes attributes) {
        if (principal == null) {
            return "redirect:/login";
        }
        else {
            orderService.cancelOrder(id);

            Order order = orderService.getOrderByOrderId(id);
            String toEmail = order.getCustomer().getUsername();
            String firstName = order.getCustomer().getFirstName();
            String lastName = order.getCustomer().getLastName();

            String subject = "Your Order Has Been Cancelled";
            String body = "Dear " + firstName + " " + lastName + ",\n\n" +
                    "We would like to inform you that your recent order (Order ID: " + id + ") has been successfully cancelled.\n" +
                    "The full amount will be refunded to your original payment method shortly.\n\n" +
                    "We apologize for any inconvenience this may have caused.\n" +
                    "If you have any questions or concerns, please do not hesitate to contact our support team.\n\n" +
                    "Thank you for your understanding.\n\n" +
                    "Best regards,\n" +
                    "Customer Support Team";
            emailSenderService.sendSimpleEmail(toEmail, subject, body);

            attributes.addFlashAttribute("success", "Order has been cancelled.");
            return "redirect:/orders";
        }
    }

    // to generate pdf report
    public void generatePDFReport(List<Order> orders) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                
                contentStream.setNonStrokingColor(Color.BLACK);
                contentStream.beginText();
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("Invoice Report");
                contentStream.newLineAtOffset(0, -20);

                // Loop through orders and write them to PDF
                for (Order order : orders) {
                    contentStream.showText("Order ID: " + order.getId());
                    // Add more fields as needed
                    contentStream.newLineAtOffset(0, -20);
                }

                contentStream.endText();
            }

            document.save("Invoice_Report.pdf");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
