package com.ecommerce.deliveryperson.controller;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.library.model.Order;
import com.ecommerce.library.service.OrderService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@RestController
public class SmsOTPController {

    // to store generated orderId, otp
    ConcurrentHashMap<String, String> hMap= new ConcurrentHashMap<String, String>();

    @Autowired
    private OrderService orderService;

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String phoneNumber;

    public Integer generateOtp(){
        Random random = new Random();
        return random.nextInt(999999);
    }

    String generatedOtp = "";
    @GetMapping("/order-detail/sendOtpSms/{orderId}")
    public ResponseEntity<String> sendOtpSms(@PathVariable("orderId") Long orderId) {
        try
        {
            System.out.println("Send otp sms api called....");
            Twilio.init(accountSid, authToken);

            // use order to generate dynamic mobile number to send otp
            //Order order = orderService.getOrderByOrderId(orderId);
             
            String toMobileNumber = "+917559201990";
            generatedOtp = generateOtp().toString();

            hMap.put(orderId.toString(), generatedOtp);
            Message.creator(
                    new PhoneNumber(toMobileNumber),
                    new PhoneNumber(phoneNumber),
                    "Your OTP for order OrderID-"+orderId+" delivery confirmation is "+generatedOtp
            ).create();

            System.out.println("Message sent successfully...");

            return new ResponseEntity<>("Message sent successfully", HttpStatus.OK);
        } 
        catch (Exception e)
        {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to send message: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/verifyOtp/{orderId}/{otp}")
    public ResponseEntity<?> verifyEnteredOtp(@PathVariable("otp") String otp, @PathVariable("orderId") String orderId) {
        System.out.println("Verifying OTP for Order ID: " + orderId);

        // Retrieve OTP from the map using the orderId
        String prevOtp = hMap.get(orderId);
        System.out.println("Stored OTP: " + prevOtp);

        // Check if the OTP is present for the given orderId
        if (prevOtp == null) {
            System.out.println("OTP not found for Order ID: " + orderId);
            return new ResponseEntity<>("OTP is invalid", HttpStatus.UNAUTHORIZED);
        }

        // Compare the entered OTP with the stored OTP
        if (prevOtp.equals(otp)) {
            System.out.println("OTP matched!");

            hMap.remove(orderId); 

            orderService.updateDeliveryStatusOnDelivery(Long.parseLong(orderId), true);
            return new ResponseEntity<>("OTP has been verified", HttpStatus.OK);
        } 
        else {
            return new ResponseEntity<>("OTP does not match", HttpStatus.UNAUTHORIZED);
        }
    }
}
