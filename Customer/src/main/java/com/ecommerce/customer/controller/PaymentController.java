package com.ecommerce.customer.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecommerce.library.model.Customer;
import com.ecommerce.library.model.PaymentOrder;
import com.ecommerce.library.service.CustomerService;
import com.ecommerce.library.service.PaymentOrderService;
import com.razorpay.*;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment")
public class PaymentController{

    @Autowired
    private final CustomerService customerService;

    @Autowired
    private final PaymentOrderService paymentOrderService;
    
    // Creating order for payment
    @PostMapping("/create_order")
    @ResponseBody
    public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception{

        if(principal==null){
            return "redirect:/login";
        }
        String username = principal.getName();
        Long customerId = customerService.getCustomerId(username);
        double amount = Double.parseDouble(data.get("amount").toString()); // Parsing as double

//here keys removed due to security purpose
        var client = new RazorpayClient("rzp_", "QPkPg");

         // Generate a unique transaction ID with date, time, customerId
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss"); // Format date-time
        String formattedDateTime = now.format(formatter); 

        String uniqueTransactionId = "txn_" + formattedDateTime + "_" + customerId.toString();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("amount", (int) amount *100); 
        jsonObject.put("currency", "INR");
        jsonObject.put("receipt", uniqueTransactionId);
        
        //creating new order, from here request goest to razorpay server
        Order order =  client.orders.create(jsonObject);
        
        // we save this order info in database
        PaymentOrder paymentOrder = new PaymentOrder();
        paymentOrder.setAmount(order.get("amount")+"");
        paymentOrder.setOrderId(order.get("id"));
        paymentOrder.setPaymentId("null");
        paymentOrder.setStatus("created");
        Customer customer = customerService.findByUsername(username);
        paymentOrder.setCustomer(customer);
        paymentOrder.setReceipt(order.get("receipt"));
        paymentOrder.setPaymentCreatedAt(LocalDateTime.now());

        this.paymentOrderService.save(paymentOrder);
        
        return order.toString();
    }
    
    @PostMapping("/update_order")
    public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data, Principal principal){

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(Map.of("msg", "User not authenticated. Please log in."));
        }
        PaymentOrder paymentOrder = paymentOrderService.findPaymentOrderByOrderId(data.get("order_id").toString());

        paymentOrder.setPaymentId(data.get("payment_id").toString());
        paymentOrder.setOrderId(data.get("order_id").toString());
        paymentOrder.setStatus(data.get("status").toString());
        paymentOrder.setPaymentCompletedAt(LocalDateTime.now());

        paymentOrderService.save(paymentOrder);

        return ResponseEntity.ok(Map.of("msg","updated"));
    }
}
