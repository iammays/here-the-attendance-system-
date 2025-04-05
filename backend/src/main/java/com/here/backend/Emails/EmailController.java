
//backend\src\main\java\com\here\backend\Emails\EmailController.java 

package com.here.backend.Emails;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.mail.MailException;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailSenderService senderService;

    @PostMapping("/send")
public ResponseEntity<String> sendEmail(@RequestBody Map<String, String> emailRequest) {
    try {
        String toEmail = emailRequest.get("toEmail");
        String subject = emailRequest.get("subject");
        String body = emailRequest.get("body");

        if (toEmail == null || subject == null || body == null) {
            return ResponseEntity.badRequest().body("Missing required fields: toEmail, subject, or body");
        }

        senderService.sendSimpleEmail(toEmail, subject, body);
        return ResponseEntity.ok("Email sent successfully");
    } catch (MailException e) {
        return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
    }
}
}