package auth_service.application.controller;

import auth_service.application.dto.LoginDTO;
import auth_service.application.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        Map<String, Object> response = authService.login(loginDTO.getEmail(), loginDTO.getPassword());

        if (response == null) {
            return ResponseEntity.status(401).body("Invalid credentials");
        }

        return ResponseEntity.ok(response);
    }



    @PostMapping("/send-reset-code")
    public ResponseEntity<?> sendResetCode(@RequestParam String email) {
        return ResponseEntity.ok(authService.sendResetCode(email));
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<?> verifyResetCode(@RequestParam String email, @RequestParam String code) {
        try {
            return ResponseEntity.ok(authService.verifyResetCode(email, code));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid code."));
        }
    }


    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        return ResponseEntity.ok(authService.resetPassword(email, newPassword));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            boolean isValid = authService.validateToken(token);

            if (isValid) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(401).body("Invalid or expired token");
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Token validation error");
        }
    }


    @PostMapping("/send-registration-code")
    public ResponseEntity<?> sendRegistrationCode(@RequestParam String email) {
        Map<String, String> response = authService.sendRegistrationCode(email);
        if (response.containsKey("error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-registration-code")
    public ResponseEntity<?> verifyRegistrationCode(@RequestParam String email, @RequestParam String code) {
        boolean isValid = authService.verifyRegistrationCode(email, code);
        if (isValid) {
            return ResponseEntity.ok(Map.of("message", "Email verified successfully."));
        } else {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid verification code."));
        }
    }

    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = authService.emailExists(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestParam String email) {
        if (authService.emailExists(email)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }
        return ResponseEntity.ok(authService.sendVerificationCode(email));
    }




}
