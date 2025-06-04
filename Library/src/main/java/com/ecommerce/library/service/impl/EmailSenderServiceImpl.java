package com.ecommerce.library.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.ecommerce.library.model.Product;
import com.ecommerce.library.service.EmailSenderService;
import com.ecommerce.library.utils.PdfGenerator;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailSenderServiceImpl implements EmailSenderService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendSimpleEmail(String toEmail,String body,String subject){

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();

        simpleMailMessage.setTo(toEmail);
        simpleMailMessage.setFrom("ghodkemayuresh86@gmail.com");
        simpleMailMessage.setText(body);
        simpleMailMessage.setSubject(subject);

        javaMailSender.send(simpleMailMessage);

    }

    @Override
    public void sendOrderReceipt(String toEmail, String orderId, String customerName,
                                List<Product> productList, double total) throws MessagingException {

        // Generate the PDF receipt
        byte[] pdfBytes = PdfGenerator.generateOrderReceiptPdf(orderId, customerName, productList, total);

        // Create email message
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("Your SPORTNEST Order Receipt");
        helper.setText("Dear " + customerName + ",\n\nPlease find your order receipt attached.\n\nThank you for shopping with SPORTNEST!", false);

        // Attach the PDF
        helper.addAttachment(orderId+"receipt.pdf", new ByteArrayResource(pdfBytes));

        // Send the email
        javaMailSender.send(message);
    }

}
