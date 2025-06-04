package com.ecommerce.customer.controller;

import com.ecommerce.library.dto.CustomerDto;
import com.ecommerce.library.dto.ProductDto;
import com.ecommerce.library.exception.ResourceNotFoundException;
import com.ecommerce.library.model.*;
import com.ecommerce.library.repository.OrderDetailRepository;
import com.ecommerce.library.repository.OrderRepository;
import com.ecommerce.library.service.*;
import com.ecommerce.library.utils.PdfGenerator;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final CustomerService customerService;
    private final OrderService orderService;
    private final ProductService productService;

    @Autowired
    private final JavaMailSender javaMailSender;

    @Autowired
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    private final AddressService addressService;

    private final ReviewService reviewService;

    @GetMapping("/check-out")
    public String checkOut(Principal principal, Model model, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        } 
        else {
            CustomerDto customer = customerService.getCustomer(principal.getName());

            Long id = customerService.getCustomerId(customer.getUsername());
            Address address = addressService.getAddressByCustomerId(id);
            if(address==null){
                model.addAttribute("page", "profile");
                redirectAttributes.addFlashAttribute("warning", "Please complete address information first, then try to proceed further...!");
                return "redirect:/profile";
            }
            else{
                ShoppingCart cart = customerService.findByUsername(principal.getName()).getCart();

                if(cart.getCartItems().isEmpty() || cart.getTotalItems()==0){
                    redirectAttributes.addFlashAttribute("warning", "Please add items to cart to proceed further...!");
                    return "redirect:/cart";
                }

                model.addAttribute("customer", customer);
                model.addAttribute("address", address);
                model.addAttribute("title", "CheckOut Page");
                model.addAttribute("page", "Check-Out");
                model.addAttribute("shoppingCart", cart);
                model.addAttribute("grandTotal", cart.getTotalItems());
                return "checkout";
            }
        }
    }

    @GetMapping("/orders")
