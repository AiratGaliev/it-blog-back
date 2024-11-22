package com.github.codogma.codogmaback.service;

import com.github.codogma.codogmaback.util.LocalizationUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Locale;
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
  private final LocalizationUtil localizationUtil;

  public void sendEmailVerification(String email, String token, String origin) {
    Locale locale = localizationUtil.getLocale();
    String confirmationUrl = String.format("%s/%s/confirm?token=%s", origin, locale, token);
    String subject = localizationUtil.getMessage("email.verification.subject");
    String message = localizationUtil.getMessage("email.verification.body",
        new Object[]{confirmationUrl});
    sendEmail(email, subject, message);
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
