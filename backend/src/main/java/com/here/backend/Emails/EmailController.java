package com.here.backend.Emails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/email")
public class EmailController {
    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private EmailSenderService senderService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestParam String toEmail,
                                            @RequestParam String subject,
                                            @RequestParam String body) {
        try {
            senderService.sendSimpleEmail(toEmail, subject, body);
            return ResponseEntity.ok("Email sent successfully");
        } catch (MailException e) {
            return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
        }
    }
}
