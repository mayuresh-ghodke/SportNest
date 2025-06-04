package com.ecommerce.library.service.impl;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.ecommerce.library.service.OtpService;

@Service
public class OtpServiceImpl implements OtpService{

    private String generatedOtp;
    private LocalDateTime otpExpiryTime;

    @Autowired
    private JavaMailSender javaMailSender;

    @Override
    public void generateAndSendOtp(String email) {
        generatedOtp = String.valueOf(new Random().nextInt(999999));
        otpExpiryTime = LocalDateTime.now().plusMinutes(5);
        sendOtpEmail(email, generatedOtp);
    }

    @Override
    public String getOnGenerateAndSendOtp(String email) {
        generatedOtp = String.valueOf(new Random().nextInt(999999));
        otpExpiryTime = LocalDateTime.now().plusMinutes(5);
        sendOtpEmail(email, generatedOtp);
        
        return generatedOtp;
    }

    // send otp to email
    @Override
    public void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code for Reset Password");
        message.setText("Your OTP code is: " + otp + "\n\nNote: This OTP is valid for 5 minutes.");
        javaMailSender.send(message);
    }

    // @Override
    // public boolean verifyOtp(String otp) {
    //     return otp.equals(generatedOtp);
    // }
    @Override
    public boolean verifyOtp(String otp) {
        if (generatedOtp == null || otpExpiryTime == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(otpExpiryTime)) {
            return false; // OTP has expired
        }

        boolean isValid = otp.equals(generatedOtp);
        
        if (isValid) {
            generatedOtp = null;
            otpExpiryTime = null;
        }

        return isValid;
    }    
}
