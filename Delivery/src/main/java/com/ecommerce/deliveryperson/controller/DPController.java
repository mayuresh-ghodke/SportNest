package com.ecommerce.deliveryperson.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecommerce.library.model.DeliveryPerson;
import com.ecommerce.library.service.DeliveryPersonService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DPController {

    @Autowired
    private DeliveryPersonService deliveryPersonService;

    @RequestMapping("/dp-login")
    public String dplogin(Model model) {
        model.addAttribute("title", "Delivery Person Login Page");
        
        return "dp-login"; // Ensure this matches the Thymeleaf template name
    }

    @RequestMapping("/delivery-person-dashboard")
    public String getDashboardPage(Model model){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/dp-login";
        }

        String dpUsername = authentication.getName();
        DeliveryPerson deliveryPerson = deliveryPersonService.getDeliveryPersonByEmail(dpUsername);
        
        model.addAttribute("dpObj", deliveryPerson);
        model.addAttribute("title", "Delivery Dashboard");
        
        return "delivery-person-dashboard";
    }

    // @RequestMapping("/pending-orders")
    // public String getPendingOrdersPage()
    // {
    //     return "pending-orders";
    // }

    
}
