package auth_service.application.service;

import auth_service.domain.model.UserAccount;
import auth_service.domain.repository.UserAccountRepository;
import auth_service.infrastructure.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private final Map<String, String> resetCodes = new HashMap<>();
    private final Map<String, String> registrationCodes = new HashMap<>();

    private final Map<String, Boolean> verifiedEmails = new HashMap<>();



    public Map<String, Object> login(String email, String password) {
        UserAccount user = userAccountRepository.findByEmail(email);
        if (user == null) {
            return null;
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            Map<String, Object> claims = Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString()
            );

            String token = jwtUtil.generateToken(claims);

            Map<String, Object> userResponse = Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole().toString()
            );

            return Map.of("user", userResponse, "token", token);
        }

        return null;
    }



    public Map<String, String> sendResetCode(String email) {
        UserAccount user = userAccountRepository.findByEmail(email);
        if (user == null) {
            return Map.of("error", "Email not found");
        }

        String code = generateCode();
        resetCodes.put(email, code);

        String subject = "Password Reset Code";
        String message = "Your password reset code is: " + code + "\nUse this code to reset your password.";

        boolean emailSent = emailService.sendEmail(email, subject, message);
        return emailSent
                ? Map.of("message", "Reset code sent successfully!")
                : Map.of("error", "Failed to send email.");
    }

    public Map<String, String> verifyResetCode(String email, String code) {
        if (resetCodes.containsKey(email) && resetCodes.get(email).equals(code)) {
            resetCodes.remove(email);
            return Map.of("message", "Code verified successfully!");
        } else {
            throw new IllegalArgumentException("Invalid code.");
        }
    }

    public Map<String, String> resetPassword(String email, String newPassword) {
        UserAccount user = userAccountRepository.findByEmail(email);
        if (user == null) {
            return Map.of("error", "User not found.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userAccountRepository.save(user);
        resetCodes.remove(email);

        return Map.of("message", "Password changed successfully!");
    }

    public Map<String, String> sendRegistrationCode(String email) {
        UserAccount existing = userAccountRepository.findByEmail(email);
        if (existing != null) {
            return Map.of("error", "Email already registered.");
        }

        String code = generateCode();
        registrationCodes.put(email, code);

        String subject = "Confirm your email address";
        String message = "Your confirmation code is: " + code + "\nUse this code to complete your registration.";

        boolean emailSent = emailService.sendEmail(email, subject, message);
        return emailSent
                ? Map.of("message", "Confirmation code sent successfully!")
                : Map.of("error", "Failed to send confirmation code.");
    }

    public boolean verifyRegistrationCode(String email, String code) {
        if (registrationCodes.containsKey(email) && registrationCodes.get(email).equals(code)) {
            verifiedEmails.put(email, true);
            return true;
        }
        return false;
    }


    public void consumeRegistrationCode(String email) {
        registrationCodes.remove(email);
    }


    private String generateCode() {
        Random random = new Random();
        return String.valueOf(random.nextInt(900000) + 100000);
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    public boolean isEmailVerified(String email) {
        return verifiedEmails.getOrDefault(email, false);
    }

    public void clearVerifiedEmail(String email) {
        verifiedEmails.remove(email);
    }



    public boolean emailExists(String email) {
        return userAccountRepository.findByEmail(email) != null;
    }

    public Map<String, String> sendVerificationCode(String email) {
        if (emailExists(email)) {
            return Map.of("error", "Email already registered");
        }

        String code = generateCode();
        registrationCodes.put(email, code);

        String subject = "Email Verification Code";
        String message = "Your verification code is: " + code;

        boolean sent = emailService.sendEmail(email, subject, message);
        if (sent) {
            return Map.of("message", "Verification code sent successfully!");
        } else {
            return Map.of("error", "Failed to send email.");
        }
    }



}