public String getOrders(Model model, Principal principal) {
    if (principal == null) {
        return "redirect:/login";
    } else {
        Customer customer = customerService.findByUsername(principal.getName());
        List<Order> orderList = customer.getOrders();
        ListIterator<Order> listIterator = orderList.listIterator(orderList.size());

        List<List<Product>> productsList = new ArrayList<>();
        List<List<OrderDetail>> orderDetailsList = new ArrayList<>();

        while (listIterator.hasPrevious()) {
            Order order = listIterator.previous();
            Long orderId = order.getId();

            List<Product> productList = orderDetailRepository.getProductsByOrderId(orderId);
            productsList.add(productList);

            List<OrderDetail> orderDetails = new ArrayList<>();
            for (Product product : productList) {
                Long productId = product.getId();
                int quantity = orderDetailRepository.getQuantityByProductIdAndOrderId(productId, orderId);
                OrderDetail detail = new OrderDetail();
                detail.setProductQuantity(quantity);
                orderDetails.add(detail);
            }

            orderDetailsList.add(orderDetails);
        }

        Address address = addressService.getAddressByCustomerId(customer.getId());

        model.addAttribute("orders", orderList);
        model.addAttribute("address", address);
        model.addAttribute("productsList", productsList);
        model.addAttribute("orderDetailsList", orderDetailsList);
        model.addAttribute("title", "View Orders");
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("page", "order");

        return "order";
    }
    }

    @GetMapping("/orders/status")
    public String viewOrders(@RequestParam(required = false) OrderStatus status, 
        Principal principal, Model model) {

        if(principal == null){
            return "redirect:/login";
        }

        String username = principal.getName();
        Customer customer = customerService.findByUsername(username);
        List<Order> orders = new ArrayList<Order>();
        if(status.toString().equals("ALL")){
            orders = orderService.findAll(username);
        }
        else{
            orders = orderService.getOrdersByCustomerIdAndOrderStatus(customer.getId(), status);
        }
        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("allStatuses", OrderStatus.values());
        return "orders";
    }


    @PostMapping("/add-order")
    public String createOrder(Principal principal,
                              @RequestParam("paymentId") String paymentId,
                              @RequestParam("status") String status,
                              Model model, HttpSession session,
                              RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        Customer customer = customerService.findByUsername(principal.getName());
        ShoppingCart cart = customer.getCart();

        // 1. when cart empty or total price empty dont proceed
        if (cart.getCartItems().isEmpty() || cart.getTotalPrice() == 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty or order already placed.");
            return "redirect:/orders";
        }

        // 2. to save an order
        Order order = orderService.save(cart);
        order.setPaymentId(paymentId);
        order.setStatus(status);
        orderRepository.save(order); // to update paymentId and status

        // to remove items from sessin, after successfull order placed.
        session.removeAttribute("totalItems");

        // 3. to generate pdf
        try{
            sendOrderConfirmationEmail(order, customer);
        }
        catch(Exception e){
           e.printStackTrace();
        }

        // 4. to send user to order cofirmation page
        redirectAttributes.addFlashAttribute("successMessage", "Order has been placed successfully.");
        return "redirect:/order-confirmation?orderId=" + order.getId();
    }

    @GetMapping("/order-confirmation")
    public String orderConfirmation(@RequestParam("orderId") Long orderId, Model model, Principal principal,
                                    @org.springframework.web.bind.annotation.ModelAttribute("successMessage") String successMessage,
                                    @org.springframework.web.bind.annotation.ModelAttribute("errorMessage") String errorMessage) {
        if (principal == null) {
            return "redirect:/login";
        }

        Customer customer = customerService.findByUsername(principal.getName());
        Order order = orderService.getOrderByOrderId(orderId);

        if (order == null || !order.getCustomer().getId().equals(customer.getId())) {
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("title", "Order Detail");
        model.addAttribute("page", "order-detail");

        if (successMessage != null && !successMessage.isEmpty()) {
            model.addAttribute("success", successMessage);
        }
        if (errorMessage != null && !errorMessage.isEmpty()) {
            model.addAttribute("error", errorMessage);
        }

        return "order-detail";
    }

    // to send order confirmation and email to customer
    private void sendOrderConfirmationEmail(Order order, Customer customer) {
        try {
            String toEmail = customer.getUsername();
            String subject = "We’ve Received Your Order – Sport Shop";
            String firstName = customer.getFirstName();
            String lastName = customer.getLastName();

            List<Product> productsList = orderDetailRepository.getProductsByOrderId(order.getId());
            StringBuilder productDetails = new StringBuilder();

            double total = 0.0;
            for (Product product : productsList) {
                Long productId = product.getId();
                int quantity = orderDetailRepository.getQuantityByProductIdAndOrderId(productId, order.getId());
                double unitPrice = product.getCostPrice();
                double totalPrice = quantity * unitPrice;
                total += totalPrice;

                productDetails.append("Product: ").append(product.getName())
                        .append(" | Qty: ").append(quantity)
                        .append(" | Unit Price: ₹").append(unitPrice)
                        .append(" | Total: ₹").append(totalPrice)
                        .append("\n");
            }

            // 📄 Generate PDF receipt
            byte[] pdfBytes = PdfGenerator.generateOrderReceiptPdf(
                    String.valueOf(order.getId()),
                    firstName + " " + lastName,
                    productsList,
                    total
            );

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject(subject);

            String body = "Dear " + firstName + " " + lastName + ",\n\n"
                    + "Thank you for shopping with Sport Shop!\n\n"
                    + "Order ID: " + order.getId() + "\n"
                    + "Total Items: " + order.getQuantity() + "\n"
                    + "Total Amount: ₹" + order.getTotalPrice() + "\n\n"
                    + "Items:\n" + productDetails.toString()
                    + "\n\nYou can track order with given OrderId. Your receipt is attached as a PDF.\n\n"
                    + "Regards,\nSport Shop Team";

            helper.setText(body, false);
            helper.addAttachment(order.getId()+"_order-receipt.pdf", new ByteArrayResource(pdfBytes));

            javaMailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Error sending order confirmation email for Order ID: " + order.getId() + " - " + e.getMessage());
            //e.printStackTrace();
        }
    }

    @GetMapping("/cancel-order/{id}")
    public String cancelOrder(@PathVariable("id") Long id, 
                RedirectAttributes attributes) {
        Order order = orderService.cancelOrder(id);
        if (order!=null) {
            attributes.addFlashAttribute("success", "Order has been cancelled successfully!");
        } else {
            attributes.addFlashAttribute("error", "Error while cancelling order!");
        }
        return "redirect:/orders";
    }

    // View Receipt
    @GetMapping("/view-order-receipt/{id}")
    public String getOrderReceipt(@PathVariable("id") Long id,
            Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        } 
        else {
            Customer customer = customerService.findByUsername(principal.getName());
            Address address = addressService.getAddressByCustomerId(customer.getId());

            Order order = orderService.getOrderByOrderId(id);
            List<List<OrderDetail>> orderDetailsList = new ArrayList<>();
            List<Product> productsList = orderDetailRepository.getProductsByOrderId(order.getId());

            // to get and display quantity
            // Retrieve order details for the current order
            List<OrderDetail> orderDetails = new ArrayList<>();
            for (Product product : productsList) {
                Long productId = product.getId();
                int quantity = orderDetailRepository.getQuantityByProductIdAndOrderId(productId, order.getId());
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setProductQuantity(quantity);
                orderDetails.add(orderDetail); // Add order detail to the list
            }
            orderDetailsList.add(orderDetails);

            // Add orders and products to the model
            model.addAttribute("orders", order);
            model.addAttribute("orderDetails", orderDetails);
            model.addAttribute("address", address);
            model.addAttribute("customer", customer);
            model.addAttribute("productsList", productsList);
            model.addAttribute("orderDetailsList", orderDetailsList);
            model.addAttribute("title", "View Receipt");
            model.addAttribute("page", "Order");

            return "view-receipt";
        }
    }

    @GetMapping("/trackOrder")
    public String getOrderTracking(Model model, @RequestParam("orderId") Long orderId,
            @RequestParam("phoneNumber") String phoneNumber, Principal principal) {
    try{
        // Retrieve order safely
        Optional<Order> optionalOrder = 
                Optional.ofNullable(orderService.getOrderByOrderId(orderId));

        // Check if order exists
        if (!optionalOrder.isPresent()) {
            model.addAttribute("status", "Order with ID-" + orderId + " not found for user with mobile number " + phoneNumber);
            return "track-order";
        }

        Order order = optionalOrder.get();

        // Validate phone number
        if (!order.getCustomer().getPhoneNumber().equalsIgnoreCase(phoneNumber)) {
            model.addAttribute("status", "Incorrect mobile number.");
            return "track-order";
        }

        // Determine order status message
        String orderStatusResult;
        OrderStatus orderStatus = order.getOrderStatus();

        switch (orderStatus) {
            case PENDING:
                orderStatusResult = "Order with OrderId: " + orderId + " is PENDING.";
                break;
            case CONFIRMED:
                orderStatusResult = "Order with OrderId: " + orderId + " is CONFIRMED.";
                break;
            case CANCELLED:
                orderStatusResult = "Order with OrderId: " + orderId + " is CANCELLED.";
                break;
            case ASSIGNED:
                orderStatusResult = "Order with OrderId: " + orderId + " is ASSIGNED.";
                break;
            case SHIPPED:
                orderStatusResult = "Order with OrderId: " + orderId + " is SHIPPED.";
                break;
            case DELIVERED:
                orderStatusResult = "Order with OrderId: " + orderId + " is DELIVERED.";
                break;
            default:
                orderStatusResult = "Unknown order status for OrderId: " + orderId;
        }
        // Add attributes to model
        model.addAttribute("order", optionalOrder.get());
        model.addAttribute("orderStatusResult", orderStatusResult);
        model.addAttribute("page", "track-order");
        model.addAttribute("title", "Track Order");

        return "track-order";
    }
    catch(ResourceNotFoundException rnfe){
        model.addAttribute("status", rnfe.getMessage());
        return "track-order";
    }
}

    @GetMapping("/write-review/{id}/{prodId}")
    public String getWriteReview(@PathVariable("id") Long id, @PathVariable("prodId") Long prodId,
            Model model, Principal principal) {

        Customer customer = customerService.findByUsername(principal.getName());
        Order order = orderService.getOrderByOrderId(id);

        List<Product> productsList = orderDetailRepository.getProductsByOrderId(order.getId());
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (Product product : productsList) {
            Long productId = product.getId();
            int quantity = orderDetailRepository.getQuantityByProductIdAndOrderId(productId, order.getId());
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProductQuantity(quantity);
            orderDetails.add(orderDetail);
        }

        ProductDto productDto = productService.getById(prodId);
        Product product = new Product();
        product.setName(productDto.getName());

        Review review = reviewService.getReviewByCustomerIdAndProductId(customer.getId(), prodId);

        if (review != null) {
            model.addAttribute("review", review);
        } else {
            Review review2 = new Review();
            review2.setProductId(prodId);
            model.addAttribute("review", review2);
        }
        model.addAttribute("orders", order);
        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("customer", customer);
        model.addAttribute("productsList", productsList);
        model.addAttribute("product", product);
        model.addAttribute("title", "Write Review");

        return "write-review";
    }

    // to filters order by status
    @GetMapping("/get-orders-by-status")
    public String filterOrders(@RequestParam("status") String orderStatus,
                               Model model, Principal principal) {
    if (principal == null) {
        return "redirect:/login";
    }
    
    String username = principal.getName();
    Long customerId = customerService.getCustomerId(username);
    Customer customer = customerService.getCustomerById(customerId);
    
    // Assuming you have a method to get orders by customer ID
    List<Order> ordersList = customer.getOrders();

    // Filter orders by status using equals method
    List<Order> filteredOrders = ordersList.stream()
        .filter(order -> orderStatus.equals(order.getOrderStatus().toString()))
        .collect(Collectors.toList());

    model.addAttribute("title", "Orders");
    model.addAttribute("orders", filteredOrders); // Add filtered orders to the model
    return "order";
}
}
