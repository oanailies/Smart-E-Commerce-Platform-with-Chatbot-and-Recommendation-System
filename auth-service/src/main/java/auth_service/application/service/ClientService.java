package auth_service.application.service;

import auth_service.domain.model.Client;
import auth_service.application.dto.ClientDTO;
import auth_service.domain.repository.ClientRepository;
import auth_service.domain.repository.UserAccountRepository;
import auth_service.domain.model.UserAccount;
import auth_service.domain.model.Role;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthService authService;

    public Client createClient(ClientDTO clientDTO) {
        String email = clientDTO.getEmail();
        if (!authService.isEmailVerified(email)) {
            throw new IllegalArgumentException("Email not verified. Please confirm your email first.");
        }

        UserAccount userAccount = new UserAccount(
                clientDTO.getUsername(),
                clientDTO.getEmail(),
                passwordEncoder.encode(clientDTO.getPassword()),
                Role.CLIENT
        );
        userAccountRepository.save(userAccount);

        Client client = new Client(
                clientDTO.getName(),
                clientDTO.getPhoneNumber(),
                userAccount
        );

        Client savedClient = clientRepository.save(client);
        sendWelcomeEmail(email, clientDTO.getName());
        authService.clearVerifiedEmail(email);

        return savedClient;
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Optional<Client> getClientById(Long id) {
        return clientRepository.findById(id);
    }

    public Optional<Client> updateClient(Long id, ClientDTO clientDTO) {
        return clientRepository.findById(id).map(client -> {
            client.setName(clientDTO.getName());
            client.setPhoneNumber(clientDTO.getPhoneNumber());
            UserAccount userAccount = client.getUserAccount();
            if (clientDTO.getEmail() != null && !clientDTO.getEmail().isBlank()) {
                userAccount.setEmail(clientDTO.getEmail());
            }
            if (clientDTO.getUsername() != null && !clientDTO.getUsername().isBlank()) {
                userAccount.setUsername(clientDTO.getUsername());
            }
            userAccountRepository.save(userAccount);

            return clientRepository.save(client);
        });
    }

    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }

    private void sendWelcomeEmail(String email, String name) {
        String subject = "Welcome to Our Store!";
        String message = "Dear " + name + ",\n\n" +
                "Thank you for registering with us! We are excited to have you as part of our community.\n\n" +
                "✔ Place orders easier and faster!\n" +
                "✔ Benefit from discounts and personalized promotions!\n" +
                "✔ Add products to your favorites section!\n\n" +
                "If you have any questions, feel free to reach out to us.\n\n" +
                "Best regards,\n" +
                "Your Clothing Store Team";

        emailService.sendEmail(email, subject, message);
    }

    public Optional<String> getClientNameById(Long id) {
        return getClientById(id).map(Client::getName);
    }

    public Optional<Client> getClientByEmail(String email) {
        UserAccount account = userAccountRepository.findByEmail(email);
        if (account == null) {
            return Optional.empty();
        }
        return clientRepository.findAll().stream()
                .filter(client -> client.getUserAccount().getId().equals(account.getId()))
                .findFirst();
    }

    @Transactional
    public boolean deleteUserByEmail(String email) {
        UserAccount user = userAccountRepository.findByEmail(email);
        if (user == null) {
            return false;
        }
        userAccountRepository.deleteByEmail(email);
        return true;
    }
}
