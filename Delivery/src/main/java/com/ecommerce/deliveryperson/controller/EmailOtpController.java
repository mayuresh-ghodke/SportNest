package com.ecommerce.deliveryperson.controller;

import com.ecommerce.library.model.Order;
import com.ecommerce.library.service.DeliveryPersonService;
import com.ecommerce.library.service.EmailSenderService;
import com.ecommerce.library.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequiredArgsConstructor
public class EmailOtpController {

    @Autowired
    private final DeliveryPersonService deliveryPersonService;

    @Autowired
    private final OrderService orderService;

    @Autowired
    private final EmailSenderService emailSenderService;

    public String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    @GetMapping("/order-detail/sendOtpEmail/{orderId}")
    public String sendOtpToEmail(@PathVariable("orderId") Long orderId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        Order order = orderService.getOrderByOrderId(orderId);
        String customerEmail = order.getCustomer().getUsername();

        String generatedOtp = generateOtp();
        session.setAttribute("generatedOtp", generatedOtp);
        session.setAttribute("otpOrderId", order.getId());

        String subject = "OTP for Order Confirmation";
        String body = "Dear " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() + ",\n\n" +
                "Your OTP for confirming the delivery of Order ID: " + orderId + " is: " + generatedOtp + "\n\n" +
                "Please provide this OTP to complete the delivery process.\n\n" +
                "Best regards,\n" +
                "E-Commerce Delivery Team";

        emailSenderService.sendSimpleEmail(customerEmail, subject, body);
        redirectAttributes.addFlashAttribute("otpModal", true);
        redirectAttributes.addFlashAttribute("success", "OTP has been sent successfully to your registered email.");
        return "redirect:/view-order/"+orderId;
    }

    @PostMapping("/verifyOtp")
    public String verifyOtp(@RequestParam("orderId") Long orderId,
                            @RequestParam("otp") String otp,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        String sessionOtp = (String) session.getAttribute("generatedOtp");

        if (sessionOtp == null || otp == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "OTP not valid or expired.");
            return "redirect:/view-order/" + orderId;
        }

        if (otp.equals(sessionOtp)) {
            Order order = orderService.getOrderByOrderId(orderId);
            orderService.updateDeliveryStatusOnDelivery(orderId, true);
            redirectAttributes.addFlashAttribute("successMessage", "OTP is validated.");
            String subject = "Order Delivery Confirmed - Order ID: " + orderId;

            String body = "Dear " + order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName() + ",\n\n" +
                    "We are pleased to inform you that your order with Order ID: " + orderId + " has been successfully delivered and confirmed using the provided OTP.\n\n" +
                    "Thank you for shopping with us. We hope you are satisfied with your purchase.\n\n" +
                    "If you have any questions or feedback, feel free to reach out to our support team.\n\n" +
                    "Best regards,\n" +
                    "E-Commerce Delivery Team";

            emailSenderService.sendSimpleEmail(order.getCustomer().getUsername(),
                    body, subject);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid OTP. Please try again.");
        }

        return "redirect:/view-order/" + orderId;
    }
}
