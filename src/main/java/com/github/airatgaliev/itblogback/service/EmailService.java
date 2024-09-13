package com.github.airatgaliev.itblogback.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  public void sendEmailVerification(String email, String token, String origin) {
    String confirmationUrl = String.format("%s/confirm?token=%s", origin, token);
    String message =
        "To confirm your email address, please click the following link: " + confirmationUrl;
    sendEmail(email, "Email Confirmation", message);
  }

  private void sendEmail(String to, String subject, String content) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
      helper.setText(content, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setFrom("noreply.linkednote@gmail.com");
      mailSender.send(mimeMessage);
    } catch (MessagingException e) {
      throw new IllegalStateException("Failed to send email", e);
    }
  }
}
