package auth_service.application.service;

import auth_service.domain.model.Admin;
import auth_service.application.dto.AdminDTO;
import auth_service.domain.repository.AdminRepository;
import auth_service.domain.repository.UserAccountRepository;
import auth_service.domain.model.UserAccount;
import auth_service.domain.model.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Admin createAdmin(AdminDTO adminDTO) {
        UserAccount userAccount = new UserAccount(
                adminDTO.getUsername(),
                adminDTO.getEmail(),
                passwordEncoder.encode(adminDTO.getPassword()),
                Role.ADMIN
        );
        userAccountRepository.save(userAccount);
        Admin admin = new Admin(
                adminDTO.getName(),
                adminDTO.getDepartment(),
                userAccount
        );
        return adminRepository.save(admin);
    }

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }

    public Optional<Admin> updateAdmin(Long id, AdminDTO adminDTO) {
        return adminRepository.findById(id).map(admin -> {
            admin.setName(adminDTO.getName());
            admin.setDepartment(adminDTO.getDepartment());
            return adminRepository.save(admin);
        });
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }
}
