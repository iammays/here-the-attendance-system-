//backend\src\main\java\com\here\backend\Emails\EmailController.java 

package com.here.backend.Emails;

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
