package com.ecommerce.admin.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.ecommerce.library.exception.ErrorDetails;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(NoHandlerFoundException ex, Model model) {
        model.addAttribute("message", "The page you are looking for doesn't exist.");
        model.addAttribute("title", "404 - Page Not Found");
        return "page-not-found"; 
    }


    @ExceptionHandler(Exception.class)
    public String handleGlobalException(Exception ex,
                                        HttpServletRequest request,
                                        Model model) {
        ErrorDetails errorDetails = new ErrorDetails(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage(),
                request.getRequestURI()
        );
        model.addAttribute("errorDetails", errorDetails);
        model.addAttribute("title", "Unexpected Error");
        return "error"; 
    }
}
