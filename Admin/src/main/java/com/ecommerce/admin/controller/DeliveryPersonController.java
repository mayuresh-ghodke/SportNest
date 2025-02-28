package com.ecommerce.admin.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ecommerce.library.model.DeliveryPerson;
import com.ecommerce.library.service.DeliveryPersonService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/delivery")
public class DeliveryPersonController {

    private final DeliveryPersonService deliveryPersonService;
    private final BCryptPasswordEncoder passwordEncoder;

    // @RequestMapping("/dp-login")
    // public String dplogin(Model model) {
    //     model.addAttribute("title", "Delivery Person Login Page");
    //     return "dp-login";
    // }

    // @GetMapping("/do-login-delivery-person")
    // public String getLogin(){
    //     System.out.println("Called here");
    //     return "delivery-person-dashboard";
    // }

    @GetMapping("/add-delivery-person")
    public String addDeliveryPerson(Model model) {
        DeliveryPerson deliveryPerson = new DeliveryPerson();
        model.addAttribute("deliveryPerson", deliveryPerson);
        model.addAttribute("title", "Add Delivery Person");
        return "add-delivery-person";
    }

    @PostMapping("/save-delivery-person")
public String saveDeliveryPerson(
        @ModelAttribute("deliveryPerson") DeliveryPerson deliveryPerson,
        Model model) 
{
    // Validate the password
    if (deliveryPerson.getPassword() == null || deliveryPerson.getPassword().trim().isEmpty()) {
        model.addAttribute("error", "Password cannot be null or empty.");
        model.addAttribute("title", "Add Delivery Person");
        return "add-delivery-person"; // Return to the form with an error message
    }

    // Set the delivery person as available and encode the password
    deliveryPerson.setAvailable(true);
    deliveryPerson.setPassword(passwordEncoder.encode(deliveryPerson.getPassword()));

    try {
        // Save the delivery person using the service
        DeliveryPerson dp = deliveryPersonService.createDeliveryPerson(deliveryPerson);
        
        // Success message
        model.addAttribute("success", "Delivery person added successfully.");
    } catch (Exception e) {
        // Handle any exception during the save operation
        model.addAttribute("error", "An error occurred while saving the delivery person: " + e.getMessage());
    }

    // Set the title and return the view
    model.addAttribute("title", "Add Delivery Person");
    return "add-delivery-person";
}



    @GetMapping("/delivery-persons")
    public String getAllDeliveryPersons(Model model, Principal principal)
    {
        if (principal == null) {
            return "redirect:/dp-login";
        }
        List<DeliveryPerson> deliveryPersonsList = deliveryPersonService.getAllDeliveryPersons();
        model.addAttribute("deliveryPersons", deliveryPersonsList);
        model.addAttribute("size", deliveryPersonsList.size());
        model.addAttribute("title", "View Delivery Persons");
        return "delivery-persons";
    }
}
