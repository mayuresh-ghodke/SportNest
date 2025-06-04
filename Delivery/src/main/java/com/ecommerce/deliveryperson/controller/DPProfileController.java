package com.ecommerce.deliveryperson.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecommerce.library.model.DeliveryPerson;
import com.ecommerce.library.service.DeliveryPersonService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DPProfileController {
    
    @Autowired
    private DeliveryPersonService deliveryPersonService;

    @RequestMapping("/profile")
    public String getProfilePage(Model model, Principal principal){
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/dp-login";
        }

        if(principal == null){
            return "redirect:/dp-login";
        }

        String username = principal.getName();
        DeliveryPerson deliveryPerson = deliveryPersonService.getDeliveryPersonByEmail(username);
        model.addAttribute("deliveryPerson", deliveryPerson);
        model.addAttribute("title", "Profile Page");
        return "profile";
    }

    @PostMapping("/update-profile")
    public String updateDeliveryPersonProfile(@ModelAttribute("deliveryPerson") DeliveryPerson deliveryPerson
    , Model model, Principal principal){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/dp-login";
        }
        if(principal == null){
            return "redirect:/dp-login";
        }
        String userName = principal.getName();
        DeliveryPerson existingDeliveryPerson= deliveryPersonService.getDeliveryPersonByEmail(userName);
        if(existingDeliveryPerson!=null){
            existingDeliveryPerson.setFirstName(deliveryPerson.getFirstName());
            existingDeliveryPerson.setLastName(deliveryPerson.getLastName());
            existingDeliveryPerson.setEmail(deliveryPerson.getEmail());
            existingDeliveryPerson.setPhoneNumber(deliveryPerson.getPhoneNumber());

            deliveryPersonService.updateDeliveryPerson(existingDeliveryPerson);
            
        } 
        deliveryPerson = existingDeliveryPerson;
        model.addAttribute("deliveryPerson", existingDeliveryPerson);
        model.addAttribute(("title"), "Profile");
        return "profile";
    }
}
