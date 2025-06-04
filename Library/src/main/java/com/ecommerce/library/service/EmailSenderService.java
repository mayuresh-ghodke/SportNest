package com.ecommerce.library.service;

import java.util.List;

import com.ecommerce.library.model.Product;

import jakarta.mail.MessagingException;

public interface EmailSenderService {

    public void sendSimpleEmail(String toEmail,String body,String subject);
    
    public void sendOrderReceipt(String toEmail, String orderId, String customerName,
                                 List<Product> productList, double total) throws MessagingException;
    
}