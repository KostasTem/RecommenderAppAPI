package com.unipi.cs.kt.appBackendTest.Utils;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class SendEmail implements Runnable{
    private final JavaMailSender javaMailSender;
    private final String email;
    private final String token;

    public SendEmail(JavaMailSender javaMailSender, String email, String token) {
        this.javaMailSender = javaMailSender;
        this.email = email;
        this.token = token;
    }

    @Override
    public void run() {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Your password reset link");
        String link = "https://localhost:8443/api/user/resetPassword?token=" + token;
        String text = "Click the following link to reset your password: \n" + link;
        msg.setText(text);
        javaMailSender.send(msg);
    }
}
