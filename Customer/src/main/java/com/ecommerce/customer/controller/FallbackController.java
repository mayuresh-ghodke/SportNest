package com.ecommerce.customer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecommerce.library.exception.PageNotFoundException;

@Controller
public class FallbackController {

    @RequestMapping("/shop/**")
    public String handleInvalidPath() {
        throw new PageNotFoundException("Invalid path. Please check the URL.");
    }

}
