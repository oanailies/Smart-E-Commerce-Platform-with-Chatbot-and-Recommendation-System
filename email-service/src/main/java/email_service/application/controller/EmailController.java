package email_service.application.controller;

import email_service.application.dto.EmailRequest;
import email_service.application.service.EmailSender;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final EmailSender sender;

    public EmailController(EmailSender sender) {
        this.sender = sender;
    }

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody EmailRequest req) {
        if (req.getTo() == null || req.getSubject() == null || req.getMessage() == null) {
            return ResponseEntity.badRequest().body("Missing: to, subject, message");
        }
        try {
            sender.send(req.getTo(), req.getSubject(), req.getMessage());
            return ResponseEntity.ok("sent");
        } catch (Exception e) {
            return ResponseEntity.status(502).body(e.getMessage());
        }
    }

    @PostMapping(value = "/send-pdf", consumes = "multipart/form-data")
    public ResponseEntity<?> sendPdf(
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            @RequestParam("fileName") String fileName,
            @RequestParam("pdfBytes") org.springframework.web.multipart.MultipartFile pdfFile) {
        try {
            byte[] bytes = pdfFile.getBytes();
            sender.sendWithPdf(to, subject, message, fileName, bytes);
            return ResponseEntity.ok("sent with PDF");
        } catch (Exception e) {
            return ResponseEntity.status(502).body(e.getMessage());
        }
    }


}
