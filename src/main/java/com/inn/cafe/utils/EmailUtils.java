package com.inn.cafe.utils;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailUtils {

    @Autowired
    private JavaMailSender emailsender;

    public void sendSimpleMessage(String to, String subject, String text, List<String> list) {
        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom("mailerspring1@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        if (list != null && list.size() > 0){
            message.setCc(list.toArray(new String[0]));
        }
        emailsender.send(message);

    }

    private String[] getCcArray(List<String> ccList){
        String[] cc=new String[ccList.size()];
        for(int i=0;i<ccList.size();i++){
            cc[i]=ccList.get(i);
        }
        return cc;
    }

    public void forgotMail(String to ,String subject, String password) throws MessagingException{
        MimeMessage message = emailsender.createMimeMessage();
        MimeMessageHelper helper =new MimeMessageHelper(message,true);
        helper.setFrom("emailerspring1@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        String htmlMsg = "<p><b>Your Login details for Cafe Management System</b><br><b>Email: </b>" + to +" <br><b>Password: </b>" + password + "<br><a href=\"http://localhost:4200/\">Click here to login</a></p>";
        message.setContent(htmlMsg,"text/html");
        emailsender.send(message);
    }

}