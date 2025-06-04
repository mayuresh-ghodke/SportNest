package com.ecommerce.admin.controller;

import com.ecommerce.library.dto.AdminDto;
import com.ecommerce.library.model.Admin;
import com.ecommerce.library.service.AdminService;
import com.ecommerce.library.service.CategoryService;
import com.ecommerce.library.service.CustomerService;
import com.ecommerce.library.service.OrderService;
import com.ecommerce.library.service.OtpService;
import com.ecommerce.library.service.ProductService;
import com.ecommerce.library.service.SubCategoryService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AdminService adminService;

    private final BCryptPasswordEncoder passwordEncoder;

    private final ProductService productService;
    private final CategoryService categoryService;
    private final SubCategoryService subCategoryService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final OtpService otpService;

    

    @RequestMapping("/admin/login")
    public String login(Model model) {
        model.addAttribute("title", "Admin Login Page");
        return "login";
    }

    @RequestMapping("/index")
    public String index(Model model) throws Exception {
        model.addAttribute("title", "Admin Dashboard");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        int productSize = productService.allProduct().size();
        int categorySize = categoryService.findALl().size();
        int subCategorySize = subCategoryService.findAllSubCategories().size();
        int customerSize = customerService.getAllCustomers().size();
        int orderSize = orderService.findALlOrders().size();
        int pendingOrderSize = orderService.getAllPendingOrders().size();
        int acceptedOrders = orderService.getAllAcceptedOrders().size();
        int deliveredOrders = orderService.getAllDeliveredOrders().size();

        model.addAttribute("productSize", productSize);
        model.addAttribute("categorySize", categorySize);
        model.addAttribute("subCategorySize", subCategorySize);
        model.addAttribute("customerSize", customerSize);
        model.addAttribute("orderSize", orderSize);
        model.addAttribute("pendingOrderSize", pendingOrderSize);
        model.addAttribute("acceptedOrders", acceptedOrders);
        model.addAttribute("deliveredOrders", deliveredOrders);

        return "index";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("title", "Register");
        model.addAttribute("adminDto", new AdminDto());
        return "register";
    }

    @PostMapping("/register-new")
    public String addNewAdmin(@Valid @ModelAttribute("adminDto") AdminDto adminDto,
                              BindingResult result,
                              Model model) {

        try {

            if (result.hasErrors()) {
                model.addAttribute("adminDto", adminDto);
                return "register";
            }
            String username = adminDto.getUsername();
            Admin admin = adminService.findByUsername(username);
            if (admin != null) {
                model.addAttribute("adminDto", adminDto);
                //System.out.println("admin not null");
                model.addAttribute("emailError", "Your email has been registered!");
                return "register";
            }
            if (adminDto.getPassword().equals(adminDto.getRepeatPassword())) {
                adminDto.setPassword(passwordEncoder.encode(adminDto.getPassword()));
                adminService.save(adminDto);
                //System.out.println("success");
                model.addAttribute("success", "Register successfully!");
                model.addAttribute("adminDto", adminDto);
            } else {
                model.addAttribute("adminDto", adminDto);
                model.addAttribute("passwordError", "Your password maybe wrong! Check again!");
                System.out.println("password not same");
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errors", "The server has been wrong!");
        }
        return "register";

    }

    // forgot password logic

    // Forgot Password:- 
    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model){
        model.addAttribute("title", "Forgot Password");
        return "forgot-password";
    }

    @PostMapping("/forgotPassword")
    public String forgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes, HttpSession session){

        Admin admin = adminService.findByUsername(email);
        if(admin==null){
            
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

        boolean isOtpValid = otpService.verifyOtp(enteredOtp); 

        if (!isOtpValid) {
            redirectAttributes.addFlashAttribute("error", "Invalid or expired OTP. Please try again.");
            return "redirect:/forgot-password";
        }
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
        Admin admin = adminService.findByUsername(email);

        if(admin==null){
            redirectAttributes.addFlashAttribute("error", "Error occured. Password not reset.");
            return "redirect:/forgot-password";
        }
        admin.setPassword(passwordEncoder.encode(newPassword));
        AdminDto adminDto = new AdminDto();
        adminDto.setPassword(admin.getPassword());
        adminService.save(adminDto);

        redirectAttributes.addFlashAttribute("success", "Password reseted successfully.");
        return "redirect:/admin/login";
    }
}
