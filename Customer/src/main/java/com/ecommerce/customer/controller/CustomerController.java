package com.ecommerce.customer.controller;

import com.ecommerce.library.dto.CustomerDto;
import com.ecommerce.library.model.Address;
import com.ecommerce.library.model.Customer;
import com.ecommerce.library.model.PaymentOrder;
import com.ecommerce.library.repository.PaymentOrderRepository;
import com.ecommerce.library.service.AddressService;
import com.ecommerce.library.service.CustomerService;
import com.ecommerce.library.service.OtpService;
import com.ecommerce.library.service.PaymentOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import com.razorpay.*;

@Controller
@RequiredArgsConstructor
public class CustomerController
{
    @Autowired
    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;
    private final PaymentOrderService paymentOrderService;

    @Autowired
    private final OtpService otpService;

    @Autowired
    private AddressService addressService;

    //this is for update order for payment
    private final PaymentOrderRepository paymentOrderRepository;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        if(principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        CustomerDto customerDto = customerService.getCustomer(username);
        Long id = customerService.getCustomerId(username);
        Address address = addressService.getAddressByCustomerId(id);
        if(address==null){
            model.addAttribute("address", new Address());
        }  
        else{
            model.addAttribute("address", address);
        }
        model.addAttribute("customer", customerDto);
        model.addAttribute("title", "Profile");
        model.addAttribute("page", "Profile");
        return "customer-information";
    }

    @PostMapping("/update-customer-address")
    public String updateAddress(@ModelAttribute("address") Address updatedAddress, 
        Model model, Principal principal, BindingResult result) {

    if (principal == null) {
        return "redirect:/login";
    }

    String username = principal.getName();
    Long customerId = customerService.getCustomerId(username);
    Customer customer = customerService.getCustomerById(customerId);
    
    // Retrieve the existing address by customer ID
    Address existingAddress = addressService.getAddressByCustomerId(customerId);
    
    if (existingAddress != null) {
        // Update the existing address with new information
        existingAddress.setStreet(updatedAddress.getStreet());
        existingAddress.setCity(updatedAddress.getCity());
        // Save the updated address
        addressService.save(existingAddress);
    }
    else{
        Address address = new Address();
        address.setCity(updatedAddress.getCity());
        address.setPincode(updatedAddress.getPincode());
        address.setCountry(updatedAddress.getCountry());
        address.setStreet(updatedAddress.getStreet());
        address.setState(updatedAddress.getState());
        address.setCustomer(customer);
        addressService.save(address);
    }
    return "redirect:/profile";
}


    @PostMapping("/update-profile")
    public String updateProfile(@Valid @ModelAttribute("customer") CustomerDto customerDto,
                                BindingResult result,
                                RedirectAttributes attributes,
                                Model model,
                                Principal principal) {
        if(principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        customerService.getCustomer(username);
        
        customerService.update(customerDto);
        CustomerDto customerUpdate = customerService.getCustomer(principal.getName());
        attributes.addFlashAttribute("success", "Update successfully!");
        model.addAttribute("customer", customerUpdate);
        return "redirect:/profile";
    }

    @GetMapping("/change-password")
    public String changePassword(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        model.addAttribute("title", "Change password");
        model.addAttribute("page", "Change password");
        return "change-password";
    }

    @PostMapping("/change-password")
    public String changePass(@RequestParam("oldPassword") String oldPassword,
                             @RequestParam("newPassword") String newPassword,
                             @RequestParam("repeatNewPassword") String repeatPassword,
                             RedirectAttributes attributes,
                             Model model,
                             Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        } 
        else
        {
            CustomerDto customer = customerService.getCustomer(principal.getName());
            if (passwordEncoder.matches(oldPassword, customer.getPassword())
                && !passwordEncoder.matches(newPassword, oldPassword)
                && !passwordEncoder.matches(newPassword, customer.getPassword())
                && repeatPassword.equals(newPassword) && newPassword.length() >= 5)
                {
                    customer.setPassword(passwordEncoder.encode(newPassword));
                    customerService.changePass(customer);
                    attributes.addFlashAttribute("success", "Your password has been changed successfully!");
                    return "redirect:/profile";
            } 
            else {
                model.addAttribute("message", "Your password is wrong");
                return "change-password";
            }
        }
    }

    // Forgot Password:- 

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model){
        model.addAttribute("title", "Forgot Password");
        return "forgot-password";
    }

    // Verify otp page for forgot password
    @GetMapping("/verify-otp")
    public String verfifyOTPPage(Model model){
        model.addAttribute("title", "Verify OTP");
        return "verify-otp";
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOTP(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Map<String, String> response = new HashMap<>();
        if (email != null && !email.isEmpty()) {
            otpService.generateAndSendOtp(email);
            response.put("status", "success");
            response.put("message", "OTP sent successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "error");
            response.put("message", "Invalid email");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/verify-entered-otp")
    public ResponseEntity<Map<String, Object>> verifyEnteredOtp(@RequestBody Map<String, String> request) {
        String otp = request.get("otp");
        Map<String, Object> response = new HashMap<>();
        boolean isValid = otpService.verifyOtp(otp);
        if (isValid) {
            response.put("valid", true);
            response.put("message", "OTP has been verified successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("valid", false);
            response.put("message", "Invalid OTP. Please try again.");
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Creating order for payment
    @PostMapping("/create_order")
    @ResponseBody
    public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception{
        String username = principal.getName();
        Long customerId = customerService.getCustomerId(username);
        double amount = Double.parseDouble(data.get("amount").toString()); // Parsing as double

        var client = new RazorpayClient("rzp_test_9NlQwnOUBr8xWJ", "QPkPgAbvVY5uKEX8XstWO9ld");

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

        this.paymentOrderService.save(paymentOrder);
        
        return order.toString();
    }
    
    @PostMapping("/update_order")
    public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderId(data.get("order_id").toString());

        paymentOrder.setPaymentId(data.get("payment_id").toString());
        paymentOrder.setOrderId(data.get("order_id").toString());
        paymentOrder.setStatus(data.get("status").toString());

        paymentOrderRepository.save(paymentOrder);

        return ResponseEntity.ok(Map.of("msg","updated"));
    }
}
