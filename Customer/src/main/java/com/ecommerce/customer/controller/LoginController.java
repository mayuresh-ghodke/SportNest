package com.ecommerce.customer.controller;

import com.ecommerce.library.dto.CustomerDto;
import com.ecommerce.library.model.Customer;
import com.ecommerce.library.service.CustomerService;
import com.ecommerce.library.service.EmailSenderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class LoginController {
    
    private final CustomerService customerService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailSenderService emailSenderService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(Model model) {
        model.addAttribute("title", "Login Page");
        model.addAttribute("page", "Home");
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("title", "Register");
        model.addAttribute("page", "Register");
        model.addAttribute("customerDto", new CustomerDto());
        return "register";
    }

    @PostMapping("/do-register") 
    public String registerCustomer(@Valid @ModelAttribute("customerDto") CustomerDto customerDto,
                                   BindingResult result,
                                   RedirectAttributes redirectAttributes) {
        try {
            if (result.hasErrors()) {
                redirectAttributes.addFlashAttribute("customerDto", customerDto);
                redirectAttributes.addFlashAttribute("page", "register");
                redirectAttributes.addFlashAttribute("title", "Registreation Failed");
                return "redirect:/register";
            }
            String username = customerDto.getUsername();
            Customer customer = customerService.findByUsername(username);

            if (customer != null) {
                redirectAttributes.addFlashAttribute("customerDto", customerDto);
                redirectAttributes.addFlashAttribute("error", "Email has been already registered.");
                return "redirect:/register";
            }
            if (customerDto.getPassword().equals(customerDto.getConfirmPassword())) {
                customerDto.setPassword(passwordEncoder.encode(customerDto.getPassword()));
                if(customerDto.getPassword().length()<6){
                    redirectAttributes.addFlashAttribute("customerDto", customerDto);
                    redirectAttributes.addFlashAttribute("error", "Password length must have 6 length.");
                    return "redirect:/register";
                }
                Customer registeredCustomer = customerService.save(customerDto);

                String subject = "Welcome to SportNest - Registration Successful!";
                String toEmail = registeredCustomer.getUsername();
                String body = "Dear " + registeredCustomer.getFirstName() + " " + registeredCustomer.getLastName() + ",\n\n"
            + "Welcome to Sport Shop!\n\n"
            + "You have been successfully registered with us. We're thrilled to have you on board.\n"
            + "Start exploring our wide range of sports equipment and enjoy a seamless shopping experience.\n\n"
            + "Happy Shopping!\n"
            + "Team SportNest Shop";

                emailSenderService.sendSimpleEmail(toEmail, body, subject);
                redirectAttributes.addFlashAttribute("success", "Registration successful! A confirmation email has been sent to your registered email address.");
            }
            else {
                redirectAttributes.addFlashAttribute("error", "Password and confirm password must be same..");
                redirectAttributes.addFlashAttribute("customerDto", customerDto);
                return "redirect:/register";
            }
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("customerDto", customerDto);
            redirectAttributes.addFlashAttribute("error", "Something went wrong. Please try again later...");
        }
        return "redirect:/register";
    }
}
