package auth_service.application.controller;

import auth_service.application.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody Map<String, String> request) {
        String to = request.get("to");
        String subject = request.get("subject");
        String message = request.get("message");

        if (to == null || subject == null || message == null) {
            return ResponseEntity.badRequest().body("Missing required fields: to, subject, message.");
        }

        boolean sent = emailService.sendEmail(to, subject, message);
        if (sent) {
            return ResponseEntity.ok("Email sent successfully to " + to);
        } else {
            return ResponseEntity.status(500).body("Failed to send email.");
        }
    }


}
