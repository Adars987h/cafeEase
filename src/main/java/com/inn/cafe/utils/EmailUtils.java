package com.inn.cafe.utils;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
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

    @Async
    public void sendHtmlMessage(String to, String subject, String htmlBody, List<String> ccList) throws MessagingException {
        MimeMessage message = emailsender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true indicates HTML

        if (ccList != null && !ccList.isEmpty()) {
            helper.setCc(ccList.toArray(new String[0]));
        }

        emailsender.send(message);
        log.info("Sent Mail to customer");
    }

    private String[] getCcArray(List<String> ccList){
        String[] cc=new String[ccList.size()];
        for(int i=0;i<ccList.size();i++){
            cc[i]=ccList.get(i);
        }
        return cc;
    }

    public void sendPasswordReset(String to, String resetLink) throws MessagingException {
        MimeMessage message = emailsender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
        helper.setTo(to);
        helper.setSubject("Reset your CafeEase password");
        helper.setText("<p>Someone asked to reset the password for your CafeEase account.</p>"
                + "<p><a href=\"" + resetLink + "\">Choose a new password</a>. The link works once and expires in 30 minutes.</p>"
                + "<p>If that wasn't you, ignore this email. Your password hasn't changed.</p>", true);
        emailsender.send(message);
    }

    public void sendWithAttachment(String to, String subject, String htmlBody, String fileName, byte[] attachment)
            throws MessagingException {
        MimeMessage message = emailsender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        helper.addAttachment(fileName, new org.springframework.core.io.ByteArrayResource(attachment), "application/pdf");
        emailsender.send(message);
    }

}
