package com.ecommerce.customer.controller;

import com.ecommerce.library.dto.ProductDto;
import com.ecommerce.library.model.Customer;
import com.ecommerce.library.model.ShoppingCart;
import com.ecommerce.library.service.CustomerService;
import com.ecommerce.library.service.ProductService;
import com.ecommerce.library.service.ShoppingCartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
@Controller
@RequiredArgsConstructor
public class ShoppingCartController {
    
    private final ShoppingCartService cartService;
    private final ProductService productService;
    private final CustomerService customerService;

    @GetMapping("/cart")
    public String cart(Model model, Principal principal, HttpSession session) {
        if (principal == null) {
            return "redirect:/login";
        }
        Customer customer = customerService.findByUsername(principal.getName());
        ShoppingCart cart = customer.getCart();
        if (cart == null) {
            model.addAttribute("check");
        }
        if (cart != null) {
            model.addAttribute("grandTotal", cart.getTotalPrice());
        }
        model.addAttribute("shoppingCart", cart);
        model.addAttribute("title", "Cart");
        if(cart== null){
            session.setAttribute("totalItems", 0);
        }
        else{
            session.setAttribute("totalItems", cart.getTotalItems());
        }
        return "cart";
    }

    @PostMapping("/add-to-cart")
    public String addItemToCart(
        @RequestParam("id") Long id,
        @RequestParam(value = "quantity", required = false, defaultValue = "1") int quantity,
        HttpServletRequest request, Model model, Principal principal, HttpSession session)
    {
        ProductDto productDto = productService.getById(id);
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        ShoppingCart shoppingCart = cartService.addItemToCart(productDto, quantity, username);
        session.setAttribute("totalItems", shoppingCart.getTotalItems());
        model.addAttribute("shoppingCart", shoppingCart);
        return "redirect:" + request.getHeader("Referer");
    }

    @PostMapping(value = "/update-cart", params = "action=update")
@ResponseBody
public Map<String, Object> updateCart(@RequestParam("id") String id,
                                      @RequestParam("quantity") int quantity,
                                      Principal principal,
                                      HttpSession session) {
    if (principal == null) {
        return Collections.singletonMap("error", "User not logged in");
    }

    Long productId = Long.parseLong(id);
    ProductDto productDto = productService.getById(productId);
    String username = principal.getName();
    ShoppingCart shoppingCart = cartService.updateCart(productDto, quantity, username);

    session.setAttribute("totalItems", shoppingCart.getTotalItems());
    
    HashMap<Long, Double> hMapTotalPriceByQty = new HashMap<Long, Double>();

    Double totalCartItemPrice =  productDto.getCostPrice() * quantity;
    hMapTotalPriceByQty.put(productId, totalCartItemPrice);

    // Return JSON response
    Map<String, Object> response = new HashMap<>();
    response.put("newGrandTotal", shoppingCart.getTotalPrice());
    response.put("totalItems", shoppingCart.getTotalItems());
    response.put("totalPriceByQty", hMapTotalPriceByQty);
    
    return response;
}

    

    @RequestMapping(value = "/update-cart", method = RequestMethod.POST, params = "action=delete")
    public String deleteItem(@RequestParam("id") Long id,
        Model model, Principal principal, HttpSession session
    ) {
        if (principal == null) {
            return "redirect:/login";
        } 
        else {
            ProductDto productDto = productService.getById(id);
            String username = principal.getName();
            ShoppingCart shoppingCart = cartService.removeItemFromCart(productDto, username);
            model.addAttribute("shoppingCart", shoppingCart);
            session.setAttribute("totalItems", shoppingCart.getTotalItems());
            return "redirect:/cart";
        }
    }

}
