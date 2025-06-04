package com.ecommerce.deliveryperson.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.library.model.DeliveryPerson;
import com.ecommerce.library.service.DPOrderService;
import com.ecommerce.library.service.DeliveryPersonService;
import com.ecommerce.library.service.OtpService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DPController {

    @Autowired
    private DeliveryPersonService deliveryPersonService;

    @Autowired
    private DPOrderService dpOrderService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private OtpService otpService;

    @RequestMapping("/dp-login")
    public String dplogin(Model model) {
        model.addAttribute("title", "Delivery Person Login Page");
        return "dp-login"; 
    }

    @RequestMapping("/delivery-person-dashboard")
    public String getDashboardPage(Model model){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/dp-login";
        }

        String dpUsername = authentication.getName();
        DeliveryPerson deliveryPerson = deliveryPersonService.getDeliveryPersonByEmail(dpUsername);

        int assignedOrderCount = dpOrderService.getAllAssignedOrders(deliveryPerson.getId()).size();
        int shippedOrderCount = dpOrderService.getAllShippedOrders(deliveryPerson.getId()).size();
        int deliveredOrderCount = dpOrderService.getAllDeliveredOrders(deliveryPerson.getId()).size();
        
        model.addAttribute("dpObj", deliveryPerson);
        model.addAttribute("title", "Delivery Dashboard");
        model.addAttribute("assignedOrderCount", assignedOrderCount);
        model.addAttribute("shippedOrderCount", shippedOrderCount);
        model.addAttribute("deliveredOrderCount", deliveredOrderCount);

        
        return "delivery-person-dashboard";
    }

    // Forgot Password:- 
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model){
        model.addAttribute("title", "Forgot Password");
        return "forgot-password";
    }

    @PostMapping("/forgotPassword")
    public String forgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes, HttpSession session){

        DeliveryPerson deliveryPerson = deliveryPersonService.getDeliveryPersonByEmail(email);
        if(deliveryPerson==null){
            
            redirectAttributes.addFlashAttribute("error", "Email is not registered with us.");
            return "redirect:/forgot-password";
        }

        otpService.getOnGenerateAndSendOtp(email);
        session.setAttribute("resetPasswordEmail", email);

        return "redirect:/checkOtp";
    }

    @GetMapping("/checkOtp")
    public String getCheckOtp(Model model, HttpSession session){
        model.addAttribute("title", "Check OTP");
        String sessionEmail = (String) session.getAttribute("resetPasswordEmail");
        model.addAttribute("email", sessionEmail);
        return "checkOtp";
    }

    @PostMapping("/verifyPasswordOtp")
    public String verifyOtp(@RequestParam("email") String email,
                            @RequestParam("otp") String enteredOtp,
                            RedirectAttributes redirectAttributes, Model model) {

        System.out.println("Entered in verifyPasswordOtp");
        boolean isOtpValid = otpService.verifyOtp(enteredOtp); 
        System.out.println("Is OTP Valid: "+isOtpValid);

        if (!isOtpValid) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP. Please try again.");
            return "redirect:/forgot-password";
        }
        System.out.println("Email: "+email);
        model.addAttribute("email", email);
        return "redirect:/reset-password?email="+email;
    }

    @GetMapping("/reset-password")
    public String getResetPassword(@RequestParam("email")String email, Model model, HttpSession session){
        model.addAttribute("email", email);
        // session.setAttribute("resetPasswordEmail", email);
        model.addAttribute("title", "Reset Password");
        return "reset-password";
    }

    @PostMapping("/createNewPassword")
    public String createNewPassword(@RequestParam("newPassword") String newPassword, HttpSession session, RedirectAttributes redirectAttributes){


        String email = (String) session.getAttribute("resetPasswordEmail");
        DeliveryPerson dp = deliveryPersonService.getDeliveryPersonByEmail(email);

        if(dp==null){
            redirectAttributes.addFlashAttribute("error", "Error occured. Password not reset.");
            return "redirect:/forgot-password";
        }
        dp.setPassword(passwordEncoder.encode(newPassword));
        deliveryPersonService.updateDeliveryPerson(dp);

        System.out.println("DP: "+dp.getEmail()+" : "+dp.getPassword());
        redirectAttributes.addFlashAttribute("success", "Password reseted successfully.");
        return "redirect:/dp-login";
    }


    @GetMapping("/change-password")
    public String getChangePassword(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/dp-login";
        }

        String dpUsername = authentication.getName();
        DeliveryPerson deliveryPerson = deliveryPersonService.getDeliveryPersonByEmail(dpUsername);
        model.addAttribute("userId", deliveryPerson.getId());
        model.addAttribute("title", "Change Password");
        return "change-password";
    }

    @PostMapping("/changePassword")
public String changePassword(
        @RequestParam("userId") String userId,
        @RequestParam("oldPassword") String oldPassword,
        @RequestParam("newPassword") String newPassword,
        @RequestParam("confirmPassword") String confirmPassword,
        RedirectAttributes redirectAttributes) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
        return "redirect:/dp-login";
    }

    String dpUsername = authentication.getName();
    DeliveryPerson deliveryPerson = deliveryPersonService.getDeliveryPersonByEmail(dpUsername);

    if (!userId.equals(deliveryPerson.getId().toString())) {
        redirectAttributes.addFlashAttribute("error", "Unauthorized action.");
        return "redirect:/profile"; 
    }

    if (!passwordEncoder.matches(oldPassword, deliveryPerson.getPassword())) {
        redirectAttributes.addFlashAttribute("error", "Old password is incorrect.");
        return "redirect:/profile";
    }

    if (!newPassword.equals(confirmPassword)) {
        redirectAttributes.addFlashAttribute("error", "New password and confirm password do not match.");
        return "redirect:/profile";
    }

    deliveryPerson.setPassword(passwordEncoder.encode(newPassword));
    deliveryPersonService.updateDeliveryPerson(deliveryPerson);

    redirectAttributes.addFlashAttribute("success", "Password changed successfully.");
    return "redirect:/profile";
}

}
