package in.omtalaviya.moneymanager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // Load from application.properties (optional)
//    @Value("${spring.mail.username}")
//    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("omtalaviya05@gmail.com"); // safer: uses your verified gmail sender
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            System.out.println("✅ Email sent to: " + to);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ SMTP Error: " + e.getMessage());
            throw new RuntimeException("Email sending failed: " + e.getMessage());
        }

    }
}
