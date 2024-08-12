package com.project3.email;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailSenderImpl implements EmailSender{

    private final static Logger LOGGER = LoggerFactory.getLogger(EmailSenderImpl.class);
    private final JavaMailSender javaMailSender;


    @Override
    @Async
    public void send(String to, String email) {
        try{

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");

            mimeMessageHelper.setText(email, true);
            mimeMessageHelper.setSubject("Confirm your email");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setFrom("Hussein@gmail.com");
            javaMailSender.send(mimeMessage);


        } catch (MessagingException e) {
            LOGGER.error("failed to send email", e);
            throw new IllegalStateException("failed to send email");
        }
    }
}
